package com.tangrun.mdm.shell.pojo;

/**
 * Account {name=360卫士, type=com.qihoo360.mobilesafe.accountsync}
 */
public class Account {
    public String name;
    public String type;

    public Account(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "\"name\":\"" + name + "\"" +
                ", \"type\":\"" + type + "\"" +
                '}';
    }
}
