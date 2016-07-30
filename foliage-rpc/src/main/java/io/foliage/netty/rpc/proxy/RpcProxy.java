package io.foliage.netty.rpc.proxy;

import io.foliage.netty.rpc.core.MessageCallBack;
import io.foliage.netty.rpc.core.MessageSendHandler;
import io.foliage.netty.rpc.core.RpcServerLoader;
import io.foliage.netty.rpc.protocol.MessageRequest;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Pattern;

public class RpcProxy<T> implements MethodInterceptor {

    private static final Pattern PATTERN = Pattern.compile("-");

    private final Class<T> clazz;

    public RpcProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        MessageRequest request = new MessageRequest();
        request.setMessageId(PATTERN.matcher(UUID.randomUUID().toString()).replaceAll(""));
        request.setClassName(clazz.getName());
        request.setMethodName(method.getName());
        request.setTypeParameters(method.getParameterTypes());
        request.setParametersVal(args);

        MessageSendHandler handler = RpcServerLoader.getInstance().getMessageSendHandler();
        MessageCallBack callBack = handler.sendRequest(request);

        return callBack.start();
    }
}
