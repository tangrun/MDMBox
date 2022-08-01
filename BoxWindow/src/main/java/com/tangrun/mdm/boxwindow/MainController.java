package com.tangrun.mdm.boxwindow;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.IOUtils;
import com.google.common.base.Converter;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.*;
import com.google.common.primitives.Bytes;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import com.tangrun.mdm.boxwindow.pojo.AccountAppInfo;
import com.tangrun.mdm.shell.core.ShellApi;
import com.tangrun.mdm.shell.core.ShellApiExecResult;
import com.tangrun.mdm.shell.enums.PackageFilterParam;
import com.tangrun.mdm.shell.impl.ShellApiCmdImpl;
import com.tangrun.mdm.shell.impl.ShellExecutorCmdImpl;
import com.tangrun.mdm.shell.pojo.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class MainController implements Initializable {
    public Label tvDeviceInfo;
    public Button btRefreshDevice;
    public Button btCancel;
    public Button btStart;
    public Label tvConfigInfo;

    Config config;

    private ShellApi adbShell;

    Device connectedDevice;
    BufferedWriter logSteam;
    private final Object object = new Object();
    Thread thread;
    boolean inRefreshing = false;

    private void startRefreshDeviceTask() {
        synchronized (object) {
            if (inRefreshing) return;
            inRefreshing = true;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (inRefreshing) {
                        onRefreshDeviceClick();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void stopRefreshDeviceTask() {
        synchronized (object) {
            if (!inRefreshing) {
                return;
            }
            inRefreshing = false;
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // config
        {
            String content = null;
            URL url = getClass().getResource("config");
            if (url != null) {
                ByteSource byteSource = Resources.asByteSource(url);
                try {
                    content = byteSource.asCharSource(StandardCharsets.UTF_8).read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (content == null) {
                File configFile = new File("config");
                if (configFile.exists() && configFile.isFile()) {
                    try {
                        content = Files.asCharSource(configFile, StandardCharsets.UTF_8).read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (content == null) {
                Alert alert = new Alert(Alert.AlertType.NONE, "配置文件不存在", ButtonType.FINISH);
                setAlertDefaultStyleAndShowAwait(alert);
                Application.exitApp();
                return;
            }
            Config config = null;
            try {
                content = content.replaceAll("[\n|\t|\r]", "");
                String read = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                config = JSON.parseObject(read, Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (config == null) {
                Alert alert = new Alert(Alert.AlertType.NONE, "配置文件读取失败", ButtonType.FINISH);
                setAlertDefaultStyleAndShowAwait(alert);
                Application.exitApp();
                return;
            }

            this.config = config;
            {
                tvConfigInfo.setText(
                        config.getAppName() + "(" + config.getPkgName() + ")" +
                                "\n" + config.getDesc()
                );
            }
        }

        // log
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        File file = new File("./logs/" + simpleDateFormat.format(new Date()) + ".log");
        try {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    System.err.println("日志文件目录创建失败" + file.getAbsolutePath());
                    Application.exitApp();
                }
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.err.println("日志文件创建失败" + file.getAbsolutePath());
                    Application.exitApp();
                }
            }
            logSteam = new BufferedWriter(new FileWriter(file, true));

            adbShell = new ShellApiCmdImpl(logSteam, new ShellExecutorCmdImpl());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("日志文件创建失败" + file.getAbsolutePath());
            Application.exitApp();
        }

        // adb
        if (!isAdbUsable()) {
            Alert alert = new Alert(Alert.AlertType.NONE, "adb工具无法使用", ButtonType.FINISH);
            setAlertDefaultStyleAndShowAwait(alert);
            Application.exitApp();
        }

        // adb device listener
        startRefreshDeviceTask();
    }

    public void onCancelClick(ActionEvent actionEvent) {
        stopRefreshDeviceTask();
        onCancelClick();
        startRefreshDeviceTask();
    }

    private void onCancelClick() {
        ShellApiExecResult<ProfileOwner> result = adbShell.getProfileOwner();
        if (!result.success) {
            Alert alert = new Alert(Alert.AlertType.NONE, "查询设备已激活信息失败\n" + result.msg, ButtonType.OK);
            setAlertDefaultStyleAndShowAwait(alert);
            return;
        }
        ProfileOwner profileOwner = result.data;
        if (profileOwner == null) {
            Alert alert = new Alert(Alert.AlertType.NONE, "设备没有查到有激活信息，不需要激活：", ButtonType.OK);
            setAlertDefaultStyleAndShowAwait(alert);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.NONE, "是否取消激活？\n已激活APP信息：" + profileOwner.getPkgName(), ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = setAlertDefaultStyleAndShowAwait(alert);
        if (!buttonType.isPresent() || buttonType.get() != ButtonType.YES) {
            return;
        }
        ShellApiExecResult<Boolean> execResult = adbShell.removeActiveAdmin(profileOwner.getComponentName().packageName, profileOwner.getComponentName().className);
        if (execResult.success) {
            alert = new Alert(Alert.AlertType.NONE, "取消激活" + (execResult.data == Boolean.TRUE ? "成功" : "失败"), ButtonType.OK);
        } else {
            alert = new Alert(Alert.AlertType.NONE, "取消激活执行失败\n" + execResult.msg, ButtonType.OK);
        }
        setAlertDefaultStyleAndShowAwait(alert);
    }

    public void onStartClick(ActionEvent actionEvent) {
        stopRefreshDeviceTask();
        onStartClick();
        startRefreshDeviceTask();
    }

    private void onStartClick(){
        Alert alert = new Alert(Alert.AlertType.NONE, "是否开始激活？请确认激活APP信息:\n"+tvConfigInfo.getText(), ButtonType.OK);
        Optional<ButtonType> buttonType = setAlertDefaultStyleAndShowAwait(alert);
        if (!buttonType.isPresent() || buttonType.get() != ButtonType.OK){
            return;
        }
        ShellApiExecResult<Void> result = startRegistration_();
        alert = new Alert(Alert.AlertType.NONE, result.success?"激活成功": ("激活失败：\n"+result.msg), ButtonType.OK);
        setAlertDefaultStyleAndShowAwait(alert);
    }


    //region 激活过程 与Box一致

    protected String RESULT_fail_shell_error = "操作失败，请重新连接盒子进行激活";
    protected String RESULT_fail_multi_user = "激活失败，请关闭系统分身/应用双开等多开功能后再试";
    protected String RESULT_fail_xiaomi_no_manager_device_admins_permisiion = "激活失败，小米用户请手动在系统设置=>开发者设置=>开启“USB 调试（安全设置）”，如仍不可以请关闭“MIUI 优化”";
    protected String RESULT_fail_multi_account = "激活失败，还有账户存在，请手动移除账号";
    protected String RESULT_fail_has_other_app_set = "激活失败，已有其他软件被激活";


    private ShellApiExecResult<Void> startRegistration_() {
        {
            ShellApiExecResult<List<UserInfo>> userList = adbShell.getUserList();
            if (!userList.success) {
                return ShellApiExecResult.fail(RESULT_fail_shell_error);
            }
            if (userList.data.size() != 1) {
                StringBuilder stringBuilder = new StringBuilder();
                int i = 0;
                for (UserInfo datum : userList.data) {
                    stringBuilder.append(String.format("%s    %s[%s] %s",
                            i++ == 0 ? "" : "\n",
                            datum.name,
                            datum.id + "",
                            datum.isMain() ? "【系统】" : ""
                    ));
                }
                stringBuilder.append("\n发现存在多个用户，是否移除非主用户之外的用户？")
                        .append("\n如有系统分身/应用双开，请提前备份其数据！！！");
                if (!showSyncBlockConfirmDialog("多用户提示", stringBuilder.toString())) {
                    return ShellApiExecResult.fail(RESULT_fail_multi_user);
                }
                // 开始移除other用户
                for (UserInfo datum : userList.data) {
                    if (datum.isMain()) continue;
                    ShellApiExecResult<Void> result = adbShell.removeUser(datum.id);
                    console("关闭分身/双开：{} {}, {}", datum.id, datum.name, result.success);
                }
            }
        }
        // 软件安装列表
        ShellApiExecResult<List<String>> packageList = adbShell.getPackageList();
        if (!packageList.success || packageList.data.isEmpty()) {
            return ShellApiExecResult.fail(RESULT_fail_shell_error);
        }
        // key: pkgName
        Map<String, AccountAppInfo> disabledAppMap = new HashMap<>();
        // 循环多次 有的账号在上一次操作后才出现新的
        boolean hasError = false;
        StringBuilder errorReturn = new StringBuilder("失败记录：");
        for (int i = 0; i < 5; i++) {
            ShellApiExecResult<Map<String, AccountAppInfo>> currentHasAccountAppMap = getMainUserAccountApps(packageList.data);
            if (currentHasAccountAppMap.success) {
                for (Map.Entry<String, AccountAppInfo> entry : currentHasAccountAppMap.data.entrySet()) {
                    if (!entry.getValue().hasPackage) {
                        errorReturn.append(parse("\n有未知账号APP存在，请手动操作 ", entry.getKey(), entry.getValue()));
                        hasError = true;
                    }
                }
            } else {
                errorReturn.append("\n").append(currentHasAccountAppMap.msg);
                hasError = true;
            }
            if (hasError) break;
            // 有的账号移除后再次查询还在 就不用再禁用了
            Iterator<String> iterator = currentHasAccountAppMap.data.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (disabledAppMap.containsKey(next)) {
                    iterator.remove();
                }
            }
            // 没有了就直接激活
            if (currentHasAccountAppMap.data.isEmpty()) {
                break;
            }
            disabledAppMap.putAll(currentHasAccountAppMap.data);
            for (String s : currentHasAccountAppMap.data.keySet()) {
                ShellApiExecResult<Void> result = adbShell.setEnabled(s, false);
                console("软件：{}，{}", s, result.success);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 激活
        ShellApiExecResult<Void> setOwnerResult = adbShell.setProfileOwner(config.getComponent());
        boolean success = setOwnerResult.success;
        if (!setOwnerResult.success) {
            if (setOwnerResult.msg.contains("but profile owner is already set")) {
                ShellApiExecResult<ProfileOwner> profileOwner = adbShell.getProfileOwner();
                errorReturn.append("\n").append(RESULT_fail_has_other_app_set);
                if (profileOwner.success) {
                    errorReturn.append(profileOwner.data.toString());
                }
            } else if (setOwnerResult.msg.contains("there are already some accounts")) {
                ShellApiExecResult<Map<String, AccountAppInfo>> userAccountApps = getMainUserAccountApps(packageList.data);
                errorReturn.append("\n").append(RESULT_fail_multi_account);
                if (userAccountApps.success) {
                    for (String s : userAccountApps.data.keySet()) {
                        errorReturn.append("\n    ").append(s);// 包名
                    }
                }
            } else if (setOwnerResult.msg.contains("users on the")) {
                errorReturn.append("\n").append(RESULT_fail_multi_user);
                ShellApiExecResult<List<UserInfo>> userList = adbShell.getUserList();
                if (userList.success) {
                    for (UserInfo userInfo : userList.data) {
                        errorReturn.append("\n    ").append(userInfo);// user
                    }
                }
            } else if (setOwnerResult.msg.contains("android.permission.MANAGE_DEVICE_ADMINS")) {
                errorReturn.append("\n").append(RESULT_fail_xiaomi_no_manager_device_admins_permisiion);
            } else {
                errorReturn.append("激活失败：").append(setOwnerResult.msg);
            }
        } else {
            if (setOwnerResult.msg != null && setOwnerResult.msg.contains("Error: Unknown admin: ComponentInfo")) {
                success = false;
                errorReturn.append("\n激活失败，请先安装应用");
            }
        }
        // 恢复
        for (String datum : disabledAppMap.keySet()) {
            console("----：{}", datum);
        }
        ShellApiExecResult<List<String>> disabledPackageList = adbShell.getPackageList(PackageFilterParam.disabled);
        if (disabledPackageList.success) {
            for (String datum : disabledPackageList.data) {
                ShellApiExecResult<Void> result1 = adbShell.setEnabled(datum, true);
                console("====：{}，{}", datum, result1.success);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            errorReturn.append("\n恢复应用设置失败");
            console("恢复应用设置失败");
        }
        if (success) return ShellApiExecResult.success(null);
        else return ShellApiExecResult.fail(errorReturn.toString());
    }

    private ShellApiExecResult<Map<String, AccountAppInfo>> getMainUserAccountApps(List<String> packageList) {
        Map<String, AccountAppInfo> accountAppMap = new HashMap<>();
        ShellApiExecResult<List<UserAccounts>> userAccounts = adbShell.getUserAccounts();
        if (!userAccounts.success) {
            return ShellApiExecResult.fail(RESULT_fail_shell_error);
        }
        UserAccounts accounts = null;
        for (UserAccounts datum : userAccounts.data) {
            if (datum.userInfo.isMain()) {
                accounts = datum;
                break;
            }
        }
        if (accounts == null) {
            return ShellApiExecResult.fail("主用户不存在");
        }

        // 先以register server为准
        for (ServiceInfo serviceInfo : accounts.serviceInfoList) {
            AccountAppInfo accountAppInfo = new AccountAppInfo();
            accountAppInfo.packageName = serviceInfo.componentName.packageName;
            accountAppInfo.className = serviceInfo.componentName.className;
            accountAppInfo.uid = serviceInfo.uid;
            accountAppInfo.type = serviceInfo.type;
            accountAppInfo.hasService = true;
            accountAppInfo.hasPackage = true;
            accountAppMap.put(accountAppInfo.packageName, accountAppInfo);
        }
        Map<String, AccountAppInfo> tempAppMap = new HashMap<>();
        for (Account account : accounts.accountList) {
            AccountAppInfo accountAppInfo = null;
            for (AccountAppInfo value : accountAppMap.values()) {
                if (value.type.equals(account.type)) {
                    accountAppInfo = value;
                    accountAppInfo.hasAccount = true;
                    break;
                }
            }
            if (accountAppInfo != null) {
                accountAppInfo.type = account.type;
                accountAppInfo.name = account.name;
                continue;
            }
            // 无register server
            accountAppInfo = new AccountAppInfo();
            accountAppInfo.hasAccount = true;
            accountAppInfo.hasService = false;
            accountAppInfo.type = account.type;
            accountAppInfo.name = account.name;
            if (packageList.contains(account.type)) {
                // type就是包名
                accountAppInfo.packageName = account.type;
                accountAppInfo.hasPackage = true;
            } else {
                // 模糊查找
                Map<String, List<String>> similarPkg = getSimilarPkg(account.type, packageList);
                int max = -1;
                int repeat = 0;
                String maxPkgName = null;
                for (Map.Entry<String, List<String>> entry1 : similarPkg.entrySet()) {
                    int size = entry1.getValue().size();
                    if (size == 0) continue;
                    if (size > max) {
                        max = size;
                        maxPkgName = entry1.getKey();
                        repeat = 0;
                    } else if (size == max) {
                        // 有多个相似
                        repeat++;
                    }
                }
                if (max > 0 && repeat == 0) {
                    // 模糊匹配有结果
                    accountAppInfo.hasPackage = true;
                    accountAppInfo.packageName = maxPkgName;
                }
            }
            if (accountAppInfo.hasPackage) {
                tempAppMap.put(accountAppInfo.packageName, accountAppInfo);
            }
        }
        accountAppMap.putAll(tempAppMap);
        return ShellApiExecResult.success(accountAppMap);
    }

    private Map<String, List<String>> getSimilarPkg(String type, List<String> pkgList) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> typeOfEffectiveKey = getPkgEffectiveKey(type);
        for (String s : pkgList) {
            List<String> pkgEffectiveKey = getPkgEffectiveKey(s);
            List<String> matchResult = getEqualString(typeOfEffectiveKey, pkgEffectiveKey);
            if (matchResult.isEmpty()) continue;
            result.put(s, matchResult);
        }
        return result;
    }

    private List<String> getEqualString(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();
        for (String s : list1) {
            if (list2.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }


    private List<String> getPkgEffectiveKey(String packageName) {
        Pattern pattern = Pattern.compile("^(?:com|org|net|android)");
        String[] split = packageName.split("\\.");
        List<String> list = new ArrayList<>();
        if (split != null) {
            for (String s : split) {
                if (s == null) continue;
                String trim = s.trim();
                if (trim.equals("") || pattern.matcher(trim).find()) continue;
                list.add(trim);
            }
        }
        return list;
    }

    private boolean showSyncBlockConfirmDialog(String title,String msg){
        Alert alert = new Alert(Alert.AlertType.NONE, msg, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        Optional<ButtonType> buttonType = setAlertDefaultStyleAndShowAwait(alert);
        return buttonType.isPresent() && buttonType.get() == ButtonType.YES;
    }

    public static String parse(String text, Object... args) {
        return parse("{", "}", text, args);
    }

    public static String parse(String openToken, String closeToken, String text, Object... args) {
        if (args == null || args.length <= 0) {
            return text;
        }
        int argsIndex = 0;

        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    ///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////

                    String value = (argsIndex <= args.length - 1) ?
                            (args[argsIndex] == null ? "" : args[argsIndex].toString()) : expression.toString();
                    builder.append(value);
                    offset = end + closeToken.length();
                    argsIndex++;
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }

    private void console(String line, Object... obj) {
        String parse = parse(line, obj);
        System.out.println(parse);
        if (logSteam != null) {
            try {
                logSteam.write(String.format("Box [%s]: %s\n",
                        DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())),
                        parse));
                logSteam.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //endregion



    private void onRefreshDeviceClick() {
        System.out.println("----------------------");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                connectedDevice = null;
                ShellApiExecResult<List<Device>> result = adbShell.getDeviceList();
                if (result.success) {
                    if (result.data == null || result.data.isEmpty()) {
                        tvDeviceInfo.setText(
                                "无设备连接，若有问题可尝试以下方法：" +
                                        "\n检查手机是否已打开USB调试模式" +
                                        "\n重新插拔USB连接线"
                        );
                    } else if (result.data.size() > 1) {
                        tvDeviceInfo.setText("存在多个设备连接，请保持只有一台设备");
                    } else {
                        connectedDevice = result.data.get(0);
                        tvDeviceInfo.setText(connectedDevice.id + "(" + connectedDevice.state.desc + ")");
                    }
                } else {
                    tvDeviceInfo.setText("获取设备失败");
                    Alert alert = new Alert(Alert.AlertType.NONE, result.msg);
                    setAlertDefaultStyleAndShowAwait(alert);
                }
                refreshDeviceState();
            }
        });
    }

    public void onRefreshDeviceClick(ActionEvent actionEvent) {
        stopRefreshDeviceTask();
        startRefreshDeviceTask();
    }

    private void refreshDeviceState() {
        if (connectedDevice != null && connectedDevice.state == Device.DeviceState.device) {
            btStart.setDisable(false);
            btCancel.setDisable(false);
        } else {
            btStart.setDisable(true);
            btCancel.setDisable(true);
        }
    }

    private Optional<ButtonType> setAlertDefaultStyleAndShowAwait(Alert alert) {
        switch (alert.getAlertType()) {
            case NONE:
                alert.setTitle("提示");
                break;
            case ERROR:
                alert.setTitle("错误");
                break;
            case INFORMATION:
                alert.setTitle("信息");
                break;
            case WARNING:
                alert.setTitle("警告");
                break;
            case CONFIRMATION:
                alert.setTitle("确定");
                break;
        }
        DialogPane dialogPane = alert.getDialogPane();
        String contentText = dialogPane.getContentText();

        TextArea textArea = new TextArea(contentText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        String style = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-label-padding: 10 8; ";
        textArea.setStyle(style);
        textArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                textArea.setStyle(style);
            }
        });

        dialogPane.setContent(textArea);
        dialogPane.setStyle("-fx-background-color: #fff; -fx-min-width: 320; -fx-max-width: 540; -fx-pref-width: 320; -fx-min-height: 160;-fx-pref-height: 160; -fx-max-height: 720");

        alert.initOwner(Application.getMainStage());

        return alert.showAndWait();
    }

    private boolean isAdbUsable() {
        ShellApiExecResult<List<Device>> deviceList = adbShell.getDeviceList();
        return !(!deviceList.success && deviceList.msg.contains("adb"));
        //Cannot run program "adb" (in directory "."): CreateProcess error=2, 系统找不到指定的文件。
    }


    public void onMouseDragOverBtn(MouseDragEvent mouseDragEvent) {

    }
}
