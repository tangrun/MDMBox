package com.tangrun.mdm.socketbox.impl;

import com.tangrun.mdm.shell.core.ShellExecResult;
import com.tangrun.mdm.shell.core.ShellExecutor;
import com.tangrun.mdm.socketbox.core.SocketBoxClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author RainTang
 * @description:
 * @date :2021/12/8 9:39
 */
public class ShellExecutorAndroidSocketImpl implements ShellExecutor {

    @Override
    public ShellExecResult execute(String commandLine) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<ShellExecResult> shellExecResultAtomicReference = new AtomicReference<>();
        SocketBoxClient.execShellForSocket(commandLine, new SocketBoxClient.OnShellResult() {
            @Override
            public void onResult(ShellExecResult result) {
                shellExecResultAtomicReference.set(result);
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return shellExecResultAtomicReference.get();
    }
}
