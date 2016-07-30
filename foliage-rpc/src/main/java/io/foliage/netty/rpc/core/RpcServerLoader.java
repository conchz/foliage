package io.foliage.netty.rpc.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcServerLoader {

    private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;
    private static ListeningExecutorService threadPoolExecutor =
            MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));
    private volatile static RpcServerLoader rpcServerLoader;

    // 等待Netty服务端链路建立通知信号
    private final Lock lock = new ReentrantLock();
    private final Condition connectStatus = lock.newCondition();
    private final Condition handlerStatus = lock.newCondition();

    // Netty nio线程池
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);
    private MessageSendHandler messageSendHandler = null;

    public static RpcServerLoader getInstance() {
        if (rpcServerLoader == null) {
            synchronized (RpcServerLoader.class) {
                if (rpcServerLoader == null) {
                    rpcServerLoader = new RpcServerLoader();
                }
            }
        }
        return rpcServerLoader;
    }

    public void load(String serverAddress, RpcSerializeProtocol serializeProtocol) {
        String[] hostArr = serverAddress.split(":");
        if (hostArr.length == 2) {
            String host = hostArr[0];
            int port = Integer.parseInt(hostArr[1]);
            final InetSocketAddress remotePeer = new InetSocketAddress(host, port);

            ListenableFuture<Boolean> listenableFuture = threadPoolExecutor
                    .submit(new MessageSendInitializeTask(eventLoopGroup, remotePeer, serializeProtocol));

            // 监听线程池异步的执行结果成功与否再决定是否唤醒全部的客户端RPC线程
            Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    try {
                        lock.lock();

                        if (messageSendHandler == null) {
                            handlerStatus.await();
                        }

                        // Futures异步回调, 唤醒所有rpc等待线程
                        if (result == Boolean.TRUE && messageSendHandler != null) {
                            connectStatus.signalAll();
                        }
                    } catch (InterruptedException ex) {
                        log.error("", ex);
                    } finally {
                        lock.unlock();
                    }
                }

                public void onFailure(Throwable t) {
                    log.error("", t);
                }
            }, threadPoolExecutor);
        }
    }

    public void setMessageSendHandler(MessageSendHandler messageInHandler) {
        try {
            lock.lock();
            this.messageSendHandler = messageInHandler;
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    public MessageSendHandler getMessageSendHandler() throws InterruptedException {
        try {
            lock.lock();
            // Netty服务端链路没有建立完毕之前, 先挂起等待
            if (messageSendHandler == null) {
                connectStatus.await();
            }
            return messageSendHandler;
        } finally {
            lock.unlock();
        }
    }

    public void unLoad() {
        messageSendHandler.close();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
