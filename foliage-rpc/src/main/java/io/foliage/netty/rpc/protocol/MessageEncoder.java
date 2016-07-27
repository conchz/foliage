package io.foliage.netty.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Object> {

    private MessageCodec codec = null;

    public MessageEncoder(final MessageCodec codec) {
        this.codec = codec;
    }

    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out)
            throws Exception {
        codec.encode(out, msg);
    }
}
