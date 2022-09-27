package com.tangrun.mdm.consolebox;


import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.tangrun.mdm.shell.core.*;
import com.tangrun.mdm.shell.enums.PackageFilterParam;
import com.tangrun.mdm.shell.impl.ShellApiCmdImpl;
import com.tangrun.mdm.shell.impl.ShellExecutorCmdImpl;
import com.tangrun.mdm.shell.pojo.*;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Box {
    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 1) {
            System.err.println("启动参数配置文件路径还未指定");
            return;
        }
        new Box(args[0]).run();
    }

    private final ShellApi adbShell;
    private ComponentName mComponentProfileOwner;
    private final Scanner mScanner;

    Config config;
    Writer logSteam = null;

    public Box(String configPath) throws IOException {
        File file = new File(configPath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("配置文件不存在" + file.getAbsolutePath());
            System.exit(-1);
        }
        config = new Gson().fromJson(com.google.common.io.Files.asCharSource(file, Charset.defaultCharset()).read(), Config.class);
        createLog();
        ShellApiCmdImpl adbShell1 = new ShellApiCmdImpl(new ShellExecutorCmdImpl());
        adbShell1.setInterceptor(new ShellInterceptor() {
            @Override
            public ShellExecResult execute(String command, ShellExecutor executor) {
                ShellExecResult result = executor.execute(command);

                long time = System.currentTimeMillis();
                if (logSteam != null) {
                    time = System.currentTimeMillis() - time;
                    try {
                        logSteam.write(String.format(Locale.getDefault(),"AdbShell [%s]: 耗时%dms" +
                                        "\n\texecute: " +
                                        "\n\t\t%s, " +
                                        "\n\tresult: " +
                                        "\n\t\texistValue: %s" +
                                        "\n\t\tout: %s" +
                                        "\n\t\terror: %s" +
                                        "\n",
                                DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())),
                                time,
                                command,
                                result.exitValue, result.out, result.error
                        ));
                        logSteam.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return result;
            }
        });
        adbShell = adbShell1;
        mComponentProfileOwner = new ComponentName(config.adminComponentPkgName, config.adminComponentClsName);
        mScanner = new Scanner(System.in);
    }

    void createLog() throws IOException {
        if (Strings.isNullOrEmpty(config.logPath)) return;
        File file = new File(config.logPath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                System.err.println("日志文件目录创建失败" + file.getAbsolutePath());
                System.exit(-1);
            }
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                System.err.println("日志文件创建失败" + file.getAbsolutePath());
                System.exit(-1);
            }
        }
        logSteam = new BufferedWriter(new FileWriter(file, true));
    }

    void run() {
        printMenu();
        boolean exit = false;
        while (!exit) {
            String next = mScanner.next();
            switch (next) {
                case "0": {
                    exit = true;
                    break;
                }
                case "clear":
                case "c": {
                    try {
                        System.out.write("cls".getBytes("gbk"));
                        System.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "help":
                case "h": {
                    printMenu();
                    break;
                }
                case "999": {
                    ShellApiExecResult<Void> voidShellApiExecResult = startRegistration_();
                    if (voidShellApiExecResult.success) {
                        console("激活成功");
                    } else {
                        console("激活失败\n{}", voidShellApiExecResult.msg);
                    }
                    console("=========complete========");
                    break;
                }
                case "1": {
                    ShellApiExecResult<List<Device>> deviceList = adbShell.getDeviceList();
                    if (deviceList.success) {
                        for (Device datum : deviceList.data) {
                            console("{}", datum);
                        }
                        console("设备列表为空，请到开发者模式下打开USB调试");
                        console("\t华为 设置->关于手机->版本号    设置=>系统和更新=>开发人员选项");
                        console("\t小米 设置=>我的设备=>全部参数=>MIUI 版本    设置=>更多设置=>开发者选项");
                        console("\toppo 设置=>关于手机=>版本信息=>版本号    设置=>其他设置=>开发者选项");
                        console("\tvivo 设置=>关于手机=>软件版本号    设置=>系统管理=>开发者选项");
                        console("\t三星 设置=>关于手机=>版本号    设置=>系统和更新=>开发人员选项");
                    } else {
                        console(deviceList.msg);
                    }
                    console("=========complete========");
                    break;
                }
                case "2": {
                    ShellApiExecResult<ProfileOwner> profileOwner = adbShell.getProfileOwner();
                    if (profileOwner.success) {
                        if (profileOwner.data == null) {
                            console("当前设备还未设置ProfileOwner");
                        } else
                            console("当前设备ProfileOwner信息: {}", profileOwner.data.toString());
                    } else console(profileOwner.msg);
                    console("=========complete========");
                    break;
                }
                case "3": {
                    ShellApiExecResult<List<String>> packageList = adbShell.getPackageList(PackageFilterParam.disabled);
                    if (packageList.success) {
                        for (String datum : packageList.data) {
                            ShellApiExecResult<Void> voidShellApiExecResult = adbShell.setEnabled(datum, true);
                            console("恢复应用：{}，{}", datum, voidShellApiExecResult.success);
                        }
                    } else console(packageList.msg);
                    console("=========complete========");
                    break;
                }
                case "4": {
                    console(mComponentProfileOwner.toString());
                    console("=========complete========");
                    break;
                }
                case "5": {
                    setProfileOwnerComponent();
                    console("=========complete========");
                    break;
                }
                case "6": {
                    adbShell.startDeviceRegistrationAction();
                    console("=========complete========");
                    break;
                }
                case "7": {
                    File file = new File(config.installApkPath);
                    if (file.exists() && file.isFile()) {
                        adbShell.installApp(config.installApkPath);
                    } else {
                        console("apk路径不正确，文件不存在");
                    }
                    console("=========complete========");
                    break;
                }
                case "8": {
//                    adb shell dpm remove-active-admin com.tangrun.safe/.dpm.DeviceAdminReceiver
                    adbShell.removeActiveAdmin(mComponentProfileOwner.packageName, mComponentProfileOwner.className);
                    console("=========complete========");
                }
                default:
                    printMenu();
                    break;
            }
        }
    }

    void printMenu() {
        System.out.println("this is menu!!!!!");
        System.out.println("    1 设备列表");
        System.out.println("    2 当前设备ProfileOwner信息");
        System.out.println("    3 恢复所有隐藏应用");
        System.out.println("    4 查看当前ProfileOwner Component");
        System.out.println("    5 设置ProfileOwner Component");
        System.out.println("    6 send action");
        System.out.println("    7 安装最新版app");
        System.out.println("    8 取消ProfileOwner");
        System.out.println("    0 退出");
        System.out.println("    999 开始激活");
        System.out.println("    help/h 查看命令");
//        System.out.println("    clear/c 清空内容");

    }

    void setProfileOwnerComponent() {
        System.out.println("请输入内容，packageName/classname 例如 com.xxx.xxx/.xxx");
        String component = mScanner.next();
        String[] split = component.split("/");
        if (split.length != 2) {
            System.out.println("输入内容错误");
            return;
        }
        mComponentProfileOwner = new ComponentName(split[0], split[1]);
    }

    private ComponentName getProfileOwnerComponent() {
        return mComponentProfileOwner;
    }

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
        ShellApiExecResult<Void> setOwnerResult = adbShell.setProfileOwner(getProfileOwnerComponent());
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


    private boolean showSyncBlockConfirmDialog(String title, String msg) {

        console("========{}========", title);
        console(msg);
        console("是否继续？【y/n】");
        String next = mScanner.next();
        return next.equals("y");
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

    private List<String> getEqualString(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();
        for (String s : list1) {
            if (list2.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }

    class AccountAppInfo {
        String name;
        String type;
        String packageName;
        String className;
        Integer uid;
        Boolean hasAccount = false;
        Boolean hasService = false;
        Boolean hasPackage = false;

        @Override
        public String toString() {
            return "AccountAppInfo{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", className='" + className + '\'' +
                    ", uid=" + uid +
                    ", hasAccount=" + hasAccount +
                    ", hasService=" + hasService +
                    ", hasPackage=" + hasPackage +
                    '}';
        }
    }


    // region 打印
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

    // endregion


}
