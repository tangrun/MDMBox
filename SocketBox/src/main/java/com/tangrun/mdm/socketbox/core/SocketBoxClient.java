package com.tangrun.mdm.socketbox.core;

import android.util.Log;
import com.tangrun.mdm.shell.core.ShellExecResult;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketBoxClient {
    private final String TAG = "HackRoot SocketClient";
    public static int sPORT = 10500;
    private SocketListener listener;
    private PrintWriter printWriter;

    public SocketBoxClient(final String cmd, SocketListener listener, OnShellResult shellResult) {
        this.listener = listener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress("127.0.0.1", sPORT), 10_000);
                    socket.setSoTimeout(10_000);
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    Log.d(TAG, "client send: " + cmd);
                    printWriter.println(cmd);
                    printWriter.flush();
                    readServerData(socket);
                } catch (IOException e) {
                    Log.d(TAG, "client send fail: " + e.getMessage());
                    e.printStackTrace();
                    if (shellResult != null) {
                        shellResult.onResult(new ShellExecResult(257, "", e.getMessage()));
                    }
                }
            }
        }).start();
    }

    private void readServerData(final Socket socket) throws IOException {
        InputStreamReader ipsReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bfReader = new BufferedReader(ipsReader);
        String line = null;
        while ((line = bfReader.readLine()) != null) {
            Log.d(TAG, "client receive: " + line);
            listener.onMessage(line);
        }
        ipsReader.close();
        bfReader.close();
        printWriter.close();
        socket.close();
    }

    interface SocketListener {
        void onMessage(String msg);
    }

    public static void execShellForSocket(String common, OnShellResult shellResult) {
        new SocketBoxClient(common, msg -> {
            Log.d("TAG", "execShellForSocket: " + common + "\nresult: " + msg);
            ShellExecResult execResult = null;
            try {
                JSONObject jsonObject = new JSONObject(msg);
                execResult = new ShellExecResult(jsonObject.getInt("code"), jsonObject.getString("success"), jsonObject.getString("error"));
            } catch (Exception e) {
                e.printStackTrace();
                execResult = new ShellExecResult(256, "", e.getMessage());
            }
            if (shellResult != null) shellResult.onResult(execResult);
        }, shellResult);
    }

    public interface OnShellResult {
        void onResult(ShellExecResult result);
    }
}
