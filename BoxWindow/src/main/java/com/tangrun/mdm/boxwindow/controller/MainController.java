package com.tangrun.mdm.boxwindow.controller;

import com.sun.javafx.tk.Toolkit;
import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.JavaFXApplication;
import com.tangrun.mdm.boxwindow.SpringBootApplication;
import com.tangrun.mdm.boxwindow.jpa.DeviceLogEntity;
import com.tangrun.mdm.boxwindow.jpa.DeviceLogRepository;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.core.InitializeLazy;
import com.tangrun.mdm.boxwindow.impl.ShellExecuteLogger;
import com.tangrun.mdm.boxwindow.pojo.AccountAppInfo;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.pojo.DeviceWrapper;
import com.tangrun.mdm.boxwindow.service.AppConfigService;
import com.tangrun.mdm.boxwindow.shell.ShellApiImpl;
import com.tangrun.mdm.boxwindow.shell.ShellExecutorImpl;
import com.tangrun.mdm.boxwindow.shell.core.ShellApi;
import com.tangrun.mdm.boxwindow.shell.core.ShellApiExecResult;
import com.tangrun.mdm.boxwindow.shell.pojo.*;
import com.tangrun.mdm.boxwindow.utils.DragUtil;
import com.tangrun.mdm.boxwindow.utils.IOThread;
import com.tangrun.mdm.boxwindow.utils.SerialNumberUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class MainController implements InitializeLazy {
    private final Logger logger = LogManager.getLogger(MainController.class);
    /**
     *
     */
    private static final int config_reset_profile_owner_count = 3;
    private static final int config_hide_account_package = 5;
    private static final long config_refresh_device_interval_time = 1000;

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


    private AppConfigService appConfigService;
    private DeviceLogRepository deviceLogRepository;

    private Config config;

    private ShellApi adbShell;

    /**
     * 当前连接设备
     */
    private DeviceWrapper connectedDevice;


    @Override
    public void initializeLazy() {
        JavaFXApplication.getMainStage().onCloseRequestProperty().addListener(new ChangeListener<EventHandler<WindowEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<WindowEvent>> observable, EventHandler<WindowEvent> oldValue, EventHandler<WindowEvent> newValue) {
                logger.debug("main stage closed. close main controller");
                stopRefreshDeviceTask();
                thread.interrupt();
            }
        });
        tvTitle.setText(BuildConfig.appName + " " + BuildConfig.appVersion);

        appConfigService = SpringBootApplication.getApplicationContext().getBean(AppConfigService.class);
        deviceLogRepository = SpringBootApplication.getApplicationContext().getBean(DeviceLogRepository.class);
        ConfigWrapper configWrapper = SpringBootApplication.getApplicationContext().getBean(ConfigWrapper.class);
        if (configWrapper.getConfig() == null) {
            showTipDialog(configWrapper.getMsg() + "\n\n设备ID: " + SerialNumberUtil.getMachineCode());
            JavaFXApplication.exitApp();
            return;
        }
        config = configWrapper.getConfig();

//        tvConfigInfo.setText(
//                config.getAppName() + "(" + config.getPkgName() + ")" +
//                        "\n" + config.getDesc() +
//                        "\nlicense有效期：" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(config.getExpireTime()))
//        );

        ShellApiImpl shellApiCmd = new ShellApiImpl(new ShellExecutorImpl());
        shellApiCmd.setInterceptor(new ShellExecuteLogger());
        adbShell = shellApiCmd;

        // adb
        if (!isAdbUsable()) {
            showTipDialog("adb工具无法使用");
            JavaFXApplication.exitApp();
            return;
        }

        // adb device listener
        startRefreshDeviceTask();
    }

    //region 激活过程 与Box一致

    protected String RESULT_fail_shell_error = "操作失败，请检查设备连接再进行激活";
    protected String RESULT_fail_multi_user = "激活失败，请关闭系统分身/应用双开等多开功能后再试";
    protected String RESULT_fail_xiaomi_no_manager_device_admins_permisiion = "激活失败，小米用户请手动在系统设置=>开发者设置=>开启“USB 调试（安全设置）”，如仍不可以请关闭“MIUI 优化”";
    protected String RESULT_fail_multi_account = "激活失败，还有账户存在，请手动移除账号";
    protected String RESULT_fail_has_other_app_set = "激活失败，已有其他软件被激活";


    private void startRegistration_pre() {
        logger.info("registration. start");

        {
            logger.info("registration. check has profile owner");

            ShellApiExecResult<ProfileOwner> result = adbShell.getProfileOwner();
            if (!result.success) {
                logger.error("get profile owner error. {}", result.msg);
                showTipDialog(RESULT_fail_shell_error);
                return;
            }
            if (result.data != null) {
                showTipDialog("已经有激活的应用了\n" + result.data.getPkgName());
                return;
            }
        }


        // 多用户检测
        {
            logger.info("registration. multi user checking.");

            ShellApiExecResult<List<UserInfo>> userList = adbShell.getUserList();
            if (!userList.success) {
                logger.error("get user list error. {}", userList.msg);
                showTipDialog(RESULT_fail_shell_error);
                return;
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
                logger.info("has multi user.{}", stringBuilder.toString());
                stringBuilder
                        .append("\n发现存在多个用户，是否移除非主用户之外的用户？(直接关闭系统分身/应用双开功能)")
                        .append("\n如有系统分身/应用双开，请提前备份其数据！！！");
                if (!showConfirmDialog(stringBuilder.toString())) {
                    return;
                }
                // 开始移除other用户
                for (UserInfo datum : userList.data) {
                    if (datum.isMain()) continue;
                    logger.info("disable user result, id: {} name: {}", datum.id, datum.name);
                    ShellApiExecResult<Void> result = adbShell.removeUser(datum.id);
                    logger.info("disable user result, id: {} name: {}, success: {}", datum.id, datum.name, result.success);
                    if (!result.success) {
                        logger.error("disable user error. {}", result.msg);
                        showTipDialog("移除用户失败\n" + result.msg);
                        return;
                    }
                }
            }
        }

        // 软件安装列表
        {
            logger.info("registration. get package list.");

            ShellApiExecResult<List<String>> packageList = adbShell.getPackageList();
            if (!packageList.success || packageList.data.isEmpty()) {
                logger.error("get package list is empty. {}", packageList.msg);
                showTipDialog(RESULT_fail_shell_error);
                return;
            }
        }

        startRegistration_setProfileOwner(0);


        // 读取主用户的账号列表 找到对应的包名
        // 循环多次 有的账号在上一次操作后才出现新的

//        boolean hasError = false;
//        StringBuilder errorReturn = new StringBuilder("失败记录：");
//        for (int i = 0; i < 10; i++) {
//            ShellApiExecResult<List<UserAccounts>> userAccounts = adbShell.getUserAccounts();
//            if (!userAccounts.success)
//                continue;
//            for (UserAccounts datum : userAccounts.data) {
//
//            }
//            ShellApiExecResult<Map<String, AccountAppInfo>> currentHasAccountAppMap = getMainUserAccountApps(packageList.data);
//            if (currentHasAccountAppMap.success) {
//                for (Map.Entry<String, AccountAppInfo> entry : currentHasAccountAppMap.data.entrySet()) {
//                    if (!entry.getValue().hasPackage) {
//                        logger.info("account not found package, key: {}, value：{}",entry.getKey(),entry.getValue());
//                        errorReturn.append("\n有未知账号APP存在，请手动操作：").append(entry.getKey()).append(" ").append(entry.getValue());
//                        hasError = true;
//                    }
//                }
//            } else {
//                logger.info("get main user account list error. {}",currentHasAccountAppMap.msg);
//                errorReturn.append("\n获取账号包名列表失败：").append(currentHasAccountAppMap.msg);
//                hasError = true;
//            }
//            if (hasError) break;
//
//            // 没有了就直接激活
//            if (currentHasAccountAppMap.data.isEmpty()) {
//                continue;
//            }
//            for (String s : currentHasAccountAppMap.data.keySet()) {
//                ShellApiExecResult<Void> result = adbShell.setEnabled(s, false);
//                logger.info("disable app, pkgName: {},result: {}", s, result.success);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    logger.error(e);
//                }
//            }
//        }


        logger.info("registration. completed.");

//        logger.info("registration. recovery hide app.");
//        // 恢复
//        ShellApiExecResult<List<String>> disabledPackageList = adbShell.getPackageList(PackageFilterParam.disabled);
//        if (disabledPackageList.success) {
//            for (String datum : disabledPackageList.data) {
//                ShellApiExecResult<Void> result1 = adbShell.setEnabled(datum, true);
//                logger.info("====：{}，{}", datum, result1.success);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            errorReturn.append("\n恢复应用设置失败");
//            logger.info("恢复应用设置失败");
//        }
//        if (success) return ShellApiExecResult.success(null);
//        else return ShellApiExecResult.fail(errorReturn.toString());
    }

    private void startRegistration_recoveryHideApp() {
        List<DeviceLogEntity> deviceLogEntityList = deviceLogRepository.findAllByDeviceId(connectedDevice.getDeviceId());
        if (deviceLogEntityList.isEmpty()) return;
        for (DeviceLogEntity deviceLogEntity : deviceLogEntityList) {
            ShellApiExecResult<Void> result = adbShell.setEnabled(deviceLogEntity.getContent(), true);
            if (result.success) {
                deviceLogEntity.setState(0);
                deviceLogRepository.save(deviceLogEntity);
            } else {
                logger.error("recovery hide app {} error, {}", deviceLogEntity.getContent(), result.msg);
            }
        }
    }

    private void startRegistration_setProfileOwner(int count) {
        if (count > config_reset_profile_owner_count) {
            logger.error("registration. retry timeout {}", count);
            showTipDialog(RESULT_fail_multi_account);
            return;
        }
        logger.error("registration. set {}", count);
        // hide有账号的应用
        {
            logger.info("registration. disable has account app.");

            for (int i = 0; i < config_hide_account_package; i++) {
                ShellApiExecResult<List<UserAccounts>> userAccounts = adbShell.getUserAccounts();
                if (!userAccounts.success) {
                    logger.error("get user account error. {}", userAccounts.msg);
                    showTipDialog(RESULT_fail_shell_error);
                    return;
                }
                for (UserAccounts accounts : userAccounts.data) {
                    if (!accounts.userInfo.isMain()) {
                        continue;
                    }
                    if (accounts.serviceInfoList.isEmpty() && accounts.accountList.isEmpty()) {
                        logger.info("user account list empty, break for loop.");
                        i = 10;
                        break;
                    }
                    for (ServiceInfo serviceInfo : accounts.serviceInfoList) {
                        DeviceLogEntity deviceLogEntity = deviceLogRepository.findFirstByDeviceIdAndContent(connectedDevice.getDeviceId(), serviceInfo.componentName.packageName);
                        if (deviceLogEntity == null)
                            deviceLogEntity = new DeviceLogEntity();
                        deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
                        deviceLogEntity.setContent(serviceInfo.componentName.packageName);
                        deviceLogEntity.setContentType(0);
                        deviceLogEntity.setState(1);
                        deviceLogRepository.saveAndFlush(deviceLogEntity);

                        ShellApiExecResult<Void> result = adbShell.setEnabled(serviceInfo.componentName.packageName, false);
                        if (!result.success) {
                            logger.error("hide package {} error. {}", serviceInfo.componentName.packageName, result.msg);
                            if (!showConfirmDialog("账号应用处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
                                return;
                            }
                        }
                    }

                    for (Account account : accounts.accountList) {
                        DeviceLogEntity deviceLogEntity = deviceLogRepository.findFirstByDeviceIdAndContent(connectedDevice.getDeviceId(), account.type);
                        if (deviceLogEntity == null)
                            deviceLogEntity = new DeviceLogEntity();
                        deviceLogEntity.setDeviceId(connectedDevice.getDeviceId());
                        deviceLogEntity.setContent(account.type);
                        deviceLogEntity.setContentType(1);
                        deviceLogEntity.setState(1);
                        deviceLogRepository.saveAndFlush(deviceLogEntity);

                        ShellApiExecResult<Void> result = adbShell.setEnabled(account.type, false);
                        if (!result.success) {
                            logger.error("hide type {} error. {}", account.type, result.msg);
                            if (!showConfirmDialog("账号应用处理失败，是否继续？\n继续有可能失败，请前往设置>账号 手动退出已登录账号")) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        // 激活
        {
            logger.info("registration. set profile owner.");

            ShellApiExecResult<Void> result = adbShell.setProfileOwner(config.getComponent());
            if (!result.success) {
                if (result.msg.contains("but profile owner is already set")) {
                    showTipDialog(RESULT_fail_has_other_app_set);
                } else if (result.msg.contains("there are already some accounts")) {
                    startRegistration_setProfileOwner(count + 1);
                } else if (result.msg.contains("users on the")) {
                    showTipDialog(RESULT_fail_multi_user);
                } else if (result.msg.contains("android.permission.MANAGE_DEVICE_ADMINS")) {
                    showTipDialog(RESULT_fail_xiaomi_no_manager_device_admins_permisiion);
//                }else  if (result.msg.contains("KNOX_PROXY_ADMIN_INTERNAL")){
                    //激活失败
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
                } else {
                    showTipDialog("激活失败\n" + result.msg);
                }
            } else {
                if (result.msg != null && result.msg.contains("Error: Unknown admin: ComponentInfo")) {
                    showTipDialog("激活失败，请先安装应用");
                } else {
                    showTipDialog("激活成功！");
                }
            }
        }
    }

    /**
     * 获取主用户下面的账号APP列表信息
     *
     * @param packageList 手机应用列表
     * @return
     */
    private ShellApiExecResult<Map<String, AccountAppInfo>> getMainUserAccountApps(List<String> packageList) {
        Map<String, AccountAppInfo> accountAppMap = new HashMap<>();
        // 账户dump出来
        // 已注册的服务是有包名的 可直接使用
        // 账号列表的 type有的是应用包名也可以使用 但是大部分是不知道那个应用的
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
                // todo 有的时候匹配不一定正确 先不用 再寻找新的解决方案
//                Map<String, List<String>> similarPkg = getSimilarPkg(account.type, packageList);
//                int max = -1;
//                int repeat = 0;
//                String maxPkgName = null;
//                for (Map.Entry<String, List<String>> entry1 : similarPkg.entrySet()) {
//                    int size = entry1.getValue().size();
//                    if (size == 0) continue;
//                    if (size > max) {
//                        max = size;
//                        maxPkgName = entry1.getKey();
//                        repeat = 0;
//                    } else if (size == max) {
//                        // 有多个相似
//                        repeat++;
//                    }
//                }
//                if (max > 0 && repeat == 0) {
//                    // 模糊匹配有结果
//                    accountAppInfo.hasPackage = true;
//                    accountAppInfo.packageName = maxPkgName;
//                }
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
        for (String s : split) {
            if (s == null) continue;
            String trim = s.trim();
            if (trim.equals("") || pattern.matcher(trim).find()) continue;
            list.add(trim);
        }
        return list;
    }

    //endregion

    //region 点击事件


    public void onCancelClick(MouseEvent actionEvent) {
        IOThread.run(new Runnable() {
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
        IOThread.run(new Runnable() {
            @Override
            public void run() {
                stopRefreshDeviceTask();
                onStartClick();
                startRefreshDeviceTask();
            }
        });
    }

    private void onStartClick() {
        if (!showConfirmDialog("是否开始激活？请确认激活APP信息:\n\t" + config.getAppName() + "(" + config.getPkgName() + ")")) {
            return;
        }
        showLoadingDialog("激活中...");
        startRegistration_pre();
        startRegistration_recoveryHideApp();
        hideLoadingDialog();
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
                }
                refreshDeviceState();
            }
        });
    }

    public void onRefreshDeviceClick(MouseEvent actionEvent) {
        showLoadingDialog("刷新中...");

        IOThread.run(new Runnable() {
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
        JavaFXApplication.exitApp();
    }

    public void onRecoveryAllHideApp(MouseEvent actionEvent) {
        IOThread.run(new Runnable() {
            @Override
            public void run() {
                stopRefreshDeviceTask();
                onRecoveryAllHideApp();
                startRefreshDeviceTask();
            }
        });
    }

    private void onRecoveryAllHideApp() {
        if (!showConfirmDialog("确定开始检查消失的APP并恢复")) {
            return;
        }

        showLoadingDialog("恢复中...");

        {
            ShellApiExecResult<List<String>> result = adbShell.getPackageList(PackageFilterParam.disabled);
            if (!result.success) {
                showTipDialog("检查失败\n" + result.msg);
                return;
            }
            if (result.data == null || result.data.isEmpty()) {
                showTipDialog("检查结果为空");
                return;
            }

            for (String s : result.data) {
                ShellApiExecResult<Void> enabled = adbShell.setEnabled(s, true);
                if (!enabled.success) {
                    if (!showConfirmDialog("恢复 " + s + " 失败，是否继续？\n" + enabled.msg)) {
                        break;
                    }
                }
            }
        }

        startRegistration_recoveryHideApp();

        hideLoadingDialog();
        showTipDialog("操作完成！");
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
            String apkPath = appConfigService.getInstallApkPath();
            logger.info("install apk. initial file path: {}", apkPath);
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
            File file1 = fileChooser.showOpenDialog(JavaFXApplication.getMainStage());
            if (file1 != null) {
                installApkPath = file1.getAbsolutePath();
                appConfigService.setInstallApkPath(file1.getAbsolutePath());
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

        IOThread.run(new Runnable() {
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
        showLoadingDialog("正在操作中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    showLoadingDialog("正在操作中..." + i);
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingDialog();
                    }
                });
            }
        }).start();
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
                .append("\n设备ID：").append(SerialNumberUtil.getMachineCode())
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
        if (!Toolkit.getToolkit().isFxUserThread()) {
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
        loadingAlert.setContentText(text);
        loadingAlert.setDialogPane(JavaFXApplication.loadFXML("/fxml/alert_loading.fxml"));
        showAlert(loadingAlert);
    }

    private void hideLoadingDialog() {
        if (!Toolkit.getToolkit().isFxUserThread()) {
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
        IOThread.run(new Runnable() {
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

        if (!Toolkit.getToolkit().isFxUserThread()) {
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
        if (!Toolkit.getToolkit().isFxUserThread()) {
            runUIThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    showAlert(alert);
                    return null;
                }
            });
            return;
        }

        DragUtil.enableDialogDrag(alert);
        alert.initOwner(JavaFXApplication.getMainStage());
        alert.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == Boolean.TRUE) {
//                    logger.info("\n{}", DebugUtil.dump(alert.getDialogPane()));
                }
            }
        });
        alert.show();
    }

    private Optional<ButtonType> showAlertAndAwait(Alert alert) {
        if (!Toolkit.getToolkit().isFxUserThread()) {
            return runUIThread(new Callable<Optional<ButtonType>>() {
                @Override
                public Optional<ButtonType> call() throws Exception {
                    return showAlertAndAwait(alert);
                }
            });
        }

        DragUtil.enableDialogDrag(alert);
        alert.initOwner(JavaFXApplication.getMainStage());
        alert.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == Boolean.TRUE) {
//                    logger.info("\n{}", DebugUtil.dump(alert.getDialogPane()));
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

    private void startRefreshDeviceTask() {
        synchronized (object) {
            if (inRefreshing) return;
            inRefreshing = true;
            if (thread == null) {
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (inRefreshing)
                                onRefreshDeviceClick();
                            try {
                                Thread.sleep(config_refresh_device_interval_time);
                            } catch (InterruptedException e) {
                                logger.debug("refresh device thread interrupted");
                                break;
                            }
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    private void stopRefreshDeviceTask() {
        synchronized (object) {
            if (!inRefreshing) {
                return;
            }
            inRefreshing = false;
//            if (thread != null) {
//                thread.interrupt();
//                thread = null;
//            }
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
