package com.tangrun.mdm.consolebox;

public class Config {
    String logPath;
    String installApkPath;
    String adminComponentPkgName;
    String adminComponentClsName;

    public String getLogPath() {
        return logPath;
    }

    public Config setLogPath(String logPath) {
        this.logPath = logPath;
        return this;
    }

    public String getInstallApkPath() {
        return installApkPath;
    }

    public Config setInstallApkPath(String installApkPath) {
        this.installApkPath = installApkPath;
        return this;
    }

    public String getAdminComponentPkgName() {
        return adminComponentPkgName;
    }

    public Config setAdminComponentPkgName(String adminComponentPkgName) {
        this.adminComponentPkgName = adminComponentPkgName;
        return this;
    }

    public String getAdminComponentClsName() {
        return adminComponentClsName;
    }

    public Config setAdminComponentClsName(String adminComponentClsName) {
        this.adminComponentClsName = adminComponentClsName;
        return this;
    }
}
