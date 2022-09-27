package com.tangrun.mdm.boxwindow.shell;

import com.tangrun.mdm.boxwindow.shell.core.ShellExecResult;
import com.tangrun.mdm.boxwindow.shell.core.ShellExecutor;
import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;

public class ShellExecutorImpl implements ShellExecutor {
    private final DefaultExecutor defaultExecutor;
    private final ByteArrayOutputStream outputStream;
    private final ByteArrayOutputStream errorStream;
    private final PumpStreamHandler pumpStreamHandler;

    public ShellExecutorImpl() {
        defaultExecutor = new DefaultExecutor();
        defaultExecutor.setWorkingDirectory(new File("./lib/"));
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        defaultExecutor.setStreamHandler(pumpStreamHandler);
    }

    public ShellExecResult execute(String commandLine) {
        ShellExecResult result = new ShellExecResult();
        int exitValue = Executor.INVALID_EXITVALUE;
        try {
            exitValue = defaultExecutor.execute(CommandLine.parse(commandLine)//, System.getenv()
            );
            result.out = getOutAsString();
            result.error = getErrorAsString();
            result.exitValue = exitValue;
        } catch (final ExecuteException e) {
            result.error = getErrorAsString();
            result.exitValue = e.getExitValue();
        } catch (final Exception e) {
            result.error = e.getMessage();
            result.exitValue = exitValue;
        }
        return result;
    }

    public String getOutAsString() {
        try {
            String content = outputStream.toString("utf-8");
            outputStream.reset();
            return content;
            //return EncodeUtil.convert(content,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getErrorAsString() {
        try {
            String content = errorStream.toString("utf-8");
            errorStream.reset();
            return content;
            //return EncodeUtil.convert(content,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        errorStream.reset();
        return "";
    }
}
