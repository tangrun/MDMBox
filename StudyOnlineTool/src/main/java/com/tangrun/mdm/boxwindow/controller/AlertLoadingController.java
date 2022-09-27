package com.tangrun.mdm.boxwindow.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertLoadingController implements Initializable {

    @FXML
    public HBox content;
    @FXML
    DialogPane dialogPane;

    @FXML
    Label tvContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


//        dialogPane.getChildren().clear();
//        dialogPane.getChildren().add(content);
        tvContent.textProperty().bind(dialogPane.contentTextProperty());



    }
}
