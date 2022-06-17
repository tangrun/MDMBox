package com.tangrun.mdm.shell.pojo;

public class ComponentName {
    public String className;
    public String packageName;

    public ComponentName( String packageName,String className) {
        this.className = className;
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "{" +
                "\"className\":\"" + className + "\"" +
                ", \"packageName\":\"" + packageName + "\"" +
                '}';
    }
}
