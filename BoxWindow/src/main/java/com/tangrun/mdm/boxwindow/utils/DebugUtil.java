package com.tangrun.mdm.boxwindow.utils;

import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class DebugUtil {

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
