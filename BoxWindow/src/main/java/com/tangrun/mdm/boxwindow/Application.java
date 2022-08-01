package com.tangrun.mdm.boxwindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Stack;

public class Application extends javafx.application.Application {

    private static Stage stage;
    private static final Stack<Scene> sceneStack = new Stack<>();

    public static void exitApp(){
        exitApp(0);
    }

    public static void exitApp(int status){
        System.exit(status);
    }

    public static void startScene(String title, Scene scene) {
        scene.getProperties().put("title", title);
        sceneStack.push(scene);
        setScene(scene);
    }

    public static void startScene(String title, String resourceURI) {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource(resourceURI));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (scene != null)
            startScene(title, scene);
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

    public static Stage getMainStage(){
        return stage;
    }

    @Override
    public void start(Stage stage) throws IOException {
//        stage.setAlwaysOnTop(true);
        stage.setWidth(320);
        stage.setHeight(540);
        stage.setResizable(false);
        Application.stage = stage;

        startScene("激活工具", "main-view.fxml");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
