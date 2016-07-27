package io.foliage.netty.rpc.protocol;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public enum RpcSerializeProtocol {

    JDK_SERIALIZE("jdknative"), KRYO_SERIALIZE("kryo");

    private String serializeProtocol;

    RpcSerializeProtocol(String serializeProtocol) {
        this.serializeProtocol = serializeProtocol;
    }

    public String getProtocol() {
        return serializeProtocol;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
