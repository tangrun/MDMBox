package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.core.JavaFXApplication;
import com.tangrun.mdm.boxwindow.utils.Utils;
import javafx.application.Application;
import javafx.scene.control.Alert;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

@Log4j2
public class Launcher {
    public static void main(String[] args) throws InterruptedException {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(e.getMessage(),e);
            }
        });

        log.debug("launcher start javafx");
        Application.launch(JavaFXApplication.class,args);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.executeCmd("adb kill-server");
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
            }
        }));
        log.debug("launcher exit");
    }
}
