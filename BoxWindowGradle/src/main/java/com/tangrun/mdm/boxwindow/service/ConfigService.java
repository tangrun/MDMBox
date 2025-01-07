package com.tangrun.mdm.boxwindow.service;

import com.google.gson.Gson;
import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
//import com.tangrun.mdm.boxwindow.dao.AppConfigService;
import com.tangrun.mdm.boxwindow.dao.AppConfigService;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.utils.Utils;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ConfigService implements LifecycleEventListener {

    private static ConfigService configService;

    public static ConfigService getInstance() {
        if (configService == null)
            configService = new ConfigService();
        return configService;
    }

    private ConfigService() {

    }

    private Map<String, String> dataMap;

    public Map<String, String> getMap() {
        if (dataMap != null) {
            return dataMap;
        }
        dataMap = new HashMap<>();
        File file = new File(getDataDir() + "data");
        if (Utils.createFileOrExists(file)) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                while (bufferedReader.ready()) {
                    String s1 = bufferedReader.readLine();
                    int i = s1.indexOf("=");
                    if (i > 0) {
                        String key = s1.substring(0, i);
                        String value = s1.substring(i + 1);
                        dataMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dataMap;
    }

    public void saveMap() {
        File file = new File(getDataDir() + "data");
        if (!Utils.createFileOrExists(file)) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ConfigWrapper configWrapper;

    public ConfigWrapper getConfig() {
        if (configWrapper == null)
            configWrapper = getConfigWrapper();
        return configWrapper;
    }

    public boolean saveConfig(String msg) {
        return writeToFile(msg, new File(getLicensePath()));
    }

    public boolean writeToFile(String msg, File configFile) {
        if (!Utils.createFileOrExists(configFile)) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(configFile);
            fileWriter.write(msg);
            fileWriter.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public void onEvent(LifecycleState state) {

        if (state == LifecycleState.OnInit) {

        } else if (state == LifecycleState.OnReady) {
            AppConfigService.setAppUseTime();
        } else if (state == LifecycleState.OnRelease) {
            AppConfigService.setAppUseTime();
        }
    }

    public static String getLogDir(){
        return ConfigService.getDataDir() + "log"+ File.separator;
    }

    public static String getDataDir() {
        return System.getProperty("user.home")
                + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "激活助手" + File.separator;
    }

    private static String getLicensePath() {
        return getDataDir() + "license.txt";
    }

    private ConfigWrapper getConfigWrapper() {


        ConfigWrapper configWrapper = new ConfigWrapper();
        String content = null;
        {
            File configFile = new File(getLicensePath());
            if (Utils.createFileOrExists(configFile)) {
                content = Utils.readFile(configFile);
            }
        }
        if (content == null) {
            configWrapper.setMsg("请右键以管理员方式启动软件");
        } else {
            Config config = null;
            try {
                content = content.replaceAll("[\n|\t|\r]", "");
                content = Utils.encode(content);
                String read = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                config = new Gson().fromJson(read, Config.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (config == null) {
                configWrapper.setMsg("license无效");
            } else {
                String machineCode = Utils.getMachineCode();
//                if (!machineCode.equals(config.getMachineId())) {
//                    configWrapper.setMsg("license在当前设备无法使用");
//                } else
                if (!AppConfigService.checkExpireTime(config.getExpireTime())) {
                    configWrapper.setMsg("license已过期，请重新获取");
                } else {
                    configWrapper.setConfig(config);
                }
            }
        }
        if (configWrapper.getConfig() == null) {
            configWrapper.setMsg(configWrapper.getMsg() + "\n\n设备ID: " + Utils.getMachineCode());
        }
        return configWrapper;
    }
}
