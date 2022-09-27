package com.tangrun.mdm.boxwindow.shell.core;

import com.tangrun.mdm.boxwindow.shell.core.ShellExecResult;
import com.tangrun.mdm.boxwindow.shell.core.ShellExecutor;
import com.tangrun.mdm.boxwindow.shell.core.ShellInterceptor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;


@Log4j2
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
