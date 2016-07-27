package io.foliage.netty.rpc.protocol;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.io.Closer;
import io.foliage.netty.rpc.core.KryoSerializer;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoCodec implements MessageCodec {

    private static Closer closer = Closer.create();

    private final KryoPool pool;

    public KryoCodec(KryoPool pool) {
        this.pool = pool;
    }

    public void encode(final ByteBuf out, final Object message) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            closer.register(byteArrayOutputStream);
            KryoSerializer kryoSerializer = new KryoSerializer(pool);
            kryoSerializer.serialize(byteArrayOutputStream, message);
            byte[] body = byteArrayOutputStream.toByteArray();
            int dataLength = body.length;
            out.writeInt(dataLength);
            out.writeBytes(body);
        } finally {
            closer.close();
        }
    }

    public Object decode(final byte[] body) throws IOException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
            closer.register(byteArrayInputStream);
            KryoSerializer kryoSerializer = new KryoSerializer(pool);
            Object obj = kryoSerializer.deserialize(byteArrayInputStream);
            return obj;
        } finally {
            closer.close();
        }
    }
}
