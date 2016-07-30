package io.foliage.netty.rpc.protocol;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

public enum RpcSerializeProtocol {

    JDK_SERIALIZE("jdknative"), KRYO_SERIALIZE("kryo");

    private final String value;

    RpcSerializeProtocol(String serializeProtocol) {
        this.value = serializeProtocol;
    }

    public String value() {
        return value;
    }

    public static RpcSerializeProtocol fromValue(String v) {
        if (v == null) {
            throw new NullPointerException("Serialize protocol can't be null");
        }
        for (RpcSerializeProtocol protocol : RpcSerializeProtocol.values()) {
            if (Objects.equals(protocol.value, v)) {
                return protocol;
            }
        }
        throw new IllegalArgumentException(v);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
