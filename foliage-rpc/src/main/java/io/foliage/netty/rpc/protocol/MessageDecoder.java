package io.foliage.netty.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    public static final int MESSAGE_LENGTH = MessageCodec.MESSAGE_LENGTH;
    private final MessageCodec codec;

    public MessageDecoder(final MessageCodec codec) {
        this.codec = codec;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 出现粘包导致消息头长度不对, 直接返回
        if (in.readableBytes() < MessageDecoder.MESSAGE_LENGTH) {
            return;
        }

        in.markReaderIndex();
        // 读取消息的内容长度
        int messageLength = in.readInt();

        if (messageLength < 0) {
            ctx.close();
        }

        // 读到的消息长度和报文头的已知长度不匹配, 重置ByteBuf读索引的位置
        if (in.readableBytes() < messageLength) {
            in.resetReaderIndex();
            return;
        } else {
            byte[] messageBody = new byte[messageLength];
            in.readBytes(messageBody);

            try {
                Object obj = codec.decode(messageBody);
                out.add(obj);
            } catch (IOException ex) {
                log.error("", ex);
            }
        }
    }
}
