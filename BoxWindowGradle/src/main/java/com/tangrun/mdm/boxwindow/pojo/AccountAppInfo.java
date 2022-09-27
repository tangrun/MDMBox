package com.tangrun.mdm.boxwindow.pojo;

public class AccountAppInfo {
    public String name;
    public String type;
    public String packageName;
    public String className;
    public Integer uid;
    public Boolean hasAccount = false;
    public Boolean hasService = false;
    public Boolean hasPackage = false;

    @Override
    public String toString() {
        return "AccountAppInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", uid=" + uid +
                ", hasAccount=" + hasAccount +
                ", hasService=" + hasService +
                ", hasPackage=" + hasPackage +
                '}';
    }
}

