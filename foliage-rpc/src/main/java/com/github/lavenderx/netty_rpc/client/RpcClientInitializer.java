package com.github.lavenderx.netty_rpc.client;

import com.github.lavenderx.netty_rpc.protocol.RpcDecoder;
import com.github.lavenderx.netty_rpc.protocol.RpcEncoder;
import com.github.lavenderx.netty_rpc.protocol.RpcRequest;
import com.github.lavenderx.netty_rpc.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder(RpcRequest.class))
            .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
            .addLast(new RpcDecoder(RpcResponse.class))
            .addLast(new RpcClientHandler());
    }
}
