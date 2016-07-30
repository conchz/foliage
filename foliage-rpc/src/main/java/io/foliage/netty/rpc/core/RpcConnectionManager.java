package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcConnectionManager {

    private volatile static RpcConnectionManager rpcConnectionManager;

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private final ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(65536));

    private final CopyOnWriteArrayList<MessageSendHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<InetSocketAddress, MessageSendHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition connected = lock.newCondition();
    private final AtomicInteger roundRobin = new AtomicInteger(0);
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    protected long connectTimeoutMillis = 6000;

    public static RpcConnectionManager getInstance() {
        if (rpcConnectionManager == null) {
            synchronized (RpcConnectionManager.class) {
                if (rpcConnectionManager == null) {
                    rpcConnectionManager = new RpcConnectionManager();
                }
            }
        }
        return rpcConnectionManager;
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {  // Get available server node
                // Update local serverNodes cache
                HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<>();
                for (String address : allServerAddress) {
                    String[] array = address.split(":");
                    if (array.length == 2) { // Should check IP and port
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        final InetSocketAddress remotePeer = new InetSocketAddress(host, port);
                        newAllServerNodeSet.add(remotePeer);
                    }
                }

                // Add new server node
                newAllServerNodeSet.stream()
                        .filter(serverNodeAddress -> !connectedServerNodes.keySet().contains(serverNodeAddress))
                        .forEach(this::connectServerNode);

                // Close and remove invalid server nodes
                for (int i = 0; i < connectedHandlers.size(); ++i) {
                    MessageSendHandler connectedServerHandler = connectedHandlers.get(i);
                    SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                    if (!newAllServerNodeSet.contains(remotePeer)) {
                        log.info("Remove invalid server node " + remotePeer);
                        MessageSendHandler handler = connectedServerNodes.get(remotePeer);
                        handler.close();
                        connectedServerNodes.remove(remotePeer);
                        connectedHandlers.remove(connectedServerHandler);
                    }
                }

            } else { // No available server node ( All server nodes are down )
                log.error("No available server node. All server nodes are down!");
                for (final MessageSendHandler connectedServerHandler : connectedHandlers) {
                    SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                    MessageSendHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(connectedServerHandler);
                }
                connectedHandlers.clear();
            }
        }
    }

    public void reconnect(final MessageSendHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }

        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.submit((Runnable) () -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new MessageSendChannelInitializer().buildRpcSerializeProtocol(RpcSerializeProtocol.KRYO_SERIALIZE));

            ChannelFuture channelFuture = bootstrap.connect(remotePeer);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.debug("Successfully connect to remote server. remote peer = " + remotePeer);
                        MessageSendHandler handler = channelFuture.channel().pipeline().get(MessageSendHandler.class);
                        addHandler(handler);
                    }
                }
            });
        });
    }

    private void addHandler(MessageSendHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public MessageSendHandler chooseHandler() {
        CopyOnWriteArrayList<MessageSendHandler> handlers =
                (CopyOnWriteArrayList<MessageSendHandler>) connectedHandlers.clone();
        int size = handlers.size();
        while (isRunning.get() && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    handlers = (CopyOnWriteArrayList<MessageSendHandler>) connectedHandlers.clone();
                    size = handlers.size();
                }
            } catch (InterruptedException ex) {
                log.error("Waiting for available node is interrupted!", ex);
                throw new RuntimeException("Can't connect any servers!", ex);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;

        return handlers.get(index);
    }

    public void stop() {
        isRunning.set(false);
        connectedHandlers.forEach(MessageSendHandler::close);
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}

