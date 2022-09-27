package com.tangrun.mdm.boxwindow.shell.pojo;

public class ProfileOwner {
    Integer userId;
    ComponentName componentName;
    String pkgName;
    String name;

    public ProfileOwner(Integer userId, ComponentName componentName, String pkgName, String name) {
        this.userId = userId;
        this.componentName = componentName;
        this.pkgName = pkgName;
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public ProfileOwner setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public ProfileOwner setComponentName(ComponentName componentName) {
        this.componentName = componentName;
        return this;
    }

    public String getPkgName() {
        return pkgName;
    }

    public ProfileOwner setPkgName(String pkgName) {
        this.pkgName = pkgName;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProfileOwner setName(String name) {
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
