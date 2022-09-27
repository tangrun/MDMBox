package com.tangrun.mdm.boxwindow.utils;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class DragUtil {

    public static void enableDialogDrag(Dialog<?> dialog){
        new DragHandler(dialog.getDialogPane()){

            @Override
            public void setXY(double x, double y) {
                dialog.setX(x);
                dialog.setY(y);
            }
        };
    }

    public static void enableWindowDrag(Stage stage){
        enableWindowDrag(stage,stage.getScene().getRoot());
    }

    public static void enableWindowDrag(Stage stage, Node node){
        new DragHandler(node) {
            @Override
            public void setXY(double x, double y) {
                stage.setX(x);
                stage.setY(y);
            }
        };
    }

    private static abstract class DragHandler implements EventHandler<MouseEvent> {

        private double xOffset = 0;
        private double yOffset = 0;

        public DragHandler(Node node) {
            node.setOnMousePressed(this);
            node.setOnMouseDragged(this);
        }

        @Override
        public void handle(MouseEvent event) {
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double x = event.getScreenX() - xOffset;
                double y = event.getScreenY() - yOffset < 0 ? 0 : event.getScreenY() - yOffset;
                setXY(x, y);
            }
        }

        public abstract void setXY(double x, double y);

    }

}
