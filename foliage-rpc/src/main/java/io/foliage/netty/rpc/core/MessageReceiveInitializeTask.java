package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.MessageRequest;
import io.foliage.netty.rpc.protocol.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class MessageReceiveInitializeTask implements Callable<Boolean> {

    private MessageRequest request = null;
    private MessageResponse response = null;
    private Map<String, Object> handlerMap = null;

    public MessageResponse getResponse() {
        return response;
    }

    public MessageRequest getRequest() {
        return request;
    }

    public void setRequest(MessageRequest request) {
        this.request = request;
    }

    public MessageReceiveInitializeTask(MessageRequest request, MessageResponse response, Map<String, Object> handlerMap) {
        this.request = request;
        this.response = response;
        this.handlerMap = handlerMap;
    }

    public Boolean call() {
        response.setMessageId(request.getMessageId());
        try {
            Object result = reflect(request);
            response.setResultDesc(result);
            return Boolean.TRUE;
        } catch (Throwable t) {
            response.setError(t.toString());
            log.error("RPC Server invoke error!", t);
            return Boolean.FALSE;
        }
    }

    private Object reflect(MessageRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);
        String methodName = request.getMethodName();
        Object[] parameters = request.getParametersVal();
        return MethodUtils.invokeMethod(serviceBean, methodName, parameters);
    }
}
