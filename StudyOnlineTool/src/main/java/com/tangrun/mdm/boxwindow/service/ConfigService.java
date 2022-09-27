package com.tangrun.mdm.boxwindow.service;

import com.google.gson.Gson;
import com.tangrun.mdm.boxwindow.BuildConfig;
import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
import com.tangrun.mdm.boxwindow.dao.AppConfigService;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.utils.Utils;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Log4j2
public class ConfigService implements LifecycleEventListener {

    private static ConfigService configService;
    public static ConfigService getInstance(){
        if (configService== null)
            configService = new ConfigService();
        return configService;
    }

    private ConfigService(){

    }

    ConfigWrapper configWrapper;
    public ConfigWrapper getConfig(){
        if (configWrapper == null)
            configWrapper = getConfigWrapper_();
        return configWrapper;
    }

    @Override
    public void onEvent(LifecycleState state) {

        if (state == LifecycleState.OnInit){

        }else if (state == LifecycleState.OnReady){
//            AppConfigService.setAppUseTime();
        }else if (state == LifecycleState.OnRelease){
            AppConfigService.setAppUseTime();
        }
    }

    private   ConfigWrapper getConfigWrapper_( ) {
        ConfigWrapper configWrapper = new ConfigWrapper();
        Config config = new Config();
        configWrapper.setConfig(config);
        config.setAppName(BuildConfig.appName);
        config.setPkgName("com.cdblue.OnlineAssistant");
        config.setClsName("com.cdblue.OnlineAssistant.receiver.DeviceManageBC");
        config.setMachineId( Utils.getMachineCode());
        LocalDateTime localDateTime = LocalDateTime.of(2050, 12, 31, 23, 56, 59);
        config.setExpireTime(localDateTime.toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
        config.setDesc("-");
        return configWrapper;
    }
    private   ConfigWrapper getConfigWrapper( ) {
        ConfigWrapper configWrapper = new ConfigWrapper();
        String content = null;
        {
            File configFile = new File("license.txt");
            if (configFile.exists() && configFile.isFile()) {
                content = Utils.readFile(configFile);
            }
        }
        if (content == null) {
            configWrapper.setMsg("license未找到，请复制设备ID给软件提供者以获取license.txt文件，然后放到软件运行目录下再启动");
        } else {
            Config config = null;
            try {
                content = content.replaceAll("[\n|\t|\r]", "");
                content = Utils.encode(content);
                String read = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                config = new Gson().fromJson(read, Config.class);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
            if (config == null) {
                configWrapper.setMsg("license无效");
            } else {
                String machineCode = Utils.getMachineCode();
                if (!machineCode.equals(config.getMachineId())) {
                    configWrapper.setMsg("license在当前设备无法使用");
                } else if (!AppConfigService.checkExpireTime(config.getExpireTime())) {
                    configWrapper.setMsg("license已过期，请重新获取");
                } else {
                    configWrapper.setConfig(config);
                }
            }
        }
        if (configWrapper.getConfig() == null){
            configWrapper.setMsg(configWrapper.getMsg()+ "\n\n设备ID: " + Utils.getMachineCode());
        }
        return configWrapper;
    }
}
