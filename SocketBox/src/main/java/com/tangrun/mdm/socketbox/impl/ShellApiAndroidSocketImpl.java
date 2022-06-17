package com.tangrun.mdm.socketbox.impl;

import com.tangrun.mdm.shell.core.ShellApi;
import com.tangrun.mdm.shell.core.ShellApiExecResult;
import com.tangrun.mdm.shell.core.ShellExecResult;
import com.tangrun.mdm.shell.core.ShellExecutor;
import com.tangrun.mdm.shell.enums.PackageFilterParam;
import com.tangrun.mdm.shell.pojo.*;
import com.tangrun.util.Function;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellApiAndroidSocketImpl implements ShellApi {
    ShellExecutor shellExecutor;
    Writer log;

    public ShellApiAndroidSocketImpl(Writer debug, ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
        log = debug;
    }

    public ShellApiExecResult<Void> installApp(String path) {
        return shellApply("pm install " + path, new Function<ShellExecResult, ShellApiExecResult<Void>>() {
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
        return shellApply("am start -a com.cdblue.DeviceRegistration", new Function<ShellExecResult, ShellApiExecResult<Void>>() {
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
        return shellApply("pm remove-user " + userId, new Function<ShellExecResult, ShellApiExecResult<Void>>() {
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
        return shellApply("dpm remove-active-admin " + pkgName+"/"+className, new Function<ShellExecResult, ShellApiExecResult<Boolean>>() {
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
            "Profile Owner \\(User (.+?)\\):\\s+?admin=ComponentInfo\\{(.+?)\\/(.+?)\\}\\s+?name=(\\w*?)\\s+?package=(\\S+)");

    public ShellApiExecResult<ProfileOwner> getProfileOwner() {
        return shellApply("dumpsys device_policy", new Function<ShellExecResult, ShellApiExecResult<ProfileOwner>>() {
            @Override
            public ShellApiExecResult<ProfileOwner> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    Matcher matcher = patternDevicePolicyProfileOwner.matcher(shellAdbShellExecResult.out);
                    if (matcher.find()) {
                        Integer userId = Integer.parseInt(matcher.group(1));
                        String cPkgName = matcher.group(2);
                        String cClsName = matcher.group(3);
                        String name = matcher.group(4);
                        String pkgName = matcher.group(5);
                        return ShellApiExecResult.success(new ProfileOwner(userId, new ComponentName(cPkgName, cClsName), pkgName, name));
                    }
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }


    public ShellApiExecResult<Void> setProfileOwner(ComponentName componentName) {
        return shellApply(String.format("dpm set-profile-owner %s/%s", componentName.packageName, componentName.className), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
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
        return shellApply(String.format("pm %s %s", enable ? "enable" : "disable-user", packageName), new Function<ShellExecResult, ShellApiExecResult<Void>>() {
            @Override
            public ShellApiExecResult<Void> apply(ShellExecResult shellAdbShellExecResult) {
                if (shellAdbShellExecResult.existOk()) {
                    return ShellApiExecResult.success(null);
                }
                return ShellApiExecResult.fail(shellAdbShellExecResult.error);
            }
        });
    }


    Pattern patternPackage = Pattern.compile("package:(((?!package:).)+)");

    public ShellApiExecResult<List<String>> getPackageList(PackageFilterParam... filters) {
        StringBuilder stringBuilder = new StringBuilder("pm list package");
        if (filters != null) {
            for (PackageFilterParam filter : filters) {
                stringBuilder.append(" ")
                        .append(filter.value);
            }
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


    Pattern patternDumpUserAccount = Pattern.compile("User UserInfo\\{(.+?):(.+?):(.+?)\\}(((?!User UserInfo)[\\s\\S])+)");
    Pattern patternAccountInfo = Pattern.compile("Account \\{name=(.+?), type=(.+?)\\}");
    Pattern patternServiceInfo = Pattern.compile("ServiceInfo.+?\\{type=(.+?)\\}.+?ComponentInfo\\{(.+?)\\/(.+?)\\}.+?uid ([\\d]+)");

    public ShellApiExecResult<List<UserAccounts>> getUserAccounts() {
        return shellApply("dumpsys account", new Function<ShellExecResult, ShellApiExecResult<List<UserAccounts>>>() {
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


    Pattern patternUserList = Pattern.compile("UserInfo\\{(.+?):(.+?):(.+?)\\}");

    public ShellApiExecResult<List<UserInfo>> getUserList() {
        return shellApply("pm list users", new Function<ShellExecResult, ShellApiExecResult<List<UserInfo>>>() {
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

    public ShellApiExecResult<List<Device>> getDeviceList() {
        List<Device> list = new ArrayList<>();
        Device device1 = new Device();
        device1.id="";
        device1.state = Device.DeviceState.device;
        list.add(device1);
        return ShellApiExecResult.success(list);
    }

    <T> T shellApply(String command, Function<ShellExecResult, T> function) {
        long time = System.currentTimeMillis();
        ShellExecResult execute = shellExecutor.execute(command);
        if (log != null) {
            time = System.currentTimeMillis() - time;
            try {
                log.write(String.format("SocketShell [%s]: 耗时%dms" +
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
                        execute.exitValue,execute.out,execute.error
                ));
                log.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return function.apply(execute);
    }
}
