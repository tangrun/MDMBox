package com.tangrun.mdm.boxwindow.shell;

import com.tangrun.mdm.boxwindow.shell.core.*;
import com.tangrun.mdm.boxwindow.shell.pojo.*;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellApiImpl implements ShellApi {

    ShellExecutor shellExecutor;

    public ShellApiImpl(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    Pattern patternProps = Pattern.compile("\\[([\\S\\s]+?)\\]: \\[([\\S\\s]+?)\\]");

    @Override
    public ShellApiExecResult<Map<String, String>> getProps() {
        return shellApply("adb shell getprop", new Function<ShellExecResult, ShellApiExecResult<Map<String, String>>>() {
            @Override
            public ShellApiExecResult<Map<String, String>> apply(ShellExecResult var1) {
                if (var1.existOk()) {
                    Matcher matcher = patternProps.matcher(var1.out);
                    Map<String, String> map = new HashMap<>();
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        map.put(key, value);
                    }
                    return ShellApiExecResult.success(map);
                } else return ShellApiExecResult.fail(var1.error);
            }
        });
    }

    public ShellApiExecResult<Void> installApp(String path) {
        return shellApply("adb install " + path, new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellExecResult) {
                if (shellExecResult.existOk()) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> startDeviceRegistrationAction() {
        return shellApply("adb shell am start -a com.cdblue.DeviceRegistration", new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellExecResult) {
                if (shellExecResult.existOk()) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> removeUser(Integer userId) {
        return shellApply("adb shell pm remove-user " + userId, new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Boolean> removeActiveAdmin(String pkgName, String className) {
        //Success: Admin removed ComponentInfo{com.tangrun.safe/com.tangrun.safe.dpm.DeviceAdminReceiver}
        return shellApply("adb shell dpm remove-active-admin " + pkgName + "/" + className, new Function<ShellExecResult, ShellApiExecResult<Boolean>>() {
            @Override
            public ShellApiExecResult<Boolean> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    return ShellApiExecResult.success(shellAdbShellExecResult.out.contains("Success"));
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    Pattern patternDevicePolicyProfileOwner = Pattern.compile(
//            "Profile Owner \\(User (.+?)\\):\\s+?admin=ComponentInfo\\{(.+?)\\/(.+?)\\}\\s+?name=(\\w*?)\\s+?package=(\\S+)");
            "Profile Owner \\(User (.+?)\\):\\s+?admin=ComponentInfo\\{(.+?)\\/(.+?)\\}");

    public ShellApiExecResult<AdminOwnerInfo> getProfileOwner() {
        return shellApply("adb shell dumpsys device_policy", new Function<ShellExecResult, ShellApiExecResult<AdminOwnerInfo>>() {
            @Override
            public ShellApiExecResult<AdminOwnerInfo> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    Matcher matcher = patternDevicePolicyProfileOwner.matcher(shellAdbShellExecResult.out);
                    if (matcher.find()) {
                        Integer userId = Integer.parseInt(matcher.group(1));
                        String cPkgName = matcher.group(2);
                        String cClsName = matcher.group(3);
//                        String name = matcher.group(4);
//                        String pkgName = matcher.group(5);
//                        return ShellApiExecResult.success(new AdminOwnerInfo(userId, new ComponentName(cPkgName, cClsName), pkgName, name));
                        return ShellApiExecResult.success(new AdminOwnerInfo(userId, new ComponentName(cPkgName, cClsName), null, null));
                    }
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    /**
     *   Device Owner:
     *     admin=ComponentInfo{com.cdblue.awlm/com.cdblue.awlm.emm.receiver.DeviceManageBC}
     *     name=
     *     package=com.cdblue.awlm
     *     canAccessDeviceIds=true
     *     User ID: 0
     */
    Pattern patternDevicePolicyDeviceOwner = Pattern.compile(
            "Device Owner[\\S\\s]+?admin=ComponentInfo\\{(.+?)\\/(.+?)\\}\\s+?name=(\\w*?)\\s+?package=(\\S+)");

    public ShellApiExecResult<AdminOwnerInfo> getDeviceOwner() {
        return shellApply("adb shell dumpsys device_policy", new Function<ShellExecResult, ShellApiExecResult<AdminOwnerInfo>>() {
            @Override
            public ShellApiExecResult<AdminOwnerInfo> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    Matcher matcher = patternDevicePolicyDeviceOwner.matcher(shellAdbShellExecResult.out);
                    if (matcher.find()) {
                        String cPkgName = matcher.group(1);
                        String cClsName = matcher.group(2);
                        String name = matcher.group(3);
                        String pkgName = matcher.group(4);
                        return ShellApiExecResult.success(new AdminOwnerInfo(null, new ComponentName(cPkgName, cClsName), pkgName, name));
                    }
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> setDeviceOwner(ComponentName componentName) {
        return shellApply(String.format("adb shell dpm set-device-owner %s/%s", componentName.packageName, componentName.className), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                System.out.println("set device owner: "+shellAdbShellExecResult.out);
                if (shellAdbShellExecResult.existOk()) {
                    ShellApiExecResult<Void> result = ShellApiExecResult.success(null);
                    result.msg = "".equals(shellAdbShellExecResult.error) ? shellAdbShellExecResult.out : shellAdbShellExecResult.error;
                    return result;
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> setDeviceAdmin(ComponentName componentName) {
        return shellApply(String.format("adb shell dpm set-active-admin %s/%s", componentName.packageName, componentName.className), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                System.out.println("set profile owner: "+shellAdbShellExecResult.out+" \n "+shellAdbShellExecResult.error);
                if (shellAdbShellExecResult.existOk()) {
                    ShellApiExecResult<Void> result = ShellApiExecResult.success(null);
                    result.msg = "".equals(shellAdbShellExecResult.error) ? shellAdbShellExecResult.out : shellAdbShellExecResult.error;
                    return result;
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }
    public ShellApiExecResult<Void> setProfileOwner(ComponentName componentName) {
        return shellApply(String.format("adb shell dpm set-profile-owner %s/%s", componentName.packageName, componentName.className), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                System.out.println("set profile owner: "+shellAdbShellExecResult.out+" \n "+shellAdbShellExecResult.error);
                if (shellAdbShellExecResult.existOk()) {
                    ShellApiExecResult<Void> result = ShellApiExecResult.success(null);
                    result.msg = "".equals(shellAdbShellExecResult.error) ? shellAdbShellExecResult.out : shellAdbShellExecResult.error;
                    return result;
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> setEnabled(String packageName, boolean enable) {
        String s = enable ? "enable" : "disable-user";
        String s1 = enable ? "enabled" : "disabled-user";
        return shellApply(String.format("adb shell pm %s %s", s, packageName), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                System.out.println(packageName+" "+enable+" "+shellAdbShellExecResult.out+" | "+shellAdbShellExecResult.error);
                if (shellAdbShellExecResult.existOk() && shellAdbShellExecResult.out.contains(s1)) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    public ShellApiExecResult<Void> setAppHide(String packageOrClassName, boolean hide) {
        return shellApply(String.format("adb shell pm %s %s", hide ? "hide" : "unhide", packageOrClassName), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    String shell_package_list = "adb shell pm list package";
    Pattern patternPackage = Pattern.compile("package:(((?!package:).)+)");

    public ShellApiExecResult<List<String>> getPackageList(String userId, PackageFilterParam... filters) {
        StringBuilder stringBuilder = new StringBuilder(shell_package_list);
        if (filters != null) {
            for (PackageFilterParam filter : filters) {
                stringBuilder.append(" ")
                        .append(filter.value);
            }
        }
        if (userId != null && userId.trim().length() != 0){
            stringBuilder.append(" --user ").append(userId.trim());
        }
        return shellApply(stringBuilder.toString(), new Function<ShellExecResult, ShellApiExecResult<List<String>>>() {
            @Override
            public ShellApiExecResult<List<String>> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    Matcher matcher = patternPackage.matcher(shellAdbShellExecResult.out);
                    Set<String> list = new HashSet<>();
                    while (matcher.find()) {
                        String packageName = matcher.group(1);
                        list.add(packageName);
                    }
                    List<String> data = new ArrayList<>(list);
                    return ShellApiExecResult.success(data);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }


    String shell_dumpsys_account = "adb shell dumpsys account";
    Pattern patternDumpUserAccount = Pattern.compile("User UserInfo\\{(.+?):(.+?):(.+?)\\}(((?!User UserInfo)[\\s\\S])+)");
    Pattern patternAccountInfo = Pattern.compile("Account \\{name=(.+?), type=(.+?)\\}");
    Pattern patternServiceInfo = Pattern.compile("ServiceInfo.+?\\{type=(.+?)\\}.+?ComponentInfo\\{(.+?)\\/(.+?)\\}.+?uid ([\\d]+)");

    public ShellApiExecResult<List<UserAccounts>> getUserAccounts() {
        return shellApply(shell_dumpsys_account, new Function<ShellExecResult, ShellApiExecResult<List<UserAccounts>>>() {
            @Override
            public ShellApiExecResult<List<UserAccounts>> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    List<UserAccounts> list = new ArrayList<>();

                    Matcher matcher = patternDumpUserAccount.matcher(shellAdbShellExecResult.out);
                    while (matcher.find()) {
                        String id = matcher.group(1);
                        String name = matcher.group(2);
                        String flags = matcher.group(3);
                        String contentStr = matcher.group(4);

                        UserAccounts userAccounts = new UserAccounts();

                        userAccounts.userInfo = new UserInfo(id, name, flags);

                        Matcher matcherAccounts = patternAccountInfo.matcher(contentStr);
                        while (matcherAccounts.find()) {
                            String accountName = matcherAccounts.group(1);
                            String accountType = matcherAccounts.group(2);
                            userAccounts.accountList.add(new Account(accountName, accountType));
                        }

                        Matcher matcherServices = patternServiceInfo.matcher(contentStr);
                        while (matcherServices.find()) {
                            String serviceType = matcherServices.group(1);
                            String servicePackage = matcherServices.group(2);
                            String serviceClass = matcherServices.group(3);
                            String serviceUid = matcherServices.group(4);
                            userAccounts.serviceInfoList.add(new ServiceInfo(serviceType, servicePackage, serviceClass, serviceUid));
                        }

                        list.add(userAccounts);
                    }

                    return ShellApiExecResult.success(list);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }


    String shell_user_list = "adb shell pm list users";
    Pattern patternUserList = Pattern.compile("UserInfo\\{(.+?):(.+?):(.+?)\\}");

    public ShellApiExecResult<List<UserInfo>> getUserList() {
        return shellApply(shell_user_list, new Function<ShellExecResult, ShellApiExecResult<List<UserInfo>>>() {
            @Override
            public ShellApiExecResult<List<UserInfo>> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    List<UserInfo> list = new ArrayList<>();
                    Matcher matcher = patternUserList.matcher(shellAdbShellExecResult.out);
                    while (matcher.find()) {
                        list.add(new UserInfo(matcher.group(1), matcher.group(2), matcher.group(3)));
                    }
                    return ShellApiExecResult.success(list);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    String shell_adb_device_list = "adb devices";
    Pattern patternAdbDeviceList = Pattern.compile("(.+?)[\\s]+?(device|unauthorized|offline|unknown)[\\s]+");

    @Override
    public ShellApiExecResult<List<Device>> getDeviceList() {
        return shellApply(shell_adb_device_list, new Function<ShellExecResult, ShellApiExecResult<List<Device>>>() {
            @Override
            public ShellApiExecResult<List<Device>> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    List<Device> list = new ArrayList<>();
                    Matcher matcher = patternAdbDeviceList.matcher(shellAdbShellExecResult.out);
                    while (matcher.find()) {
                        Device device = new Device();
                        device.id = matcher.group(1);
                        device.state = Device.DeviceState.valueOf(matcher.group(2));
                        list.add(device);
                    }
                    return ShellApiExecResult.success(list);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }

    @Override
    public ShellApiExecResult<Void> reboot() {
        return shellApply("adb reboot", new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellExecResult) {
                return shellExecResult.existOk()?ShellApiExecResult.success(null):ShellApiExecResult.fail(shellExecResult.error);
            }
        });
    }

    <T> T shellApply(String command, Function<ShellExecResult, T> function) {
        ShellExecResult result;
        if (interceptor != null) {
            result = interceptor.execute(command, shellExecutor);
        } else {
            result = shellExecutor.execute(command);
        }
        return function.apply(result);
    }

    ShellInterceptor interceptor;

    public void setInterceptor(ShellInterceptor interceptor) {
        this.interceptor = interceptor;
    }
}
