package com.tangrun.mdm.boxwindow.controller;

import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.core.BaseController;
import com.tangrun.mdm.boxwindow.core.LifecycleComposeEvent;
import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
import com.tangrun.mdm.boxwindow.dao.AppConfigService;
import com.tangrun.mdm.boxwindow.dao.DeviceLogService;
import com.tangrun.mdm.boxwindow.service.ConfigService;
import com.tangrun.mdm.boxwindow.service.DBService;
import com.tangrun.mdm.boxwindow.shell.core.ShellExecuteLogger;
import com.tangrun.mdm.boxwindow.dao.entity.DeviceLogEntity;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.pojo.DeviceWrapper;
import com.tangrun.mdm.boxwindow.shell.ShellApiImpl;
import com.tangrun.mdm.boxwindow.shell.ShellExecutorImpl;
import com.tangrun.mdm.boxwindow.shell.core.ShellApi;
import com.tangrun.mdm.boxwindow.shell.core.ShellApiExecResult;
import com.tangrun.mdm.boxwindow.shell.pojo.*;
import com.tangrun.mdm.boxwindow.service.IOService;
import com.tangrun.mdm.boxwindow.utils.JavaFXUtil;
import com.tangrun.mdm.boxwindow.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class MainController extends BaseController {
    /**
     * 激活失败重试次数
     */
    private static final int config_reset_profile_owner_count = 3;
    /**
     * 关闭账号循环检测次数
     */
    private static final int config_hide_account_package = 5;
    private static final long config_refresh_device_interval_time = 1000;

    private static boolean check_vivo_device_owner = true;

    @FXML
    public Label tvDeviceInfo;
    @FXML
    public Label tvTitle;
    @FXML
    public ImageView btStart;
    @FXML
    public VBox llDeviceEmpty;
    @FXML
    public AnchorPane llDeviceContainer;

    private Config config;

    private ShellApi adbShell;

    /**
     * 当前连接设备
     */
    private DeviceWrapper connectedDevice;

    @Override
    public void initialLazy() {

        log.debug("main controller initialLazy");

//        tvTitle.setText(BuildConfig.appName + " " + BuildConfig.appVersion);
        tvTitle.setText(BuildConfig.appName);

        showLoadingDialog("初始化中...");

        LifecycleComposeEvent composeEvent = new LifecycleComposeEvent();
        composeEvent.addSource(getContext().getLifecycle());
        composeEvent.addSource(getContext().getApplicationContext().getLifecycle(), LifecycleState.OnRelease, LifecycleState.OnClosed);
        composeEvent.getLifecycle()
                .addEvent(new LifecycleEventListener() {
                    @Override
                    public void onEvent(LifecycleState state) {
                        log.debug("compose event {}", state);
                        if (state == LifecycleState.OnInit) {

                            IOService.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
//                                    try {
//                                        Socket socket = new Socket("127.0.0.1", 5037);
//                                        log.debug("============ {} {}",socket.isConnected(),socket.isClosed());

//                                        showTipDialog("端口5037已被占用，请关闭其他手机助手类应用后再试");
//                                        getContext().getApplicationContext()
//                                                .exitApp();
//                                    }catch (Exception e){
//                                        log.error("adb port other using.",e);
//                                    }

                                    if (!DBService.getInstance().initTable()) {
                                        showTipDialog("数据初始化失败或已在运行中");
                                        getContext().getApplicationContext().exitApp();
                                        return;
                                    }

                                    ConfigWrapper configWrapper = ConfigService.getInstance().getConfig();
                                    if (configWrapper.getConfig() == null) {
                                        showTipDialog(configWrapper.getMsg());
                                        getContext().getApplicationContext()
                                                .exitApp();
                                        return;
                                    }


                                    AppConfigService.setAppUseTime();

                                    config = configWrapper.getConfig();

                                    ShellApiImpl shellApiCmd = new ShellApiImpl(new ShellExecutorImpl());
                                    shellApiCmd.setInterceptor(new ShellExecuteLogger());
                                    adbShell = shellApiCmd;

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoadingDialog();

                                            // adb
                                            if (!isAdbUsable()) {
                                                showTipDialog("adb工具无法使用");
                                                getContext().getApplicationContext().exitApp();
                                                return;
                                            }

                                            // adb device listener
                                            startRefreshThread();
                                            startRefreshDeviceTask();
                                        }
                                    });

                                }
                            });


                        } else if (state == LifecycleState.OnReady) {


                        } else if (state == LifecycleState.OnRelease) {
                            log.debug("main controller stopping");
                            stopRefreshDeviceTask();
                            stopRefreshThread();
                            log.debug("main controller stopped");
                        }
                    }
                });
    }


    public String request(String requestUrl, String method) throws Exception {
        //创建Url类
        URL url = new URL(requestUrl);
        //创建HttpURLConnection类，由这个类发起Http请求
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        //设置请求参数
        httpURLConnection.setRequestMethod(method);
        httpURLConnection.setDoOutput(true);
        //发起请求建立链接
        InputStream inputStream = httpURLConnection.getInputStream();
        //读取response的返回值
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(bytes)) >= 0) {
            bout.write(bytes, 0, len);
        }
        inputStream.close();
        bout.close();
        return bout.toString(StandardCharsets.UTF_8);
    }

    //region 激活过程 与Box一致

    private void onStartClick() {
        if (!showConfirmDialog("是否开始激活？")) {
            return;
        }
        showLoadingDialog("激活中，预计时间1-2分钟...\n\n激活过程中请勿断开手机连接！！！");
        ResultWrapper resultWrapper = startRegistration_beforeCheck();
        if (resultWrapper.resultSuccess) {
            log.debug("激活前检测结果正常");
            resultWrapper = new ResultWrapper();
            startRegistration_setProfileOwner(0, resultWrapper);
            if (resultWrapper.resultSuccess) {
                log.debug("激活成功！！！");
            } else {
                log.debug("激活结果 {}", resultWrapper.resultMsg);
            }
            startRegistration_recoveryHideApp();
        } else {
            log.debug("激活前检测结果 {}", resultWrapper.resultMsg);
        }
        showTipDialog(resultWrapper.resultMsg);
        hideLoadingDialog();
    }

    private void onRecoveryAllHideApp() {
        if (!showConfirmDialog("确定开始检查消失的APP并恢复")) {
            return;
        }

        showLoadingDialog("恢复中...");

        startRegistration_recoveryHideApp();

        hideLoadingDialog();
        showTipDialog("操作完成！");
    }


    protected String RESULT_fail_uninstall_app = "请先安装APP后再进行激活";
    protected String RESULT_fail_shell_error = "操作失败，请检查设备连接再进行激活";
    protected String RESULT_fail_multi_user = "激活失败，请关闭系统分身/应用双开等多开功能后再试";
    protected String RESULT_fail_xiaomi_no_manager_device_admins_permisiion = "激活失败，小米用户请手动在系统设置=>开发者设置=>开启“USB 调试（安全设置）”，如仍不可以请关闭“MIUI 优化”";
    protected String RESULT_fail_multi_account = "激活失败，请退出系统登录账号后再试";
    protected String RESULT_fail_has_other_app_set = "激活失败，已有其他软件被激活";

    private ResultWrapper startRegistration_beforeCheck() {
        log.debug("开始激活");

        ResultWrapper resultWrapper = new ResultWrapper();

        {
            log.info("检查是否已激活");

            ShellApiExecResult<ProfileOwner> result;
            if (config_isSetDeviceOwner()) {
                result = adbShell.getDeviceOwner();
            } else {
                result = adbShell.getProfileOwner();
            }
            if (!result.success) {
                log.error("获取owner信息失败. {}", result.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (result.data != null) {
                if (Objects.equals(result.data.getPkgName(), config.getPkgName())) {
                    resultWrapper.resultMsg = "设备已经激活过了，不需要再次激活";
                } else
                    resultWrapper.resultMsg = "已经有激活的应用了\n" + result.data.getPkgName();
                return resultWrapper;
            }
        }

        {
            log.debug("检查是否安装APP");
            ShellApiExecResult<List<String>> result = adbShell.getPackageList(PackageFilterParam.user);
            if (!result.success) {
                log.error("获取安装应用列表失败，{}", result.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (!result.data.contains(config.getPkgName())) {
                resultWrapper.resultMsg = RESULT_fail_uninstall_app;
                return resultWrapper;
            }
        }

        // 多用户检测
        {
            log.info("检查多用户");

            ShellApiExecResult<List<UserInfo>> userList = adbShell.getUserList();
            if (!userList.success) {
                log.error("获取用户列表. {}", userList.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (userList.data.size() != 1) {
                StringBuilder stringBuilder = new StringBuilder("多用户提示");
                for (UserInfo datum : userList.data) {
                    stringBuilder.append(String.format("\n\t%s[%s] %s",
                            datum.name,
                            datum.id + "",
                            datum.isMain() ? "【系统】" : ""
                    ));
                }
                log.debug("多用户信息 {}", stringBuilder.toString());
                stringBuilder
                        .append("\n发现存在多个用户，是否移除非主用户之外的用户？(直接关闭系统分身/应用双开功能)")
                        .append("\n如有系统分身/应用双开，请提前备份其数据！！！");
                if (!showConfirmDialog(stringBuilder.toString())) {
                    resultWrapper.resultMsg = "操作已中止";
                    return resultWrapper;
                }
                // 开始移除other用户
                for (UserInfo datum : userList.data) {
                    if (datum.isMain()) continue;
                    ShellApiExecResult<Void> result = adbShell.removeUser(datum.id);
                    log.debug("移除用户结果, id: {} name: {}, success: {}", datum.id, datum.name, result.success);
                    if (!result.success) {
                        log.error("移除用户失败. {}", result.msg);
                        resultWrapper.resultMsg = "移除用户失败\n" + result.msg;
                        return resultWrapper;
                    }
                }
            }
        }

        resultWrapper.resultSuccess = true;
        return resultWrapper;
    }

    private void startRegistration_setProfileOwner(int count, ResultWrapper resultWrapper) {
        if (count > config_reset_profile_owner_count) {
            log.error("重新设置owner超时，共{}次", count);
            StringJoiner stringJoiner = new StringJoiner("\n", "\n", "");
            for (String s : resultWrapper.hideErrorList) {
                stringJoiner.add(s);
            }
            resultWrapper.resultMsg = RESULT_fail_multi_account  + stringJoiner;
            return;
        }
        log.error("准备设置owner {}", count);
        // hide有账号的应用
        {
            log.info("开始检测账号应用");

            for (int i = 0; i < config_hide_account_package; i++) {
                ShellApiExecResult<List<UserAccounts>> userAccounts = adbShell.getUserAccounts();
                if (!userAccounts.success) {
                    log.error("获取账号列表信息失败. {}", userAccounts.msg);
                    resultWrapper.resultMsg = RESULT_fail_shell_error;
                    return;
                }
                for (UserAccounts accounts : userAccounts.data) {
                    if (!accounts.userInfo.isMain()) {
                        continue;
                    }
                    if (accounts.serviceInfoList.isEmpty() && accounts.accountList.isEmpty()) {
                        log.debug("账号列表为空，提前退出检测");
                        i = config_hide_account_package;
                        break;
                    }

                    log.debug("serviceInfoList: {}",accounts.serviceInfoList);
                    log.debug("accountList: {}",accounts.accountList);

                    for (ServiceInfo serviceInfo : accounts.serviceInfoList) {
                        DeviceLogEntity deviceLogEntity = DeviceLogService.getDeviceLog(connectedDevice.getDeviceId(), serviceInfo.componentName.packageName);
                        if (deviceLogEntity == null)
                            deviceLogEntity = new DeviceLogEntity();
                        deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
                        deviceLogEntity.setContent(serviceInfo.componentName.packageName);
                        deviceLogEntity.setContentType(0);
                        deviceLogEntity.setState(1);
                        DeviceLogService.save(deviceLogEntity);

                        log.info("禁用APP {}", serviceInfo.componentName.packageName);
                        ShellApiExecResult<Void> result =
                                config_isUseHideAppMode() ?
                                        adbShell.setAppHide(serviceInfo.componentName.packageName, false) :
                                        adbShell.setEnabled(serviceInfo.componentName.packageName, false);
                        if (!result.success) {
                            resultWrapper.hideErrorList.add(serviceInfo.componentName.packageName);
                            log.warn("禁用APP {} 失败. {}", serviceInfo.componentName.packageName, result.msg);
//                            if (!showConfirmDialog("账号应用 "+serviceInfo.componentName.packageName+ " 处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
//                                return;
//                            }
                        } else {
                            resultWrapper.hideErrorList.remove(serviceInfo.componentName.packageName);
                        }
                    }

                    for (Account account : accounts.accountList) {
                        DeviceLogEntity deviceLogEntity = DeviceLogService.getDeviceLog(connectedDevice.getDeviceId(), account.type);
                        if (deviceLogEntity == null)
                            deviceLogEntity = new DeviceLogEntity();
                        deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
                        deviceLogEntity.setContent(account.type);
                        deviceLogEntity.setContentType(1);
                        deviceLogEntity.setState(1);
                        DeviceLogService.save(deviceLogEntity);

                        log.debug("禁用APP type {}", account.type);
                        ShellApiExecResult<Void> result =
                                config_isUseHideAppMode() ?
                                        adbShell.setAppHide(account.type, false) :
                                        adbShell.setEnabled(account.type, false);
                        if (!result.success) {
                            log.warn("禁用APP type {} 失败. {}", account.type, result.msg);
                            resultWrapper.hideErrorList.add(account.type);
//                            if (!showConfirmDialog("账号应用组件 "+account.type+" 处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
//                                return;
//                            }
                        } else {
                            resultWrapper.hideErrorList.remove(account.type);
                        }
                    }
                }
            }
        }

        // 激活
        {
            log.info("开始设置owner");


            ShellApiExecResult<Void> result;
            if (config_isSetDeviceOwner()) {
                log.debug("检测到是VIVO手机");
                result = adbShell.setDeviceOwner(config.getComponent());
            } else {
                result = adbShell.setProfileOwner(config.getComponent());
            }


            resultWrapper.resultSuccess = false;
            if (result.msg != null) {
                log.debug("设置owner结果 {}", result.msg);
                if (result.msg.contains("is already set")) {
                    resultWrapper.resultMsg = RESULT_fail_has_other_app_set;
                } else if (result.msg.contains("already some accounts")) {
                    startRegistration_setProfileOwner(count + 1, resultWrapper);
                } else if (result.msg.contains("users on the")) {
                    resultWrapper.resultMsg = RESULT_fail_multi_user;
                } else if (result.msg.contains("android.permission.MANAGE_DEVICE_ADMINS")) {
                    resultWrapper.resultMsg = RESULT_fail_xiaomi_no_manager_device_admins_permisiion;
                } else if (result.msg.contains("Unknown admin")) {
                    resultWrapper.resultMsg = "激活失败，请先安装应用";
                } else if (result.msg.contains("KNOX_PROXY_ADMIN_INTERNAL")) {
                    //激活失败
                    resultWrapper.resultMsg = "激活失败，当前设备不支持";
                    //java.lang.SecurityException: Neither user 2000 nor current process has com.samsung.android.knox.permission.KNOX_PROXY_ADMIN_INTERNAL.,com.sec.enterprise.permission.MDM_PROXY_ADMIN_INTERNAL
                    //	at android.os.Parcel.readException(Parcel.java:1693)
                    //	at android.os.Parcel.readException(Parcel.java:1646)
                    //	at android.app.admin.IDevicePolicyManager$Stub$Proxy.setActiveAdmin(IDevicePolicyManager.java:5754)
                    //	at com.android.commands.dpm.Dpm.runSetProfileOwner(Dpm.java:173)
                    //	at com.android.commands.dpm.Dpm.onRun(Dpm.java:99)
                    //	at com.android.internal.os.BaseCommand.run(BaseCommand.java:51)
                    //	at com.android.commands.dpm.Dpm.main(Dpm.java:41)
                    //	at com.android.internal.os.RuntimeInit.nativeFinishInit(Native Method)
                    //	at com.android.internal.os.RuntimeInit.main(RuntimeInit.java:316)
                }
            }

            if (resultWrapper.resultMsg == null) {
                if (result.success) {
                    resultWrapper.resultSuccess = true;
                    resultWrapper.resultMsg = "激活成功!";
                } else {
                    resultWrapper.resultMsg = "激活失败\n" + result.msg;
                }
            }
        }
    }

    private ResultWrapper startRegistration_recoveryHideApp() {
        log.debug("开始恢复APP");
        ResultWrapper resultWrapper = new ResultWrapper();

        List<DeviceLogEntity> deviceLogEntityList = DeviceLogService.getDeviceLogList(connectedDevice.getDeviceId());
        if (!deviceLogEntityList.isEmpty()) {
            for (DeviceLogEntity deviceLogEntity : deviceLogEntityList) {
                log.info("恢复APP: " + deviceLogEntity.getContent());
                ShellApiExecResult<Void> result = config_isUseHideAppMode() ?
                        adbShell.setAppHide(deviceLogEntity.getContent(), true) :
                        adbShell.setEnabled(deviceLogEntity.getContent(), true);
                if (result.success) {
                    deviceLogEntity.setState(0);
                    DeviceLogService.save(deviceLogEntity);
                } else {
                    log.warn("恢复APP {} 失败, {}", deviceLogEntity.getContent(), result.msg);
                }
            }
        }

        ShellApiExecResult<List<String>> result = adbShell.getPackageList(PackageFilterParam.disabled);
        if (!result.success) {
            log.warn("禁用app列表获取失败\n" + result.msg);
            resultWrapper.resultMsg = "获取临时清单失败\n" + result.msg;
            return resultWrapper;
        }
        if (result.data == null || result.data.isEmpty()) {
            log.warn("禁用app列表结果为空");
        } else {
            for (String s : result.data) {
                log.debug("恢复本机APP: " + s);
                ShellApiExecResult<Void> enabled = config_isUseHideAppMode() ?
                        adbShell.setAppHide(s, true) :
                        adbShell.setEnabled(s, true);
                if (!enabled.success) {
                    log.warn("恢复APP {} 失败, {}", s, enabled.msg);
                }
            }
        }
        resultWrapper.resultSuccess = true;
        return resultWrapper;
    }

    /**
     * 暂时为测试使用 华为、荣耀等 手机 账号检测时 禁用APP不管用
     * 原为adb shell pm enable/disable-user pkg/type
     * 返回true时 adb shell pm hide/unhide pkg/type
     *
     * 测试发现 使用hide会报错
     * java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_USERS.
     * @return
     */
    private boolean config_isUseHideAppMode() {
        return false;
//        if (connectedDevice == null) return false;
//        String phoneManufacturer = connectedDevice.getPhoneManufacturer().toLowerCase(Locale.ROOT);
//        return phoneManufacturer.contains("honor");
    }

    /**
     * 默认是设置profileowner vivo设置为deviceowner
     *
     * @return
     */
    private boolean config_isSetDeviceOwner() {
        if (connectedDevice == null) return false;
        String phoneManufacturer = connectedDevice.getPhoneManufacturer().toLowerCase(Locale.ROOT);
        return check_vivo_device_owner && phoneManufacturer.contains("vivo");
    }

    private static class ResultWrapper {
        Set<String> hideErrorList = new HashSet<>();
        boolean resultSuccess;
        String resultMsg;

    }

    //endregion

    //region 点击事件


    public void onUSBDebugCLick(MouseEvent mouseEvent) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle(BuildConfig.appName);
        Scene scene = getContext().getApplicationContext().startStage(stage).startScene("/fxml/tip_usb_debug.fxml");
        JavaFXUtil.enableWindowDrag(stage);
    }

    public void onTipDeviceConnectClick(MouseEvent mouseEvent) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle(BuildConfig.appName);
        Scene scene = getContext().getApplicationContext().startStage(stage).startScene("/fxml/tip_connect_device.fxml");
        JavaFXUtil.enableWindowDrag(stage);
    }

    public void onUSBVivoClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbVivo.html");
    }

    public void onUSBOppoClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbOppo.html");
    }

    public void onUSBXiaomiClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbXiaomi.html");
    }

    public void onUSBSanxingClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbSamsung.html");
    }

    public void onUSBHuaweiClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbHuawei.html");
    }

    private void openURL(String url) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(URI.create(url));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            showTipDialog("打开网页 " + url + " 失败\n" + e.getMessage());
        }
    }

    public void onOpenUSBCourseClick(MouseEvent mouseEvent) {

    }

    public void onOpenCompleteCourseClick(MouseEvent mouseEvent) {

    }


    public void onCancelClick(MouseEvent actionEvent) {
        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog("执行操作中...");
                stopRefreshDeviceTask();
                onCancelClick();
                startRefreshDeviceTask();
                hideLoadingDialog();
            }
        });
    }

    private void onCancelClick() {

        ShellApiExecResult<ProfileOwner> result = adbShell.getProfileOwner();
        if (!result.success) {
            showTipDialog("查询设备已激活信息失败\n" + result.msg);
            return;
        }
        ProfileOwner profileOwner = result.data;
        if (profileOwner == null) {
            showTipDialog("设备没有查到有激活信息，不需要激活");
            return;
        }
        if (!showConfirmDialog("是否取消激活？\n已激活APP信息：" + profileOwner.getPkgName())) {
            return;
        }
        ShellApiExecResult<Boolean> execResult = adbShell.removeActiveAdmin(profileOwner.getComponentName().packageName, profileOwner.getComponentName().className);
        if (execResult.success) {
            showTipDialog("取消激活" + (execResult.data == Boolean.TRUE ? "成功" : "失败"));
        } else {
            showTipDialog("取消激活执行失败\n" + execResult.msg);
        }
    }

    public void onStartClick(MouseEvent actionEvent) {
        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                stopRefreshDeviceTask();
                onStartClick();
                startRefreshDeviceTask();
            }
        });
    }


    private void onRefreshDeviceClick() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                connectedDevice = null;
                ShellApiExecResult<List<Device>> result = runIOThread(() -> adbShell.getDeviceList());

                if (result.success) {
                    if (result.data == null || result.data.isEmpty()) {
                        llDeviceContainer.getChildren().remove(tvDeviceInfo);
                        if (!llDeviceContainer.getChildren().contains(llDeviceEmpty))
                            llDeviceContainer.getChildren().add(llDeviceEmpty);

//                        tvDeviceInfo.setVisible(false);
//                        llDeviceEmpty.setVisible(true);
                    } else if (result.data.size() > 1) {
                        tvDeviceInfo.setText("当前有多个设备连接，请保持只有一台设备");
                        if (!llDeviceContainer.getChildren().contains(tvDeviceInfo))
                            llDeviceContainer.getChildren().add(tvDeviceInfo);
                        llDeviceContainer.getChildren().remove(llDeviceEmpty);
//                        tvDeviceInfo.setVisible(true);
//                        llDeviceEmpty.setVisible(false);
                    } else {
                        ShellApiExecResult<Map<String, String>> execResult = adbShell.getProps();
                        if (!execResult.success) {
                            tvDeviceInfo.setText("获取设备信息失败，请检查USB连接是否正常");
                            if (!llDeviceContainer.getChildren().contains(tvDeviceInfo))
                                llDeviceContainer.getChildren().add(tvDeviceInfo);
                            llDeviceContainer.getChildren().remove(llDeviceEmpty);
//                            tvDeviceInfo.setVisible(true);
//                            llDeviceEmpty.setVisible(false);
                            return;
                        }
                        Map<String, String> map = execResult.data;
                        //[ro.product.device]: [raphael]
                        //[ro.product.odm.brand]: [Xiaomi]
                        //[ro.product.odm.device]: [raphael]
                        //[ro.product.odm.manufacturer]: [Xiaomi]
                        //[ro.product.odm.model]: [Redmi K20 Pro]
                        //[ro.product.odm.name]: [raphael]
                        //[ro.product.product.brand]: [Xiaomi]
                        //[ro.product.product.device]: [raphael]
                        //[ro.product.product.manufacturer]: [Xiaomi]
                        //[ro.product.product.model]: [Redmi K20 Pro]
                        //[ro.product.product.name]: [raphael]
                        //[ro.system.build.version.release]: [11]
                        //[ro.system.build.version.release_or_codename]: [11]
                        //[ro.system.build.version.sdk]: [30]

                        connectedDevice = new DeviceWrapper(result.data.get(0), map);
                        tvDeviceInfo.setText(
                                String.format("%s %s Android %s (%s)" +
                                                "\n设备ID: %s",
                                        connectedDevice.getPhoneManufacturer(), connectedDevice.getPhoneModel(),
                                        connectedDevice.getAndroidVersion(), connectedDevice.getDevice().state.desc,
                                        connectedDevice.getDeviceId()
                                )
                        );
                        if (!llDeviceContainer.getChildren().contains(tvDeviceInfo))
                            llDeviceContainer.getChildren().add(tvDeviceInfo);
                        llDeviceContainer.getChildren().remove(llDeviceEmpty);
//                        tvDeviceInfo.setVisible(true);
//                        llDeviceEmpty.setVisible(false);
                    }
                } else {
                    llDeviceContainer.getChildren().remove(tvDeviceInfo);
                    if (!llDeviceContainer.getChildren().contains(llDeviceEmpty))
                        llDeviceContainer.getChildren().add(llDeviceEmpty);
//                    tvDeviceInfo.setVisible(false);
//                    llDeviceEmpty.setVisible(true);
                    showTipDialog("获取设备失败\n" + result.msg);
                    getContext().getApplicationContext().exitApp();
                }
                refreshDeviceState();
            }
        });
    }

    public void onRefreshDeviceClick(MouseEvent actionEvent) {
        showLoadingDialog("刷新中...");

        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                stopRefreshDeviceTask();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                } finally {
                    startRefreshDeviceTask();
                    hideLoadingDialog();
                }
            }
        });
    }


    public void onExitClick(MouseEvent actionEvent) {
        getContext().getApplicationContext()
                .exitApp();
    }

    public void onRecoveryAllHideApp(MouseEvent actionEvent) {
        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                stopRefreshDeviceTask();
                onRecoveryAllHideApp();
                startRefreshDeviceTask();
            }
        });
    }

    public void onInstallAPK(MouseEvent actionEvent) {
        stopRefreshDeviceTask();
        onInstallAPK();
        startRefreshDeviceTask();
    }

    private String installApkPath;

    private void onInstallAPK() {
        File file = installApkPath == null ? null : new File(installApkPath);

        if (file == null || !file.exists() || !file.isFile()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择安装APK文件");
            String apkPath = AppConfigService.getInstallApkPath();
            log.info("install apk. initial file path: {}", apkPath);
            if (apkPath != null) {
                File file1 = new File(apkPath);
                if (file1.exists()) {
                    if (file1.isFile()) {
                        fileChooser.setInitialDirectory(file1.getParentFile());
                        fileChooser.setInitialFileName(file1.getName());
                    } else if (file1.isDirectory()) {
                        fileChooser.setInitialDirectory(file1);
                    }
                }
            }
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("APK Files", "*.apk"));
            File file1 = fileChooser.showOpenDialog(getContext().getStage());
            if (file1 != null) {
                installApkPath = file1.getAbsolutePath();
                AppConfigService.setInstallApkPath(file1.getAbsolutePath());
                onInstallAPK();
            }
            return;
        }

        ButtonType pick = new ButtonType("重新选择");
        ButtonType buttonType = showTextAreaAlertAndShowAwait("请确定", "是否安装" + file.getName(), ButtonType.YES, ButtonType.NO, pick).get();
        if (buttonType == pick) {
            installApkPath = null;
            onInstallAPK();
            return;
        }
        if (buttonType == ButtonType.NO)
            return;

        showLoadingDialog("安装中...");

        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ShellApiExecResult<Void> result = adbShell.installApp(file.getAbsolutePath());
                if (result.success) {
                    showTipDialog("安装成功");
                } else {
                    showTipDialog("安装失败\n" + result.msg);

                    //adb: failed to install C:\Users\RainTang\Downloads\shadowsocks-armeabi-v7a-v5.2.6.apk: Failure [INSTALL_FAILED_ALREADY_EXISTS: Attempt to re-install com.github.shadowsocks without first uninstalling.]
                }

                hideLoadingDialog();
            }
        });
    }

    public void onHelpClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/doc/index.html");
//        File file = new File("app/doc/index.html");
//        if (!file.exists())
//            file = new File("doc/index.html");
//        Desktop desktop = Desktop.getDesktop();
//        try {
//            desktop.browse(file.toURI());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            showTipDialog("打开帮助页面失败\n" + e.getMessage());
//        }
    }

    public void onAboutClick(MouseEvent mouseEvent) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("软件信息：").append(BuildConfig.appName).append(" ").append(BuildConfig.appVersion)
                .append("\n")
                .append("\nlicense授权给：").append(config.getAppName()).append("(").append(config.getPkgName()).append(")")
                .append("\nlicense信息：").append(config.getDesc())
                .append("\nlicense有效期：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(config.getExpireTime()))
                .append("\n")
                .append("\n设备ID：").append(Utils.getMachineCode())
        ;
        showTextAreaAlertAndShowAwait("关于", stringBuilder.toString(), ButtonType.OK);
    }

    //endregion


    // region alert


    Alert loadingAlert = null;

    private void showLoadingDialog(String text) {
        showLoadingDialog("请稍等...", text);
    }

    private void showLoadingDialog(String title, String text) {
        if (!Platform.isFxApplicationThread()) {
            runUIThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    showLoadingDialog(title, text);
                    return null;
                }
            });
            return;
        }

        if (loadingAlert != null) {
            loadingAlert.setTitle(title);
            loadingAlert.setContentText(text);
            return;
        }
        loadingAlert = new Alert(Alert.AlertType.NONE);
        loadingAlert.initStyle(StageStyle.UNDECORATED);
        loadingAlert.setTitle(title);

        DialogPane dialogPane = Utils.loadFXML("/fxml/alert_loading.fxml");
        dialogPane.getStylesheets().add(getClass().getResource("/css/alert_loading.css").toExternalForm());
        dialogPane.setContentText(text);
        loadingAlert.setDialogPane(dialogPane);


        showAlert(loadingAlert);
    }

    private void hideLoadingDialog() {
        if (!Platform.isFxApplicationThread()) {
            runUIThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    hideLoadingDialog();
                    return null;
                }
            });
            return;
        }

        if (loadingAlert != null) {
            loadingAlert.resultProperty().setValue(ButtonType.CLOSE);
            loadingAlert.close();
            loadingAlert = null;
        }
    }

    private boolean showConfirmDialog(String msg) {
        Optional<ButtonType> buttonType = showTextAreaAlertAndShowAwait("请确定", msg, ButtonType.YES, ButtonType.NO);
        return buttonType.isPresent() && buttonType.get() == ButtonType.YES;
    }

    private boolean showTipDialog(String msg) {
        Optional<ButtonType> buttonType = showTextAreaAlertAndShowAwait("提示", msg, ButtonType.OK);
        return buttonType.isPresent() && buttonType.get() == ButtonType.OK;
    }

    private <T> T runIOThread(Callable<T> callable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<T> atomicReference = new AtomicReference<>();
        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    atomicReference.set(callable.call());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        return atomicReference.get();
    }

    private <T> T runUIThread(Callable<T> callable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<T> atomicReference = new AtomicReference<>();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    atomicReference.set(callable.call());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        return atomicReference.get();
    }

    private Optional<ButtonType> showTextAreaAlertAndShowAwait(String title, String msg, ButtonType... buttonType) {
        if (buttonType == null || buttonType.length == 0) {
            throw new RuntimeException("button is empty");
        }

        if (!Platform.isFxApplicationThread()) {
            return runUIThread(new Callable<Optional<ButtonType>>() {
                @Override
                public Optional<ButtonType> call() throws Exception {
                    return showTextAreaAlertAndShowAwait(title, msg, buttonType);
                }
            });
        }

        Alert alert = new Alert(Alert.AlertType.NONE, msg, buttonType);

        alert.titleProperty().bind(alert.headerTextProperty());
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setHeaderText(title);

        DialogPane dialogPane = alert.getDialogPane();
        String contentText = dialogPane.getContentText();

        TextArea textArea = new TextArea(contentText);
        textArea.textProperty().bind(dialogPane.contentTextProperty());

        dialogPane.setContent(textArea);
        dialogPane.getStylesheets().add(getClass().getResource("/css/alert_text_area.css").toExternalForm());
        return showAlertAndAwait(alert);
    }

    private void showAlert(Alert alert) {
        if (!Platform.isFxApplicationThread()) {
            runUIThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    showAlert(alert);
                    return null;
                }
            });
            return;
        }

        JavaFXUtil.enableDialogDrag(alert);
        alert.initOwner(getContext().getStage());
        alert.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == Boolean.TRUE) {
//                    log.info("\n{}", DebugUtil.dump(alert.getDialogPane()));
                }
            }
        });
        alert.show();
    }

    private Optional<ButtonType> showAlertAndAwait(Alert alert) {
        if (!Platform.isFxApplicationThread()) {
            return runUIThread(new Callable<Optional<ButtonType>>() {
                @Override
                public Optional<ButtonType> call() throws Exception {
                    return showAlertAndAwait(alert);
                }
            });
        }

        JavaFXUtil.enableDialogDrag(alert);
        alert.initOwner(getContext().getStage());
        alert.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == Boolean.TRUE) {
//                    log.info("\n{}", DebugUtil.dump(alert.getDialogPane()));
                }
            }
        });
        return alert.showAndWait();
    }

    // endregion

    //region 设备在线检测
    private final Object object = new Object();
    Thread thread;
    boolean inRefreshing = false;
    boolean threadStop;

    private void startRefreshThread() {
        threadStop = false;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadStop) {
                    //log.debug("refresh device thread {}",inRefreshing);
                    if (inRefreshing)
                        onRefreshDeviceClick();
                    try {
                        Thread.sleep(config_refresh_device_interval_time);
                    } catch (InterruptedException e) {
                        log.debug("refresh device thread interrupted");
                        break;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void stopRefreshThread() {
        if (thread != null)
            thread.interrupt();
        threadStop = true;
        thread = null;
    }

    private void startRefreshDeviceTask() {
        synchronized (object) {
            if (inRefreshing) return;
            inRefreshing = true;
        }
    }

    private void stopRefreshDeviceTask() {
        synchronized (object) {
            if (!inRefreshing) {
                return;
            }
            inRefreshing = false;
        }

    }
    // endregion

    ObjectProperty<Boolean> deviceConnected = new SimpleObjectProperty<Boolean>(null) {
        @Override
        protected void invalidated() {
            Boolean value = getValue();

            btStart.setDisable(!value);

            double opacity = value ? 1 : 0.5d;

            btStart.setOpacity(opacity);
        }
    };

    private void refreshDeviceState() {
        Boolean usable = connectedDevice != null && connectedDevice.getDevice().state == Device.DeviceState.device;
        deviceConnected.setValue(usable);
    }


    private boolean isAdbUsable() {
        ShellApiExecResult<List<Device>> deviceList = adbShell.getDeviceList();
        return !(!deviceList.success && deviceList.msg.contains("adb"));
        //Cannot run program "adb" (in directory "."): CreateProcess error=2, 系统找不到指定的文件。
    }

    public void downloadQRCode(MouseEvent mouseEvent) {

    }

    public void startQRCode(MouseEvent mouseEvent) {

    }

    public void stopQRCode(MouseEvent mouseEvent) {

    }
}
