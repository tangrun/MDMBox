package com.tangrun.mdm.shell.core;

public interface ShellInterceptor {
    ShellExecResult execute(String command,ShellExecutor executor);
}
