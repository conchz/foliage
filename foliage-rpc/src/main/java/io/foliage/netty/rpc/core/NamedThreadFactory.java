package io.foliage.netty.rpc.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    private final AtomicInteger mThreadNum = new AtomicInteger(1);
    private final String prefix;
    private final boolean daemonThread;
    private final ThreadGroup threadGroup;

    public NamedThreadFactory() {
        this("rpcserver-threadpool-" + threadNumber.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix + "-thread-";
        daemonThread = daemon;
        SecurityManager securityManager = System.getSecurityManager();
        threadGroup = securityManager == null ? Thread.currentThread().getThreadGroup()
                : securityManager.getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        String name = prefix + mThreadNum.getAndIncrement();
        Thread t = new Thread(threadGroup, r, name, 0);
        t.setDaemon(daemonThread);
        return t;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
