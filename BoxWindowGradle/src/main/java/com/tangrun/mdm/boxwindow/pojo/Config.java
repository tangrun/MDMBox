package com.tangrun.mdm.boxwindow.pojo;

import com.tangrun.mdm.boxwindow.shell.pojo.ComponentName;
import lombok.Data;

@Data
public class Config {
    /**
     * app name
     */
    String appName;
    /**
     * desc
     */
    String desc;
    /**
     * app package name
     */
    String pkgName;
    /**
     * admin class name
     */
    String clsName;

    /**
     * 机器id
     */
    String machineId;

    /**
     * 有效期
     */
    Long expireTime;

    /**
     * 调试日志
     */
    Boolean debug;


    public ComponentName getComponent() {
        return new ComponentName(pkgName, clsName);
    }
}
