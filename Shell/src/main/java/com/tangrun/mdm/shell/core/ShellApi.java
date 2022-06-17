package com.tangrun.mdm.shell.core;

import com.tangrun.mdm.shell.enums.PackageFilterParam;
import com.tangrun.mdm.shell.pojo.*;

import java.util.*;

public interface ShellApi {

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
    ShellApiExecResult<ProfileOwner> getProfileOwner();

    /**
     * 设置profile owner
     * @param componentName
     * @return
     */
    ShellApiExecResult<Void> setProfileOwner(ComponentName componentName);

    /**
     * 设置应用 禁用/使用
     * @param packageName
     * @param enable
     * @return
     */
    ShellApiExecResult<Void> setEnabled(String packageName, boolean enable);

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

}
