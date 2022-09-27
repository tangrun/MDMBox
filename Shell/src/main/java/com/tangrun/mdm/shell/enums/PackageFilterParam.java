package com.tangrun.mdm.shell.enums;

public enum PackageFilterParam {
    /**
     * 只显示 disabled 的应用
     */
    disabled("-d"),
    /**
     * 只显示 enabled 的应用
     */
    enabled("-e"),
    /**
     * 只显示系统应用
     */
    system("-s"),
    /**
     * 只显示第三方应用
     */
    user("-3"),
    /**
     * 包含已卸载应用
     */
    uninstalled("-u"),
    /**
     * 显示应用的 installer
     */
    installer("-i"),
    /**
     * 显示应用关联的 apk 文件
     */
    apk("-f"),
    ;
    public final String value;

    PackageFilterParam(String value) {
        this.value = value;
    }
}
