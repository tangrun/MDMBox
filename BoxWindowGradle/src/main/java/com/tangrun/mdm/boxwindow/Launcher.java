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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

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
//        {
//            LoggerContext context = (LoggerContext) LogManager.getContext(false);
//            Configuration configuration = context.getConfiguration();
//            System.out.println("debugFilePath: "+((RollingRandomAccessFileAppender) configuration.getAppender("debugFile")).getFileName());
//            System.out.println("errorFilePath: "+((RollingRandomAccessFileAppender) configuration.getAppender("errorFile")).getFileName());
//
//            {
//                RollingRandomAccessFileAppender errorFileAppender = RollingRandomAccessFileAppender.newBuilder()
//                        .withFileName(ConfigService.getLogDir() + "error.log")
//                        .withFilePattern(ConfigService.getLogDir() + "error_%d{yyyyMMdd_HHmm}")
//                        .setName("errorFile")
//                        .withPolicy(TimeBasedTriggeringPolicy.newBuilder()
//                                .withInterval(30)
//                                .build())
//                        .setFilter(ThresholdFilter.createFilter(Level.ERROR, Filter.Result.ACCEPT, Filter.Result.DENY))
//                        .setLayout(PatternLayout.newBuilder().withPattern("%d %-5p [%t] %C{2} (%F:%L) - %m%n").build())
//                        .build();
//                errorFileAppender.start();
//                configuration.addAppender(errorFileAppender);
//                LoggerConfig rootLogger = configuration.getRootLogger();
//                rootLogger.setLevel(Level.ERROR);
//                rootLogger.addAppender(errorFileAppender, null, null);
//            }
//
//
//            {
//                RollingRandomAccessFileAppender appender = RollingRandomAccessFileAppender.newBuilder()
//                        .withFileName(ConfigService.getLogDir() + "debug.log")
//                        .withFilePattern(ConfigService.getLogDir() + "debug_%d{yyyyMMdd_HHmm}")
//                        .setName("debugFile")
//                        .withPolicy(TimeBasedTriggeringPolicy.newBuilder()
//                                .withInterval(30)
//                                .build())
//                        .setFilter(ThresholdFilter.createFilter(Level.DEBUG, Filter.Result.ACCEPT, Filter.Result.DENY))
//                        .setLayout(PatternLayout.newBuilder().withPattern("%d %-5p [%t] %C{2} (%F:%L) - %m%n").build())
//                        .build();
//                appender.start();
//                configuration.addAppender(appender);
//                LoggerConfig rootLogger = configuration.getRootLogger();
//                rootLogger.setLevel(Level.DEBUG);
//                rootLogger.addAppender(appender, null, null);
//            }
//
//            context.updateLoggers();
//        }

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
