package com.tangrun.mdm.licensegenerator;

import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.*;

public class JavaFXUtil {

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


    public static String dump(Node n) {
        StringBuilder stringBuilder = new StringBuilder();
        dump(n, 0, stringBuilder);
        return stringBuilder.toString();
    }

    private static String stringJoiner(Collection<String> collection, String de) {
        StringJoiner stringJoiner = new StringJoiner(de);
        for (String s : collection) {
            stringJoiner.add(s);
        }
        return stringJoiner.toString();
    }

    private static String printNode(Node node, String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(prefix)
                .append(node.getClass().getName()).append("@").append(node.hashCode()).append("[");
        Map<String, String> map = new HashMap<>();

        if (!node.getStyleClass().isEmpty())
            map.put("styleClass", stringJoiner(node.getStyleClass(), " "));
        if (node.getId() != null)
            map.put("id", node.getId());
        if (node instanceof Labeled && ((Labeled) node).getText() != null)
            map.put("text", ((Labeled) node).getText());

        if (node instanceof Styleable) {
            List<CssMetaData<? extends Styleable, ?>> cssMetaData = ((Styleable) node).getCssMetaData();
            Map<String,Object> map1 = new TreeMap<>();
            for (CssMetaData<?, ?> cssMetaDatum : cssMetaData) {
                boolean error = false, set = false;
                for (Method method : cssMetaDatum.getClass().getMethods()) {
                    if (method.getName().equals("getStyleableProperty")) {
                        try {
                            method.setAccessible(true);
                            Object invoke = method.invoke(cssMetaDatum, node);
                            Object value = invoke;
                            if (invoke instanceof WritableValue) {
                                value = ((WritableValue<?>) invoke).getValue();
                            }
                            if (value != null)
                                map1.put(cssMetaDatum.getProperty(),value);
                            set = true;
                        } catch (Exception e) {
                            map1.put(cssMetaDatum.getProperty(),"error1");
                            error = true;
                        }
                        break;
                    }
                }
                if (!set && !error)
                    map1.put(cssMetaDatum.getProperty(),"error2");

            }
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, Object> entry : map1.entrySet()) {
                s.append("\n").append(prefix).append("\t\t").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            map.put("style", s.toString());
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append("\n").append(prefix).append("\t").append(entry.getKey()).append("=").append(entry.getValue());
        }

        stringBuilder.append("\n").append(prefix).append("]");
        return stringBuilder.toString();
    }

    private static void dump(Node n, int depth, StringBuilder stringBuilder) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < depth; i++) prefix.append("\t");
        String prefixString = prefix.toString();
        stringBuilder.append("\n").append(printNode(n, prefixString));
        if (n instanceof Parent)
            for (Node c : ((Parent) n).getChildrenUnmodifiable())
                dump(c, depth + 1, stringBuilder);
    }
}
