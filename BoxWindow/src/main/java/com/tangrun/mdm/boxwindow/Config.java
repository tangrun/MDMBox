package com.tangrun.mdm.boxwindow;

import com.alibaba.fastjson2.JSON;
import com.google.common.io.BaseEncoding;
import com.tangrun.mdm.shell.pojo.ComponentName;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    public String getAppName() {
        return appName;
    }

    public Config setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Config setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getPkgName() {
        return pkgName;
    }

    public Config setPkgName(String pkgName) {
        this.pkgName = pkgName;
        return this;
    }

    public String getClsName() {
        return clsName;
    }

    public Config setClsName(String clsName) {
        this.clsName = clsName;
        return this;
    }

    public static void main(String[] args) {
        // safe
        //eyJhcHBOYW1lIjoiUmFpbiIsImNsc05hbWUiOiJjb20udGFuZ3J1bi5zYWZlLmRwbS5EUE1SZXN0cmljdGlvbnNSZWNlaXZlciIsImNvbXBvbmVudCI6eyJjbGFzc05hbWUiOiJjb20udGFuZ3J1bi5zYWZlLmRwbS5EUE1SZXN0cmljdGlvbnNSZWNlaXZlciIsInBhY2thZ2VOYW1lIjoiY29tLnRhbmdydW4uc2FmZSJ9LCJkZXNjIjoiUmFpbiDmv4DmtLvlt6XlhbciLCJwa2dOYW1lIjoiY29tLnRhbmdydW4uc2FmZSJ9
        Config config = new Config();
        config.appName ="Rain";
        config.desc ="Rain 激活工具";
        config.pkgName ="com.tangrun.safe";
        config.clsName ="com.tangrun.safe.dpm.DPMRestrictionsReceiver";
        String encode = BaseEncoding.base64().encode(JSON.toJSONString(config).getBytes(StandardCharsets.UTF_8));
        System.out.println(encode);
        byte[] decode = BaseEncoding.base64().decode(encode);
        System.out.println(new String(decode,StandardCharsets.UTF_8));

    }

    public ComponentName getComponent() {
        return new ComponentName(pkgName, clsName);
    }
}
