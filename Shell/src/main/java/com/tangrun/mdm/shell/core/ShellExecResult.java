package com.tangrun.mdm.shell.core;

public class ShellExecResult  {

    public  int exitValue;
    public String out;
    public String error;

    public ShellExecResult() {
    }

    public ShellExecResult(int exitValue, String out, String error) {
        this.exitValue = exitValue;
        this.out = out;
        this.error = error;
    }

    public boolean existOk() {
        return exitValue == 0;
    }

    @Override
    public String toString() {
        return "ExecResult{" +
                "existValue='" + exitValue + '\'' +
                ", out='" + out + '\'' +
                ", error='" + error + '\'' +
                ", exception='" + exitValue + '\'' +
                '}';
    }
}
