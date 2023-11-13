package com.tangrun.mdm.licensegenerator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HelloController implements Initializable {
    public Label tvTitle;
    public Label btHistory;
    public Label btExit;
    public TextField etName;
    public TextField etPackage;
    public TextField etComponent;
    public TextField etRemark;
    public TextArea etDeviceCode;
    public DateTimePicker etExpireTime;
    public Button btGenerate;
    public TextArea etLicense;
    public CheckBox cbDebug;
    Popup popup;
    ListView<Config> listView;
    ObservableList<Config> configList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tvTitle.setText(BuildConfig.appName + " " + BuildConfig.appVersionName);
        etExpireTime.setFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        etExpireTime.dateTimeProperty().set(LocalDateTime.now().plusYears(1));

        popup = new Popup();
        VBox vBox = new VBox();
        vBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        listView = new ListView<>();
        listView.setItems(configList);
        listView.setCellFactory(param -> new MyListCell());
        listView.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(listView, Priority.ALWAYS);
        vBox.getChildren().add(listView);


        {
            HBox hBox = new HBox();
            hBox.setSpacing(16);
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(16));

            Label labelDelete = new Label();
            labelDelete.setText("删除");
            labelDelete.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    configList.remove(listView.getSelectionModel().getSelectedItem());
                    saveHistory();
                }
            });
            hBox.getChildren().add(labelDelete);

            Label labelConfirm = new Label();
            labelConfirm.setText("确认");
            labelConfirm.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popup.hide();
                    Config config = listView.getSelectionModel().getSelectedItem();
                    etName.setText(config.getAppName());
                    etPackage.setText(config.getPkgName());
                    etRemark.setText(config.getDesc());
                    etComponent.setText(config.getClsName());
                }
            });
            hBox.getChildren().add(labelConfirm);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    boolean b = listView.getSelectionModel()
                            .getSelectedIndex() < 0;
                    labelConfirm.setDisable(b);
                    labelDelete.setDisable(b);
                }
            };

            Label labelCancel = new Label();
            labelCancel.setText("取消");
            labelCancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popup.hide();
                }
            });
            hBox.getChildren().add(labelCancel);

            runnable.run();
            listView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            runnable.run();
                        }
                    });

            vBox.getChildren().add(0, hBox);
        }

        vBox.setPrefWidth(320);
        vBox.setPrefHeight(640);
        popup.getContent().add(vBox);
        popup.setWidth(320);
        popup.setHeight(640);
        popup.setAutoHide(true);

        Data data = readDataFromFile();
        if (data.getHistory() != null) {
            configList.addAll(data.getHistory());
        }
    }

    public void onHistoryClick(MouseEvent mouseEvent) {
        if (popup.isShowing()) {
            popup.hide();
        } else {
            popup.show(btHistory,
                    +btHistory.localToScene(0, 0).getX()
                            + btHistory.getScene().getWindow().getX()
                            + btHistory.getWidth()
                            - popup.getWidth()
                    ,
                    +btHistory.localToScene(0, 0).getY()
                            + btHistory.getScene().getWindow().getY()
                            + btHistory.getHeight()
            );
        }
    }

    private void addHistory(Config config){
        String key = config.getAppName()+config.getPkgName();
        for (Config config1 : configList) {
            String key1 = config1.getAppName()+config1.getPkgName();
            if (key.equals(key1)){
                configList.remove(config1);
                break;
            }
        }
        configList.add(config);
        saveHistory();
    }

    private void saveHistory(){
        Data data = readDataFromFile();
        Map<String,Config> map = new HashMap<>();
        for (Config config : configList) {
            String key = config.getAppName() + "_" + config.getPkgName();
            if (map.containsKey(key))
                continue;
            map.put(key,config);
        }
        data.setHistory(map.values().stream().toList());
        saveDataToFile(data);
    }

    String historyFilePath = "./data.json";

    int dataReadVersion = -1 ,dataWriteVersion;
    Data dataCache = new Data();

    private Data readDataFromFile() {
        int temp = dataWriteVersion;
        if (dataReadVersion == dataWriteVersion){
            return dataCache;
        }
        File file = new File(historyFilePath);
        if (file.exists() && file.isFile()) {
            try (FileReader fileReader = new FileReader(file)) {
                dataCache = new Gson().fromJson(fileReader, Data.class);
                dataReadVersion = temp;
            } catch (IOException e) {
                System.err.println("读取持久化文件失败");
                e.printStackTrace();
            }
        }
        return dataCache;
    }

    private void saveDataToFile(Data data) {
        File file = new File(historyFilePath);
        if (!file.exists()) {
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    System.err.println("创建持久化文件父目录失败");
                    return;
                }
            }
            try {
                if (!file.createNewFile()) {
                    System.err.println("创建持久化文件失败");
                    return;
                }
            } catch (IOException e) {
                System.err.println("创建持久化文件出错");
                e.printStackTrace();
                return;
            }
        }
        try (FileWriter fileWriter = new FileWriter(file)){
            new Gson().toJson(data,fileWriter);
            dataWriteVersion++;
        } catch (IOException e) {
            System.err.println("持久化出错");
            e.printStackTrace();
        }
    }

    static class MyListCell extends ListCell<Config> {

        @Override
        protected void updateItem(Config entity, boolean empty) {
            super.updateItem(entity, empty);
            if (entity == null) {
                this.setText("");
            } else {
                this.setText(entity.getAppName() + "(" + entity.getPkgName() + ")");
            }
        }

    }

    public void onGenerateClick(MouseEvent mouseEvent) {
        if (etName.getText().isBlank()) {
            showTipDialog("应用名不能为空");
            return;
        }
        if (etPackage.getText().isBlank()) {
            showTipDialog("应用包名不能为空");
            return;
        }
        if (etComponent.getText().isBlank()) {
            showTipDialog("应用组件名不能为空");
            return;
        }
        if (etDeviceCode.getText().isBlank()) {
            showTipDialog("机器码不能为空");
            return;
        }

        Config config = new Config();
        config.setAppName(etName.getText().trim());
        config.setPkgName(etPackage.getText().trim());
        config.setDesc(etRemark.getText().trim());
        config.setMachineId(etDeviceCode.getText().trim());
        config.setClsName(etComponent.getText().trim());
        config.setExpireTime(etExpireTime.dateTimeProperty().get().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        config.setDebug(cbDebug.isSelected());

        String json = new Gson().toJson(config);
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String license = encode(base64);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < license.length(); i += 64) {
            stringBuilder.append(i == 0 ? "" : "\n")
                    .append(license, i, Math.min(i + 64, license.length()));
        }
        String formatLicense = stringBuilder.toString();

        etLicense.setText(formatLicense);

        addHistory(config);
    }

    public static String encode(String s) {
        // a-z 97-122
        // A-Z 65-90
        // 0-9 48-57
        char[] chars = s.toCharArray();
        char[] newChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            char origin = chars[i];
            if (origin <= 122 && origin >= 97) {
                int offset = origin - 97;
                origin = (char) (90 - offset);
            } else if (origin <= 90 && origin >= 65) {
                int offset = origin - 65;
                origin = (char) (122 - offset);
            } else if (origin <= 57 && origin >= 48) {
                origin = (char) (105 - origin);
            }
            newChars[i] = origin;
        }
        return new String(newChars);
    }


    private void showTipDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.NONE, msg, ButtonType.OK);
        alert.setTitle("提示");
        alert.showAndWait();
    }

    public void onExit(MouseEvent mouseEvent) {
        Platform.exit();
    }
}
