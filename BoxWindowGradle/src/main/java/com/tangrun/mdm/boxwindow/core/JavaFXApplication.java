package com.tangrun.mdm.boxwindow.core;

import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.Launcher;
import com.tangrun.mdm.boxwindow.service.ConfigService;
import com.tangrun.mdm.boxwindow.service.DBService;
import com.tangrun.mdm.boxwindow.service.IOService;
import com.tangrun.mdm.boxwindow.utils.JavaFXUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class JavaFXApplication extends javafx.application.Application {

    ApplicationContext applicationContext = new ApplicationContext();

    public JavaFXApplication() {
        long time = System.currentTimeMillis();
        applicationContext.getLifecycle().addEvent(IOService.getInstance());
        log.debug("io server time {}",System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        applicationContext.getLifecycle().addEvent(DBService.getInstance());
        log.debug("db server time {}",System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        applicationContext.getLifecycle().addEvent(ConfigService.getInstance());
        log.debug("config server time {}",System.currentTimeMillis() - time);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle(BuildConfig.appName);

        Scene scene = applicationContext.startStage(stage).startScene("/fxml/main.fxml");
        JavaFXUtil.enableWindowDrag(stage);
        scene.getStylesheets().add(getClass().getResource("/css/page_main.css").toExternalForm());
    }

    @Override
    public void init() throws Exception {
        log.debug("javafx application init");
        applicationContext.onEvent(LifecycleState.OnInit);
        applicationContext.onEvent(LifecycleState.OnReady);
    }

    @Override
    public void stop() throws Exception {
        log.debug("javafx application stopping");
        applicationContext.onEvent(LifecycleState.OnRelease);
        applicationContext.onEvent(LifecycleState.OnClosed);

    }

    public static void main(String[] args) throws InterruptedException {
        Launcher.main(args);
    }
}
