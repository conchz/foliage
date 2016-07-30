package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class MessageSendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final RpcSendSerializerFrame frame = new RpcSendSerializerFrame();
    private RpcSerializeProtocol protocol;

    public MessageSendChannelInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        frame.select(protocol, socketChannel.pipeline());
    }
}
