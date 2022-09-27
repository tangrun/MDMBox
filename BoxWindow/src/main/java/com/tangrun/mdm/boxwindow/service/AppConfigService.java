package com.tangrun.mdm.boxwindow.service;

import com.tangrun.mdm.boxwindow.jpa.AppConfig;
import com.tangrun.mdm.boxwindow.jpa.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    private final int id_last_app_use_time = 0x01;
    private final int id_last_install_apk_path = 0x02;

    @Autowired
    AppConfigRepository appConfigRepository;

    AppConfigService appConfigService;

    public AppConfigService() {
        appConfigService = this;
    }

    public void setConfig(int id, Object value) {
        AppConfig appConfig = new AppConfig();
        appConfig.setId((long) id);
        appConfig.setValue(String.valueOf(value));
        appConfigRepository.save(appConfig);
    }

    public String getConfigString(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .orElse(null);
    }

    public Integer getConfigInteger(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .map(Integer::parseInt)
                .orElse(null);
    }

    public Long getConfigLong(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .map(Long::parseLong)
                .orElse(null);
    }

    public Boolean getConfigBoolean(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .map(Boolean::parseBoolean)
                .orElse(null);
    }

    public Float getConfigFloat(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .map(Float::parseFloat)
                .orElse(null);
    }

    public Double getConfigDouble(int id) {
        return appConfigRepository.findById((long) id)
                .map(AppConfig::getValue)
                .map(Double::parseDouble)
                .orElse(null);
    }

    public String getInstallApkPath(){
        return appConfigService.getConfigString(id_last_install_apk_path);
    }
    public void setInstallApkPath(String path){
        appConfigService.setConfig(id_last_install_apk_path,path);
    }

    public void setAppUseTime() {
        Long lastOpenTime = appConfigService.getConfigLong(id_last_app_use_time);
        if (lastOpenTime == null) lastOpenTime = 0L;

        appConfigService.setConfig(id_last_app_use_time, Math.max(lastOpenTime, System.currentTimeMillis()));
    }

    public boolean checkExpireTime(Long time) {
        if (time == null) return false;
        Long lastOpenTime = appConfigService.getConfigLong(id_last_app_use_time);
        if (lastOpenTime == null) {
            lastOpenTime = 0L;
        }
        return Math.max(System.currentTimeMillis(), lastOpenTime) < time;
    }
}
