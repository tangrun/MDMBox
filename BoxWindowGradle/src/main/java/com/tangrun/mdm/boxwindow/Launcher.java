package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.core.JavaFXApplication;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.service.ConfigService;
import com.tangrun.mdm.boxwindow.utils.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Consumer;

@Log4j2
public class Launcher {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(Launcher.class.getResource("/fxml/main.fxml"));
//        System.out.println(Launcher.class.getResource("/static"));
//        System.out.println(Launcher.class.getResource("/log4j2.xml"));

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(e.getMessage(), e);
                ConfigWrapper configWrapper = ConfigService.getInstance().getConfig();
                if (configWrapper.getConfig() != null && configWrapper.getConfig().getDebug() == Boolean.TRUE) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            StringWriter stringWriter = new StringWriter();
                            PrintWriter printWriter = new PrintWriter(stringWriter);
                            e.printStackTrace(printWriter);
                            String contentText = stringWriter.toString();

                            Button copyBtn = new Button("复制内容");
                            Button exitBtn = new Button("退出程序");
                            Button closeBtn = new Button("关闭窗口");

                            Stage stage = Dialogs.CreateStage("程序出错", contentText, copyBtn, exitBtn, closeBtn);

                            copyBtn.setOnAction(event -> {
                                Toolkit.getDefaultToolkit().getSystemClipboard()
                                        .setContents(new StringSelection(stringWriter.toString()), null);
                            });
                            exitBtn.setOnAction(event -> {
                                Platform.exit();
                            });
                            closeBtn.setOnAction(event -> {
                                stage.close();
                            });
                            stage.show();
                        }
                    });
                }
            }
        });

        log.debug("launcher start javafx");
        Application.launch(JavaFXApplication.class, args);
        log.debug("launcher exit");
    }
}
