package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

public class MessageReceiveChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerializeProtocol protocol;
    private RpcReceiveSerializerFrame frame = null;

    MessageReceiveChannelInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    MessageReceiveChannelInitializer(Map<String, Object> handlerMap) {
        this.frame = new RpcReceiveSerializerFrame(handlerMap);
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        frame.select(protocol, socketChannel.pipeline());
    }
}
