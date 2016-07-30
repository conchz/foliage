package io.foliage.netty.rpc.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.foliage.netty.rpc.annotation.RpcListenerContainer;
import io.foliage.netty.rpc.protocol.MessageRequest;
import io.foliage.netty.rpc.protocol.MessageResponse;
import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.foliage.netty.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class MessageReceiveExecutor implements ApplicationContextAware, InitializingBean {

    private static ListeningExecutorService threadPoolExecutor;

    private final Map<String, Object> handlerMap = new ConcurrentHashMap<>();
    private final RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.KRYO_SERIALIZE;

    private final String serverAddress;
    private final ServiceRegistry serviceRegistry;

    public MessageReceiveExecutor(String serverAddress, ServiceRegistry serviceRegistry) {
        Assert.notNull(serverAddress);
        Assert.notNull(serviceRegistry);

        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Callable<Boolean> task, ChannelHandlerContext ctx, MessageRequest request, MessageResponse response) {
        if (threadPoolExecutor == null) {
            synchronized (MessageReceiveExecutor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));
                }
            }
        }

        ListenableFuture<Boolean> listenableFuture = threadPoolExecutor.submit(task);
        // Netty服务端把计算结果异步返回
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                ctx.writeAndFlush(response).addListener(channelFuture -> {
                    log.info("RPC server send messageId response: {}", request.getMessageId());
                });
            }

            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcListenerContainer.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean
                        .getClass()
                        .getAnnotation(RpcListenerContainer.class)
                        .value()
                        .getName();

                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        // Netty的线程池模型设置成主从线程池模式, 这样可以应对高并发请求
        // 当然netty还支持单线程、多线程网络IO模型, 可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory = new NamedThreadFactory("NettyRPC ThreadFactory");

        // 方法返回到Java虚拟机的可用的处理器数量
        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(new MessageReceiveChannelInitializer(handlerMap).buildRpcSerializeProtocol(serializeProtocol))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] hostArr = serverAddress.split(":");

            if (hostArr.length == 2) {
                String host = hostArr[0];
                int port = Integer.parseInt(hostArr[1]);
                ChannelFuture future = bootstrap.bind(host, port).sync();
                log.info("Netty RPC server started successfully! host: {}, port {}, protocol: {}", host, port, serializeProtocol);

                serviceRegistry.register(serverAddress);

                future.channel().closeFuture().sync();
            } else {
                log.error("Netty RPC server start failed!");
            }
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}