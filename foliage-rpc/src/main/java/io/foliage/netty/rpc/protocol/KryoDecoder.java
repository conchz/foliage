package io.foliage.netty.rpc.protocol;

public class KryoDecoder extends MessageDecoder {

    public KryoDecoder(final MessageCodec codec) {
        super(codec);
    }
}
