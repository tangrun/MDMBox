package com.tangrun.mdm.boxwindow.impl;

import com.tangrun.mdm.boxwindow.shell.core.ShellExecResult;
import com.tangrun.mdm.boxwindow.shell.core.ShellExecutor;
import com.tangrun.mdm.boxwindow.shell.core.ShellInterceptor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ShellExecuteLogger implements ShellInterceptor {
//    Logger logger = LogManager.getLogger(ShellExecuteLogger.class);
    @Override
    public ShellExecResult execute(String command, ShellExecutor executor) {
        long time = System.currentTimeMillis();
        ShellExecResult execute = executor.execute(command);
        log.debug("\ncmd execute: {}\n耗时: {}S, existValue: {}\nout:\n{}error:\n{}",
                command, (1.0f * System.currentTimeMillis() - time) / 1000, execute.exitValue, execute.out, execute.error);
        return execute;
    }
}
