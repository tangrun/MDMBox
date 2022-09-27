package com.tangrun.mdm.boxwindow.shell.core;

public class ShellApiExecResult<T> {
    public boolean success;
    public String msg;
    public T data;

    public static <T> ShellApiExecResult<T> success(T data) {
        ShellApiExecResult<T> result = new ShellApiExecResult<>();
        result.data = data;
        result.success = true;
        return result;
    }

    public static <T> ShellApiExecResult<T> fail(String msg) {
        ShellApiExecResult<T> result = new ShellApiExecResult<>();
        result.msg = msg;
        result.success = false;
        return result;
    }

}
