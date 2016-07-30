package io.foliage.netty.rpc.core;

import io.foliage.netty.rpc.protocol.MessageRequest;
import io.foliage.netty.rpc.protocol.MessageResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageCallBack {

    private final Lock lock = new ReentrantLock();
    private final Condition finish = lock.newCondition();
    private MessageRequest request;
    private MessageResponse response;

    public MessageCallBack(MessageRequest request) {
        this.request = request;
    }

    public Object start() throws InterruptedException {
        try {
            lock.lock();
            // 设定一下超时时间, rpc服务器太久没有相应的话, 就默认返回空.
            finish.await(10_1000, TimeUnit.MILLISECONDS);
            if (this.response != null) {
                return this.response.getResultDesc();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void over(MessageResponse response) {
        try {
            lock.lock();
            finish.signal();
            this.response = response;
        } finally {
            lock.unlock();
        }
    }
}
