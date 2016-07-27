package io.foliage.netty.rpc.proxy;

import io.foliage.netty.rpc.core.MessageCallBack;
import io.foliage.netty.rpc.core.MessageSendHandler;
import io.foliage.netty.rpc.core.RpcServerLoader;
import io.foliage.netty.rpc.protocol.MessageRequest;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;

public class RpcProxy<T> implements MethodInterceptor {

    private final Class<T> clazz;

    public RpcProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
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
