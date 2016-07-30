package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.foliage.netty.rpc.proxy.RpcProxy;
import io.foliage.netty.rpc.registry.ServiceDiscovery;
import net.sf.cglib.proxy.Enhancer;

public class MessageSendExecutor {

    private final RpcServerLoader loader = RpcServerLoader.getInstance();
    private final RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.KRYO_SERIALIZE;
    private final ServiceDiscovery serviceDiscovery;

    public MessageSendExecutor(String serverAddress, ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        loader.load(serverAddress, serializeProtocol);
    }

    public void setRpcServerLoader(String serverAddress, RpcSerializeProtocol serializeProtocol) {
        loader.load(serverAddress, serializeProtocol);
    }

    public void stop() {
        serviceDiscovery.stop();
        loader.unLoad();
    }

    @SuppressWarnings("unchecked")
    public static <T> T execute(Class<T> rpcInterface) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(rpcInterface);
        enhancer.setCallback(new RpcProxy<>(rpcInterface));
        return (T) enhancer.create();
    }
}
