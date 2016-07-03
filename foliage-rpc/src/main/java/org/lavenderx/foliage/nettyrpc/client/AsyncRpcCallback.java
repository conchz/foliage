package org.lavenderx.foliage.nettyrpc.client;

public interface AsyncRpcCallback {

    void success(Object result);

    void fail(Exception ex);
}
