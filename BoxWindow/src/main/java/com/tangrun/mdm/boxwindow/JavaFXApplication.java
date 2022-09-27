package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.core.InitializeLazy;
import com.tangrun.mdm.boxwindow.impl.AppUseTimeSetter;
import com.tangrun.mdm.boxwindow.utils.DragUtil;
import com.tangrun.mdm.boxwindow.utils.IOThread;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Stack;

@Slf4j
public class JavaFXApplication extends javafx.application.Application {
    private static Stage stage;
    private static final Stack<Scene> sceneStack = new Stack<>();

    public static void exitApp() {
        if (stage.isShowing()) {
            stage.close();
        } else
            stage.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    stage.close();
                }
            });
    }

    public static void startScene(String title, Scene scene) {
        scene.getProperties().put("title", title);
        sceneStack.push(scene);
        setScene(scene);
    }

    public static <T> T loadFXML(String resourceURI) {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaFXApplication.class.getResource(resourceURI));
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void startScene(String title, String resourceURI) {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaFXApplication.class.getResource(resourceURI));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (scene != null) {
            startScene(title, scene);
            DragUtil.enableWindowDrag(stage);
            if (fxmlLoader.getController() != null && fxmlLoader.getController() instanceof InitializeLazy) {
                ((InitializeLazy) fxmlLoader.getController()).initializeLazy();
            }
        }
    }

    public static void popBack() {
        sceneStack.pop();
        Scene scene = sceneStack.peek();
        if (scene == null) {
            exitApp();
            return;
        }
        setScene(scene);
    }

    private static void setScene(Scene scene) {
        stage.setTitle((String) scene.getProperties().getOrDefault("title", "无标题"));
        stage.setScene(scene);
    }

    public static Stage getMainStage() {
        return stage;
    }

    @Override
    public void start(Stage stage) throws IOException {
//        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        JavaFXApplication.stage = stage;

        startScene("激活工具", "/fxml/main-view.fxml");

        stage.show();
    }

    @Override
    public void init() throws Exception {
        log.debug("javafx application init");
        AppUseTimeSetter.sInstance.run();
    }

    @Override
    public void stop() throws Exception {
        log.debug("javafx application stop");
        AppUseTimeSetter.sInstance.run();
        IOThread.shutdown();
        SpringBootApplication.getConfigurableApplicationContext().close();
    }

    public static void main(String[] args) throws InterruptedException {
        Launcher.main(args);
    }
}
