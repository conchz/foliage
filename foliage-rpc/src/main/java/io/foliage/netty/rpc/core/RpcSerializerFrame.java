package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.RpcSerializeProtocol;
import io.netty.channel.ChannelPipeline;

public interface RpcSerializerFrame {

    public void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline);
}
