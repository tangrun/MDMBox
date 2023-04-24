package com.tangrun.mdm.boxwindow.shell.core;

import com.tangrun.mdm.boxwindow.shell.pojo.*;

import java.util.List;
import java.util.Map;

public interface ShellApi {

    ShellApiExecResult<Map<String,String>> getProps();

    /**
     * 安装apk
     * @param path
     * @return
     */
    ShellApiExecResult<Void> installApp(String path);

    /**
     * 发送action 打开激活页面
     * @return
     */
    ShellApiExecResult<Void> startDeviceRegistrationAction();

    /**
     * 移除手机用户
     * @param userId
     * @return
     */
    ShellApiExecResult<Void> removeUser(Integer userId);

    /**
     * 移除设备admin
     * @param pkgName
     * @param className
     * @return
     */
    ShellApiExecResult<Boolean> removeActiveAdmin(String pkgName, String className);

    /**
     * 获得当前profile owner
     * @return
     */
    ShellApiExecResult<AdminOwnerInfo> getProfileOwner();
    ShellApiExecResult<AdminOwnerInfo> getDeviceOwner();

    /**
     * 设置profile owner
     * @param componentName
     * @return
     */
    ShellApiExecResult<Void> setDeviceAdmin(ComponentName componentName);
    ShellApiExecResult<Void> setProfileOwner(ComponentName componentName);

    ShellApiExecResult<Void> setDeviceOwner(ComponentName componentName);

    /**
     * 设置应用 禁用/使用
     * @param packageName
     * @param enable
     * @return
     */
    ShellApiExecResult<Void> setEnabled(String packageName, boolean enable);

    ShellApiExecResult<Void> setAppHide(String packageName, boolean hide);

    /**
     * 获取应用包名列表
     * @param filters
     * @return
     */
    ShellApiExecResult<List<String>> getPackageList(PackageFilterParam... filters);

    /**
     * 获取手机所有用户下的账户列表
     * @return
     */
    ShellApiExecResult<List<UserAccounts>> getUserAccounts();

    /**
     * 获取手机所有用户列表
     * @return
     */
    ShellApiExecResult<List<UserInfo>> getUserList();

    /**
     * 获取设备列表
     * @return
     */
    ShellApiExecResult<List<Device>> getDeviceList();

    ShellApiExecResult<Void> reboot();

}
