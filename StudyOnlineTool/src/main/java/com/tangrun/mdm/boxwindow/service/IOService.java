package com.tangrun.mdm.boxwindow.service;

import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class IOService implements LifecycleEventListener {
    private static IOService ioThread;

    public static IOService getInstance(){
        if (ioThread == null)
            ioThread = new IOService();
        return ioThread;
    }

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private IOService() {

    }

    public void executeAwait(Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                }finally {
                    countDownLatch.countDown();
                }
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
    }

    public void execute(Runnable runnable) {
        if (ioThread == null) ioThread = new IOService();
        if (ioThread.executor.isShutdown()) return;
        ioThread.executor.execute(runnable);
    }

    @Override
    public void onEvent(LifecycleState state) {
        if (state == LifecycleState.OnReady){

        }else if (state == LifecycleState.OnClosed){
            log.debug("io thread stopping");
            if (ioThread != null && !ioThread.executor.isShutdown()) ioThread.executor.shutdownNow();
            log.debug("io thread stopped");
        }
    }
}
