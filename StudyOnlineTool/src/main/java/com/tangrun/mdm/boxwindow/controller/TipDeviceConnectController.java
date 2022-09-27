package com.tangrun.mdm.boxwindow.controller;

import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.core.BaseController;
import com.tangrun.mdm.boxwindow.utils.JavaFXUtil;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TipDeviceConnectController extends BaseController {
    @Override
    public void initialLazy() {

    }

    public void onUSBDebugCLick(MouseEvent mouseEvent) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle(BuildConfig.appName);
        Scene scene = getContext().getApplicationContext().startStage(stage).startScene("/fxml/tip_usb_debug.fxml");
        JavaFXUtil.enableWindowDrag(stage);
    }

    public void onCloseClick(MouseEvent mouseEvent) {
        getContext().popBack();
    }
}
