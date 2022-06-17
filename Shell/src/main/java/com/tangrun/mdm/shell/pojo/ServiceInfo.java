package com.tangrun.mdm.shell.pojo;

/**
 * ServiceInfo: AuthenticatorDescription {type=com.xunmeng.pinduoduo.tide_account_type}, ComponentInfo{com.xunmeng.pinduoduo/com.xunmeng.pinduoduo.service.UserAuthService}, uid 10278
 */
public class ServiceInfo {
    public String type;
    public  ComponentName componentName;
    public Integer uid;

    public ServiceInfo(String type, String packageName,String className, String uid) {
        this.type = type;
        this.componentName = new ComponentName(packageName,className);
        this.uid = Integer.parseInt(uid);
    }

    @Override
    public String toString() {
        return "{" +
                "\"type\":\"" + type + "\"" +
                ", \"componentName\":" + componentName +
                ", \"uid\":" + uid +
                '}';
    }
}
