package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.KryoCodec;
import io.foliage.netty.rpc.protocol.KryoDecoder;
import io.foliage.netty.rpc.protocol.KryoEncoder;
import io.foliage.netty.rpc.protocol.MessageCodec;
import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Map;

public class RpcReceiveSerializerFrame implements RpcSerializerFrame {

    private Map<String, Object> handlerMap = null;

    public RpcReceiveSerializerFrame(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    //后续可以优化成通过spring ioc方式注入
    public void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline) {
        switch (protocol) {
            case JDK_SERIALIZE: {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageCodec.MESSAGE_LENGTH, 0, MessageCodec.MESSAGE_LENGTH));
                pipeline.addLast(new LengthFieldPrepender(MessageCodec.MESSAGE_LENGTH));
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                pipeline.addLast(new MessageReceiveHandler(handlerMap));
                break;
            }
            case KRYO_SERIALIZE: {
                KryoCodec util = new KryoCodec(KryoPoolFactory.getKryoPoolInstance());
                pipeline.addLast(new KryoEncoder(util));
                pipeline.addLast(new KryoDecoder(util));
                pipeline.addLast(new MessageReceiveHandler(handlerMap));
                break;
            }
            default:
                break;
        }
    }
}
