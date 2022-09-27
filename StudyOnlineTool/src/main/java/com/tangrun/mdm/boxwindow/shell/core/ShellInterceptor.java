package com.tangrun.mdm.boxwindow.shell.core;

public interface ShellInterceptor {
    ShellExecResult execute(String command, ShellExecutor executor);
}
