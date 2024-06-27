package com.tangrun.mdm.boxwindow.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Rain
 * @date 2024/6/27 11:58
 */
public class Dialogs {
    public static Stage CreateStage(String title, String contentText, Button... buttons){
        TextArea textArea = new TextArea(contentText);
        HBox buttonBar = new HBox(
                buttons
        );
        VBox root =
                new VBox(
                        textArea,
                        buttonBar
                );
        {
            root.setSpacing(16);

            buttonBar.setSpacing(16);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);

            root.setPadding(new Insets(16));
        }
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        return stage;
    }
}
