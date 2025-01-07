package com.tangrun.mdm.boxwindow.shell.pojo;

import lombok.extern.log4j.Log4j2;

/**
 * UserInfo{0:机主:c13} running
 */
@Log4j2
public class UserInfo {
    public static int FLAG_PRIMARY = 0x01;
    public static int FLAG_ADMIN = 0x02;
    public static int FLAG_GUEST = 0x04;
    public static int FLAG_RESTRICTED = 0x08;
    public static int FLAG_INITIALIZED = 0x10;
    public static int FLAG_MANAGED_PROFILE = 0x20;
    public static int FLAG_DISABLED = 0x40;
    public static int FLAG_QUIET_MODE = 0x80;
    public static int FLAG_EPHEMERAL = 0x100;
    public static int FLAG_DEMO = 0x200;
    public static int FLAG_FULL = 0x400;
    public static int FLAG_SYSTEM = 0x800;
    public static int FLAG_PROFILE = 0x1000;

    public long id;
    public String name;
    public long flags;
    public String state;

    public UserInfo(String id, String name, String flags) {
        this(id, name, flags, "running");
    }

    public UserInfo(String id, String name, String flags,String state) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.state = state;
        this.flags = Long.parseLong(flags, 16);
    }


    public boolean isMain(){
        return id == 0 || isPrimary();
    }

    public boolean isPrimary() {
        return (flags & FLAG_PRIMARY) == FLAG_PRIMARY;
    }
    public boolean isSystem() {
        return (flags & FLAG_SYSTEM) == FLAG_SYSTEM;
    }


    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + "\"" +
                ", \"name\":\"" + name + "\"" +
                ", \"flags\":" + flags +
                '}';
    }
}
