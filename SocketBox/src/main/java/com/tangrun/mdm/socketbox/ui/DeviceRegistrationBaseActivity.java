package com.tangrun.mdm.socketbox.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import com.tangrun.mdm.shell.core.ShellApi;
import com.tangrun.mdm.shell.core.ShellApiExecResult;
import com.tangrun.mdm.shell.enums.PackageFilterParam;
import com.tangrun.mdm.shell.pojo.*;
import com.tangrun.mdm.socketbox.impl.ShellApiAndroidSocketImpl;
import com.tangrun.mdm.socketbox.impl.ShellExecutorAndroidSocketImpl;

import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author RainTang
 * @description:
 * @date :2021/12/8 8:50
 */
public abstract class DeviceRegistrationBaseActivity extends AppCompatActivity {

    protected String RESULT_fail_shell_error = "操作失败，请重新连接设备进行激活";
    protected String RESULT_fail_multi_user = "激活失败，请关闭系统分身/应用双开等多开功能后再试";
    protected String RESULT_fail_xiaomi_no_manager_device_admins_permisiion = "激活失败，小米用户请手动在系统设置=>开发者设置=>开启“USB 调试（安全设置）”，如仍不可以请关闭“MIUI 优化”";
    protected String RESULT_fail_multi_account = "激活失败，还有账户存在，请手动移除账号";
    protected String RESULT_fail_has_other_app_set = "激活失败，已有其他软件被激活";

    protected String TIP_activating = "您的设备正在激活中，请勿重复操作！";
    protected String TIP_activated = "您的设备已激活，请勿重复操作！";
    protected ShellApi adbShell;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adbShell = new ShellApiAndroidSocketImpl(new OutputStreamWriter(System.out),new ShellExecutorAndroidSocketImpl());

        String flg = getIntent().getStringExtra("flg");
        String msg = getIntent().getStringExtra("msg");
        if (!TextUtils.isEmpty(flg)){
            onBoxMsg(flg, msg);
        }
    }

    /**
     * 激活结束 结果回调
     * @param success
     * @param result
     */
    protected abstract void onRegistrationResult(boolean success, String result);

    /**
     *
     */
    protected abstract void onRemoveAdmin();

    protected abstract void onOpenDebugMode();

    protected abstract void onBoxMsg(String flag, String msg);

    protected abstract boolean isOwner();

    protected void onConsole(String line) {
    }

    protected abstract ComponentName getProfileOwnerComponent();

    protected AtomicBoolean inRegistration = new AtomicBoolean();

    public void startRegistration() {
        if (inRegistration.get()) {
            showInfoDialog(null, TIP_activating, null);
            return;
        }
        if (isOwner()) {
            showInfoDialog(null, TIP_activated, null);
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    inRegistration.set(true);
                    showLoading("激活中");
                    ShellApiExecResult<Void> voidShellApiExecResult = startRegistration_();
                    inRegistration.set(false);
                    hideLoading();
                    if (voidShellApiExecResult.success) {
                        showThreadSafetyInfoDialog(null, "激活成功", null);
                    } else {
                        showThreadSafetyInfoDialog("激活失败", voidShellApiExecResult.msg, null);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRegistrationResult(voidShellApiExecResult.success, voidShellApiExecResult.msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    inRegistration.set(false);
                    hideLoading();
                    showThreadSafetyInfoDialog("激活失败", "出现错误：" + e.getMessage(), null);
                }
            }
        }.start();
    }

    protected void recoveryApp() {
        showLoading("恢复中...");
        new Thread() {
            @Override
            public void run() {
                try {
                    ShellApiExecResult<List<String>> packageList = adbShell.getPackageList(PackageFilterParam.disabled);
                    if (!packageList.success) {
                        throw new RuntimeException(RESULT_fail_shell_error);
                    }
                    int size = packageList.data.size();
                    int success = 0;
                    for (String datum : packageList.data) {
                        ShellApiExecResult<Void> voidShellApiExecResult = adbShell.setEnabled(datum, true);
                        console("恢复应用：{}，{}", datum, voidShellApiExecResult.success);
                        if (voidShellApiExecResult.success)success++;
                    }
                    hideLoading();
                    showThreadSafetyInfoDialog(null,size ==0?"没有需要恢复的应用":"恢复完成" , null);
                } catch (Exception e) {
                    hideLoading();
                    showThreadSafetyInfoDialog("恢复异常", "恢复失败：" + e.getMessage(), null);
                }
            }
        }.start();
    }


    @WorkerThread
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
                stringBuilder.append("\n发现有系统分身/应用双开，请提前备份其数据！");
                if (!showSyncBlockConfirmDialog("系统提示", stringBuilder.toString())) {
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

        return success ? ShellApiExecResult.success(null) : ShellApiExecResult.fail(errorReturn.toString());
    }


    // region 连点退出
    boolean debug;
    long continuityClickLastTime;
    int continuityClickMinIntervalTime = 500;
    int continuityClickNum;
    int continuityClickMaxNum = 7;

    /**
     * 设置debug控件 长按打开调试 连点取消激活
     * @param view
     */
    public void setSuperView(View view) {
        view.setOnLongClickListener(v -> {
            Log.d("TAG", "setSuperView: "+continuityClickNum +"  "+continuityClickLastTime+"  "+SystemClock.elapsedRealtime() +debug);
            if (debug) return false;
            else {
                debug = true;
                onOpenDebugMode();
                return true;
            }
        });
        view.setOnClickListener(v -> {
            Log.d("TAG", "setSuperView: "+continuityClickNum +"  "+continuityClickLastTime+"  "+SystemClock.elapsedRealtime());
            if (SystemClock.elapsedRealtime() - continuityClickLastTime > continuityClickMinIntervalTime) {
                continuityClickNum = 1;
            } else {
                continuityClickNum++;
            }
            continuityClickLastTime = SystemClock.elapsedRealtime();

            if (continuityClickNum > continuityClickMaxNum) {
                onRemoveAdmin();
                continuityClickNum = 0;
            }
        });
    }
    // endregion


    // region adb shell 获取主用户的账号app列表


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

    private static class AccountAppInfo {
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

    // endregion

    private boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
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
        Log.d("TAG", parse);
        if (debug)runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConsole(parse);
            }
        });
    }

    // endregion

    // region 窗口
    private ProgressDialog progressDialog;

    protected void showLoading(String msg) {
        if (!isMainThread()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLoading(msg);
                }
            });
            return;
        }
        if (progressDialog == null)
            progressDialog = ProgressDialog.show(this, null, msg, false, false);
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    protected void hideLoading() {
        if (!isMainThread()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideLoading();
                }
            });
            return;
        }
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    /**
     * 线程阻塞 等待用户操作后继续
     *
     * @param title
     * @param msg
     * @return true/false 确定/取消
     */
    protected boolean showSyncBlockConfirmDialog(String title, String msg) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showConfirmDialog(title, msg, new Runnable() {
                    @Override
                    public void run() {
                        atomicBoolean.set(true);
                        countDownLatch.countDown();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        atomicBoolean.set(false);
                        countDownLatch.countDown();
                    }
                });
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return atomicBoolean.get();
    }

    protected void showThreadSafetyInfoDialog(String title, String msg, Runnable confirm) {
        if (!isMainThread()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfoDialog(title, msg, confirm);
                }
            });
        } else showInfoDialog(title, msg, confirm);
    }

    /**
     * 确定/取消 dialog
     *
     * @param title
     * @param msg
     * @param sure
     * @param cancel
     */
    protected void showConfirmDialog(String title, String msg, Runnable sure, Runnable cancel) {
        new  AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    if (sure != null)
                        sure.run();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    if (cancel != null)
                        cancel.run();
                })
                .show();
    }

    /**
     * 单 确定 dialog
     *
     * @param title
     * @param msg
     * @param confirm
     */
    protected void showInfoDialog(String title, String msg, Runnable confirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    if (confirm != null)
                        confirm.run();
                })
                .show();
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // endregion
}
