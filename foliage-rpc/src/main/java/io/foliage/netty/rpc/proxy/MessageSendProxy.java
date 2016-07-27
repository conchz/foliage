package io.foliage.netty.rpc.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import io.foliage.netty.rpc.core.MessageCallBack;
import io.foliage.netty.rpc.core.MessageSendHandler;
import io.foliage.netty.rpc.core.RpcServerLoader;
import io.foliage.netty.rpc.protocol.MessageRequest;

import java.lang.reflect.Method;
import java.util.UUID;

public class MessageSendProxy<T> extends AbstractInvocationHandler {

    public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        MessageRequest request = new MessageRequest();
        request.setMessageId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setTypeParameters(method.getParameterTypes());
        request.setParametersVal(args);

        MessageSendHandler handler = RpcServerLoader.getInstance().getMessageSendHandler();
        MessageCallBack callBack = handler.sendRequest(request);
        return callBack.start();
    }
}