package com.tangrun.mdm.boxwindow.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOThread {
    private static IOThread ioThread;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private IOThread() {

    }

    public static void shutdown() {
        if (ioThread != null) ioThread.executor.shutdown();
    }

    public static synchronized void run(Runnable runnable) {
        if (ioThread == null) ioThread = new IOThread();
        if (ioThread.executor.isShutdown()) return;
        ioThread.executor.execute(runnable);
    }
}
