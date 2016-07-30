package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageSendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final RpcSendSerializerFrame frame = new RpcSendSerializerFrame();
    private RpcSerializeProtocol protocol;

    public MessageSendChannelInitializer buildRpcSerializeProtocol(final RpcSerializeProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));

        frame.select(protocol, channelPipeline);
    }
}
