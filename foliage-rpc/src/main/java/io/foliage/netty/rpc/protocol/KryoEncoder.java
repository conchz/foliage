package io.foliage.netty.rpc.protocol;

public class KryoEncoder extends MessageEncoder {

    public KryoEncoder(final MessageCodec codec) {
        super(codec);
    }
}
