package com.tangrun.mdm.boxwindow.shell.pojo;

public class AdminOwnerInfo {
    Integer userId;
    ComponentName componentName;
    String pkgName;
    String name;

    public AdminOwnerInfo(Integer userId, ComponentName componentName, String pkgName, String name) {
        this.userId = userId;
        this.componentName = componentName;
        this.pkgName = pkgName;
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public AdminOwnerInfo setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public AdminOwnerInfo setComponentName(ComponentName componentName) {
        this.componentName = componentName;
        return this;
    }

    public String getPkgName() {
        return pkgName;
    }

    public AdminOwnerInfo setPkgName(String pkgName) {
        this.pkgName = pkgName;
        return this;
    }

    public String getName() {
        return name;
    }

    public AdminOwnerInfo setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "\"userId\":" + userId +
                ", \"componentName\":" + componentName +
                ", \"pkgName\":\"" + pkgName + "\"" +
                ", \"name\":\"" + name + "\"" +
                '}';
    }
}
