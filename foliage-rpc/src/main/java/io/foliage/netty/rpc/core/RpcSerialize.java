package io.foliage.netty.rpc.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RpcSerialize {

    void serialize(OutputStream output, Object obj) throws IOException;

    Object deserialize(InputStream input) throws IOException;

}
