package io.foliage.netty.rpc.core;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by lavenderx on 2016-07-27.
 */
public enum RpcSerializeProtocol {

    //目前由于没有引入跨语言RPC通信机制，暂时采用支持同构语言Java序列化/反序列化机制的第三方插件
    //NettyRPC目前已知的序列化插件有:Java原生序列化、Kryo、Hessian
    JDKSERIALIZE("jdknative"), KRYOSERIALIZE("kryo"), HESSIANSERIALIZE("hessian");

    private String serializeProtocol;

    private RpcSerializeProtocol(String serializeProtocol) {
        this.serializeProtocol = serializeProtocol;
    }

    public String toString() {
        ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
        return ReflectionToStringBuilder.toString(this);
    }

    public String getProtocol() {
        return serializeProtocol;
    }
}
