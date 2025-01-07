package com.tangrun.mdm.boxwindow.controller;

import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.core.BaseController;
import com.tangrun.mdm.boxwindow.core.LifecycleComposeEvent;
import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
//import com.tangrun.mdm.boxwindow.dao.AppConfigService;
//import com.tangrun.mdm.boxwindow.dao.DeviceLogService;
import com.tangrun.mdm.boxwindow.dao.AppConfigService;
import com.tangrun.mdm.boxwindow.service.ConfigService;
//import com.tangrun.mdm.boxwindow.service.DBService;
import com.tangrun.mdm.boxwindow.shell.core.ShellExecuteLogger;
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
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    private static final int config_hide_account_count = 5;
    private static final long config_refresh_device_interval_time = 1000;

    /**
     * 隐藏账号获取时 type 因为不一定是包名 而且看adb源码得知会报错 所以 一般不用
     */
    private static boolean config_hide_account_type = false;

    @FXML
    public Label tvDeviceInfo;
    @FXML
    public ImageView btRefreshDevice;
    @FXML
    public ImageView btStart;
    @FXML
    public ImageView btCancel;
    @FXML
    public ImageView btRecoveryAllHideApp;
    @FXML
    public ImageView btInstallAPK;
    @FXML
    public ImageView btHelp;
    @FXML
    public ImageView btAbout;
    @FXML
    public Label tvTitle;

    private Config config;

    private ShellApi adbShell;

    /**
     * 当前连接设备
     */
    private DeviceWrapper connectedDevice;

    private void showInputLicense() {
        ButtonType btSave = new ButtonType("保存");
        ButtonType btExit = new ButtonType("退出");
        Alert alert = new Alert(Alert.AlertType.NONE, null, btSave, btExit);
        alert.setTitle("输入license");
        DialogPane dialogPane = alert.getDialogPane();
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(true);
        dialogPane.setContent(textArea);
        Optional<ButtonType> optional = showAlertAndAwait(alert);
        if (optional.isPresent() && optional.get() == btSave) {
            String text = textArea.getText();
            boolean b = ConfigService.getInstance().saveConfig(text);
            if (b) {
                showTipDialog("请重启软件生效");
                getContext().getApplicationContext()
                        .exitApp();
            } else {
                File file = new File("");
                showTipDialog("保存失败，请手动复制license.txt到" + file.getAbsolutePath() + "目录下");
            }
        }
    }

    private void showInputLicenseTip(String msg) {
        if (!Platform.isFxApplicationThread()) {
            runUIThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    showInputLicenseTip(msg);
                    return null;
                }
            });
            return;
        }
        ButtonType btLicense = new ButtonType("输入license");
        ButtonType btExit = new ButtonType("退出");
        Optional<ButtonType> optional = showTextAreaAlertAndShowAwait("提示", msg, btLicense, btExit);
        if (optional.isPresent()) {
            ButtonType buttonType = optional.get();
            if (buttonType == btLicense) {
                showInputLicense();
            }
        }
    }

    @Override
    public void initialLazy() {

        log.debug("main controller initialLazy");

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
//                                    if (!DBService.getInstance().initTable()) {
//                                        showTipDialog("数据初始化失败或已在运行中");
//                                        getContext().getApplicationContext().exitApp();
//                                        return;
//                                    }

                                    ConfigWrapper configWrapper = ConfigService.getInstance().getConfig();
                                    if (configWrapper.getConfig() == null) {
                                        showInputLicenseTip(configWrapper.getMsg());
                                        getContext().getApplicationContext()
                                                .exitApp();
                                        return;
                                    }
                                    AppConfigService.setAppUseTime();

                                    config = configWrapper.getConfig();
//                                    if (config.getDebug() == Boolean.TRUE) {
//                                        LoggerContext context = (LoggerContext) LogManager.getContext(false);
//                                        Configuration configuration = context.getConfiguration();
//
//                                        // 配置一个 FileAppender，指定输出到文件
//                                        RollingRandomAccessFileAppender appender = RollingRandomAccessFileAppender.newBuilder()
//                                                .withFileName(ConfigService.getDataDir() + "log/debug.log")
//                                                .withFilePattern(ConfigService.getDataDir()+"log/debug_%d{yyyyMMdd_HHmm}")
//                                                .setName("debugFile")
//                                                .withPolicy(TimeBasedTriggeringPolicy.newBuilder()
//                                                        .withInterval(30)
//                                                        .build())
//                                                .setFilter(ThresholdFilter.createFilter(Level.DEBUG, Filter.Result.ACCEPT, Filter.Result.DENY))
//                                                .setLayout(PatternLayout.newBuilder().withPattern("%d %-5p [%t] %C{2} (%F:%L) - %m%n").build())
//                                                .build();
//                                        appender.start();
//                                        configuration.addAppender(appender);
//                                        LoggerConfig rootLogger = configuration.getRootLogger();
//                                        rootLogger.setLevel(Level.DEBUG);
//                                        rootLogger.addAppender(appender, null, null);
//
//                                        context.updateLoggers();
//
//                                    }

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

    //region 激活过程 与Box一致

    private void onStartClick() {
        if (!showConfirmDialog("是否开始激活？")) {
            return;
        }
        showLoadingDialog("激活中...\n\n激活过程中请勿断开手机连接！！！");
        ResultWrapper resultWrapper = startRegistration_setProfileOwner();
        if (resultWrapper.resultMsg.contains(RESULT_fail_multi_account)) {
            showTipDialog(resultWrapper.resultMsg);
            if (showConfirmDialog("现在是否需要重启手机？")) {
                ShellApiExecResult<Void> result = adbShell.reboot();
                showTipDialog(result.success ? "已重启，请稍等，开机后请再执行激活操作" : "自动重启失败，请手动重启");
            }
        } else {
            startRegistration_recoveryHideApp();
            showTipDialog(resultWrapper.resultMsg);
        }
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


    protected String RESULT_fail_uninstall_app = "还未安装应用";
    protected String RESULT_fail_shell_error = "操作失败，请检查设备连接再进行激活";
    protected String RESULT_fail_multi_user = "激活失败，请关闭系统分身/应用双开等多开功能后再试";
    protected String RESULT_fail_xiaomi_no_manager_device_admins_permisiion = "激活失败，小米用户请手动在系统设置=>开发者设置=>开启“USB 调试（安全设置）”，如仍不可以请关闭“MIUI 优化”";
    protected String RESULT_fail_multi_account = "激活失败，请尝试以下操作后再重试\n1.荣耀和VIVO手机请先退出荣耀、VIVO账号；\n2.前往设置”账号与同步“，手动退出所有账号；\n3.重启手机\n\n";
    protected String RESULT_fail_has_other_app_set = "激活失败，已有其他软件被激活";


    private ResultWrapper startRegistration_setProfileOwner() {
        log.info("registration. start");

        ResultWrapper resultWrapper = new ResultWrapper();

        {
            log.info("registration. check has profile owner");

            ShellApiExecResult<AdminOwnerInfo> result = adbShell.getDeviceOwner();
            if (!result.success) {
                log.warn("get profile owner error. {}", result.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (result.data != null) {
                if (Objects.equals(result.data.getComponentName().packageName, config.getPkgName())) {
                    resultWrapper.resultMsg = "应用已激活";
                } else
                    resultWrapper.resultMsg = "已经有激活的应用了\n" + result.data.getComponentName().packageName;
                return resultWrapper;
            }

            result = adbShell.getProfileOwner();
            if (!result.success) {
                log.warn("get device owner error. {}", result.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (result.data != null) {
                if (Objects.equals(result.data.getComponentName().packageName, config.getPkgName())) {
                    resultWrapper.resultMsg = "应用已激活";
                } else
                    resultWrapper.resultMsg = "已经有激活的应用了\n" + result.data.getComponentName().packageName;
                return resultWrapper;
            }


            ShellApiExecResult<List<String>> packageList = adbShell.getPackageList(getMainUserId());
            if (!packageList.success) {
                log.warn("get packagelist error. {}", result.msg);
                resultWrapper.resultMsg = RESULT_fail_shell_error;
                return resultWrapper;
            }
            if (!packageList.data.contains(config.getPkgName())) {
                resultWrapper.resultMsg = RESULT_fail_uninstall_app;
                return resultWrapper;
            }
        }


        // 多用户检测
        {
            log.info("registration. multi user checking.");

            ShellApiExecResult<List<UserInfo>> userList = adbShell.getUserList();
            if (!userList.success) {
                log.warn("get user list error. {}", userList.msg);
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
                log.info("has multi user.{}", stringBuilder.toString());
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
                    log.info("disable user result, id: {} name: {}", datum.id, datum.name);
                    ShellApiExecResult<Void> result = adbShell.removeUser(datum.id);
                    log.info("disable user result, id: {} name: {}, success: {}", datum.id, datum.name, result.success);
                    if (!result.success) {
                        log.warn("disable user error. {}", result.msg);
                        String phoneManufacturer = connectedDevice.getPhoneManufacturer().toLowerCase();
                        if (phoneManufacturer.contains("vivo")){
                            // vivo的源自隐私系统 新版本采用多用户形式， 不移除也不影响激活 删是删不掉的
                            if (datum.id == 666) {
                                if (!showConfirmDialog("注意！激活后将无法使用原子隐私系统，请提前备份其中资料，确定继续激活？")) {
                                    resultWrapper.resultMsg = "操作已中止";
                                    return resultWrapper;
                                }
                                continue;
                            }
                        }

                        resultWrapper.resultMsg = "移除用户失败\n" + result.msg;
                        if (datum.id == 999) {
                            resultWrapper.resultMsg += "\n建议手动关闭应用双开后再试，最好同时杀死所有后台APP后重启手机再执行激活操作，可能需要重复2-3次";
                        }
                        if (phoneManufacturer.contains("xiaomi")){
                            if (datum.id == 10) {
                                resultWrapper.resultMsg += "\n建议手动关闭系统分身后再试，最好同时杀死所有后台APP后重启手机再执行激活操作，可能需要重复2-3次";
                            }
                        }
                        return resultWrapper;
                    }
                }
            }
        }
        startRegistration_setProfileOwner(0, resultWrapper);
        return resultWrapper;
    }

    private String getMainUserId() {
        ShellApiExecResult<List<UserInfo>> result = adbShell.getUserList();
        if (result.success) {
            for (UserInfo datum : result.data) {
                if (datum.isMain()) {
                    return datum.id + "";
                }
            }
        }
        return null;
    }

    private ResultWrapper startRegistration_recoveryHideApp() {
        ResultWrapper resultWrapper = new ResultWrapper();

//        List<DeviceLogEntity> deviceLogEntityList = DeviceLogService.getDeviceLogList(connectedDevice.getDeviceId());
//        if (!deviceLogEntityList.isEmpty()) {
//            for (DeviceLogEntity deviceLogEntity : deviceLogEntityList) {
//                log.info("set enabled 1: " + deviceLogEntity.getContent());
//                ShellApiExecResult<Void> result = adbShell.setEnabled(deviceLogEntity.getContent(), true);
//                if (result.success) {
//                    deviceLogEntity.setState(0);
//                    DeviceLogService.save(deviceLogEntity);
//                } else {
//                    log.warn("recovery hide app {} error, {}", deviceLogEntity.getContent(), result.msg);
//                }
//            }
//        }

        ShellApiExecResult<List<String>> result = adbShell.getPackageList(getMainUserId(), PackageFilterParam.disabled);
        if (!result.success) {
            log.warn("禁用app列表获取失败\n" + result.msg);
            resultWrapper.resultMsg = "获取临时清单失败\n" + result.msg;
            return resultWrapper;
        }
        if (result.data == null || result.data.isEmpty()) {
            log.warn("禁用app列表结果为空");
        } else {
            for (String s : result.data) {
                log.info("set enabled 2: " + s);
                ShellApiExecResult<Void> enabled = adbShell.setEnabled(s, true);
                if (!enabled.success) {
                    log.warn("恢复 " + s + " 失败\n" + enabled.msg);
                }
            }
        }
        resultWrapper.resultSuccess = true;
        return resultWrapper;
    }

    private void startRegistration_setProfileOwner(int count, ResultWrapper resultWrapper) {
        if (count > config_reset_profile_owner_count) {
            log.warn("registration. retry timeout {}", count);
            StringJoiner stringJoiner = new StringJoiner("\n", "\n", "");
            for (String s : resultWrapper.hideErrorList) {
                stringJoiner.add(s);
            }
            resultWrapper.resultMsg = RESULT_fail_multi_account + stringJoiner;
            return;
        }
        log.warn("registration. set {}", count);
        // hide有账号的应用
        {
            log.info("registration. disable has account app.");

            for (int i = 0; i < config_hide_account_count; i++) {
                ShellApiExecResult<List<UserAccounts>> userAccounts = adbShell.getUserAccounts();
                if (!userAccounts.success) {
                    log.warn("get user account error. {}", userAccounts.msg);
                    resultWrapper.resultMsg = RESULT_fail_shell_error;
                    return;
                }
                for (UserAccounts accounts : userAccounts.data) {
                    if (!accounts.userInfo.isMain()) {
                        continue;
                    }
                    if (accounts.serviceInfoList.isEmpty() && accounts.accountList.isEmpty()) {
                        log.info("user account list empty, break for loop.");
                        i = 10;
                        break;
                    }
                    for (ServiceInfo serviceInfo : accounts.serviceInfoList) {
//                        DeviceLogEntity deviceLogEntity = DeviceLogService.getDeviceLog(connectedDevice.getDeviceId(), serviceInfo.componentName.packageName);
//                        if (deviceLogEntity == null)
//                            deviceLogEntity = new DeviceLogEntity();
//                        deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
//                        deviceLogEntity.setContent(serviceInfo.componentName.packageName);
//                        deviceLogEntity.setContentType(0);
//                        deviceLogEntity.setState(1);
//                        DeviceLogService.save(deviceLogEntity);

//                        ShellApiExecResult<Void> result = adbShell.setEnabled(serviceInfo.componentName.packageName+"/"+serviceInfo.componentName.className, false);
                        log.info("set disabled pkg: " + serviceInfo.componentName.packageName);
                        ShellApiExecResult<Void> result = adbShell.setEnabled(serviceInfo.componentName.packageName, false);
                        if (!result.success) {
                            resultWrapper.hideErrorList.add(serviceInfo.componentName.packageName);
                            log.warn("hide package {} error. {}", serviceInfo.componentName.packageName, result.msg);
//                            if (!showConfirmDialog("账号应用 "+serviceInfo.componentName.packageName+ " 处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
//                                return;
//                            }
                        } else {
                            resultWrapper.hideErrorList.remove(serviceInfo.componentName.packageName);
                        }
                    }

                    if (config_hide_account_type) {
                        for (Account account : accounts.accountList) {
//                            DeviceLogEntity deviceLogEntity = DeviceLogService.getDeviceLog(connectedDevice.getDeviceId(), account.type);
//                            if (deviceLogEntity == null)
//                                deviceLogEntity = new DeviceLogEntity();
//                            deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
//                            deviceLogEntity.setContent(account.type);
//                            deviceLogEntity.setContentType(1);
//                            deviceLogEntity.setState(1);
//                            DeviceLogService.save(deviceLogEntity);

                            log.info("set disabled type: " + account.type);
                            ShellApiExecResult<Void> result = adbShell.setEnabled(account.type, false);
                            if (!result.success) {
                                log.warn("hide type {} error. {}", account.type, result.msg);
                                resultWrapper.hideErrorList.add(account.type);
//                            if (!showConfirmDialog("账号应用组件 "+account.type+" 处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
//                                return;
//                            }
                            } else {
                                resultWrapper.hideErrorList.remove(account.type);
                            }
                        }
                    }
                    if (count == 0 && i == 0 && !resultWrapper.hideErrorList.isEmpty()) {
                        StringJoiner stringJoiner = new StringJoiner("\n", "\n", "");
                        for (String s : resultWrapper.hideErrorList) {
                            stringJoiner.add(s);
                        }
                        showTipDialog("可能需要暂时移除以下应用的账号记录：" + stringJoiner);
                    }
                }
            }
        }

        // 激活
        {
            log.info("registration. set profile owner.");

            adbShell.setDeviceAdmin(config.getComponent());
            String phoneManufacturer = connectedDevice.getPhoneManufacturer().toLowerCase(Locale.ROOT);
            ShellApiExecResult<Void> result;
            int androidVersion = Integer.MAX_VALUE;
            try {
                androidVersion = Integer.parseInt(connectedDevice.getAndroidVersion());
            } catch (Exception e) {

            }

            boolean deviceOwner = false;
            if (config.getAppName().equals("com.cdblue.awlm") && phoneManufacturer.contains("vivo")) {
                // 内蒙古的vivo直接最高
                deviceOwner = true;
            }
            if (androidVersion < 7) {
                // clearProfileOwner在7.0才有 所以只能是deviceOwner
                deviceOwner = true;
            }
//            if (androidVersion == 14 && showConfirmDialog("使用更高级别的激活方式？")) {
//                deviceOwner = true;
//            }

            if (deviceOwner) {
                result = adbShell.setDeviceOwner(config.getComponent());
                if(!result.success){
                    result = adbShell.setProfileOwner(config.getComponent());
                }
            } else {
                result = adbShell.setProfileOwner(config.getComponent());
            }


            resultWrapper.resultSuccess = false;
            if (result.msg != null) {
                if (result.msg.contains("is already set") || result.msg.contains("already has a")) {
                    resultWrapper.resultMsg = RESULT_fail_has_other_app_set;
                } else if (result.msg.contains("there are already some accounts")) {
                    startRegistration_setProfileOwner(count + 1, resultWrapper);
                } else if (result.msg.contains("users on the")) {
                    resultWrapper.resultMsg = RESULT_fail_multi_user;
                } else if (result.msg.contains("android.permission.MANAGE_DEVICE_ADMINS")
                        || result.msg.contains("Calling identity is not authorized")) {
                    resultWrapper.resultMsg = RESULT_fail_xiaomi_no_manager_device_admins_permisiion;
                } else if (result.msg.contains("User must be running and unlocked")) {
                    resultWrapper.resultMsg = "激活失败，请确认是否解锁屏幕进入桌面，且激活过程保持常亮";
                } else if (result.msg.contains("Unknown admin: ComponentInfo")) {
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
                } else if (result.msg.contains("is being removed")) {
                    resultWrapper.resultMsg = "激活失败，应用已取消激活，请打开应用并启用设备管理器。或在设置中搜索 设备管理器，找到当前应用并启用。";
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

    private class ResultWrapper {
        Set<String> hideErrorList = new HashSet<>();
        boolean resultSuccess;
        String resultMsg;

    }

    //endregion

    //region 点击事件


    public void onCancelClick(MouseEvent actionEvent) {
        IOService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog("执行操作中...");
                stopRefreshDeviceTask();
//                onCancelClick();
                adbShell.startDeviceRegistrationAction();
                startRefreshDeviceTask();
                hideLoadingDialog();
            }
        });
    }

    private void onCancelClick() {

        ShellApiExecResult<AdminOwnerInfo> result = adbShell.getDeviceOwner();
        if (!result.success) {
            showTipDialog("查询设备已激活信息失败\n" + result.msg);
            return;
        }

        // device owner
        if (result.data == null) {
            result = adbShell.getProfileOwner();
            if (!result.success) {
                showTipDialog("查询设备已激活信息失败\n" + result.msg);
                return;
            }
        }

        AdminOwnerInfo adminOwnerInfo = result.data;
        if (adminOwnerInfo == null) {
            showTipDialog("设备没有查到有激活信息，不需要激活");
            return;
        }
        if (!showConfirmDialog("是否取消激活？\n已激活APP信息：" + adminOwnerInfo.getComponentName().packageName)) {
            return;
        }
        ShellApiExecResult<Boolean> execResult = adbShell.removeActiveAdmin(adminOwnerInfo.getComponentName().packageName, adminOwnerInfo.getComponentName().className);
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
                        tvDeviceInfo.setText(
                                "无设备连接，若有问题可尝试以下方法：" +
                                        "\n\t1、检查手机是否已打开USB调试模式" +
                                        "\n\t2、重新插拔USB连接线"
                        );
                    } else if (result.data.size() > 1) {
                        tvDeviceInfo.setText("存在多个设备连接，请保持只有一台设备");
                    } else {
                        ShellApiExecResult<Map<String, String>> execResult = adbShell.getProps();
                        if (!execResult.success) {
                            tvDeviceInfo.setText("查询连接设备信息失败");
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
                                                "\ndeviceId: %s",
                                        connectedDevice.getPhoneManufacturer(), connectedDevice.getPhoneModel(),
                                        connectedDevice.getAndroidVersion(), connectedDevice.getDevice().state.desc,
                                        connectedDevice.getDeviceId()
                                )
                        );
                    }
                } else {
                    tvDeviceInfo.setText("获取设备失败");
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

    private Optional<String> installApkPath = null;

    private void onInstallAPK() {
        String lastPickFile = AppConfigService.getInstallApkPath();
        if (lastPickFile == null) lastPickFile = "";
        if (installApkPath == null) {
            if (lastPickFile.isBlank()) {
                installApkPath = Optional.empty();
            } else {
                installApkPath = Optional.of(lastPickFile);
            }
        }

        if (installApkPath.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择安装APK文件");

            log.info("install apk. initial file path: {}", lastPickFile);
            if (!lastPickFile.isBlank()) {
                File file1 = new File(lastPickFile);
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
                installApkPath = Optional.of(file1.getAbsolutePath());
                AppConfigService.setInstallApkPath(file1.getAbsolutePath());
                onInstallAPK();
            }
            return;
        }
        File file = new File(installApkPath.get());

        ButtonType pick = new ButtonType("重新选择");
        ButtonType buttonType = showTextAreaAlertAndShowAwait("请确定", "是否安装" + file.getName(), ButtonType.YES, ButtonType.NO, pick).get();
        if (buttonType == pick) {
            installApkPath = Optional.empty();
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
        File file = new File("doc/index.html");
        if (!file.exists()) {
            file = new File("app/doc/index.html");
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(file.toURI());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            showTipDialog("打开帮助页面失败\n" + e.getMessage());
        }
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
        ButtonType buttonType = new ButtonType("新授权码");
        ButtonType debugType = new ButtonType("打开日志");

        Optional<ButtonType> optional = null;
        if (config.getDebug() == Boolean.TRUE) {
            optional = showTextAreaAlertAndShowAwait("关于", stringBuilder.toString(), ButtonType.OK, buttonType, debugType);
        } else {
            optional = showTextAreaAlertAndShowAwait("关于", stringBuilder.toString(), ButtonType.OK, buttonType);
        }
        if (optional.isPresent()) {
            if (optional.get() == buttonType)
                showInputLicense();
            else if (optional.get() == debugType) {
                try {
                    // 获取当前日志文件路径
                    File logDir = new File(ConfigService.getLogDir());
                    String openPath = logDir.getAbsolutePath();
                    if (logDir.exists() && logDir.isDirectory()) {
                        File[] files = logDir.listFiles();
                        if (files != null && files.length > 0) {
                            File file = null;
                            for (File listFile : files) {
                                if (file == null || listFile.lastModified() > file.lastModified()) {
                                    file = listFile;
                                }
                            }
                            if (file != null) {
                                openPath = file.getAbsolutePath();
                            }
                        }
                    }
                    ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe", "/select,", openPath);
                    processBuilder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
            btCancel.setDisable(!value);
            btRecoveryAllHideApp.setDisable(!value);
            btInstallAPK.setDisable(!value);

            double opacity = value ? 1 : 0.5d;

            btStart.setOpacity(opacity);
            btCancel.setOpacity(opacity);
            btRecoveryAllHideApp.setOpacity(opacity);
            btInstallAPK.setOpacity(opacity);
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
}
