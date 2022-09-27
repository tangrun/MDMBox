package com.tangrun.mdm.boxwindow.impl;

import com.tangrun.mdm.boxwindow.SpringBootApplication;
import com.tangrun.mdm.boxwindow.service.AppConfigService;

public class AppUseTimeSetter implements Runnable{
    public static final AppUseTimeSetter sInstance = new AppUseTimeSetter();

    public AppUseTimeSetter() {
    }

    @Override
    public void run() {
        if (!SpringBootApplication.getConfigurableApplicationContext().isActive()) {
            return;
        }
        AppConfigService appConfigService = SpringBootApplication.getApplicationContext()
                .getBean(AppConfigService.class);
        appConfigService.setAppUseTime();
    }
}
