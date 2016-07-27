package io.foliage.netty.rpc.core;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface MessageCodec {

    // RPC消息报文头长度4个字节
    int MESSAGE_LENGTH = 4;

    void encode(final ByteBuf out, final Object message) throws IOException;

    Object decode(final byte[] body) throws IOException;
}
