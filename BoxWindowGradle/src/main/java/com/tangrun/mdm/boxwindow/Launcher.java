package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.core.JavaFXApplication;
import javafx.application.Application;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Launcher {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(Launcher.class.getResource("/fxml/main.fxml"));
//        System.out.println(Launcher.class.getResource("/static"));
//        System.out.println(Launcher.class.getResource("/log4j2.xml"));

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(e.getMessage(),e);
            }
        });

        log.debug("launcher start javafx");
        Application.launch(JavaFXApplication.class,args);
        log.debug("launcher exit");
    }
}
