package com.tangrun.mdm.shell.pojo;

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
