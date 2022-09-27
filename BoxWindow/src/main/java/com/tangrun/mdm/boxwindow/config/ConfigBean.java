package com.tangrun.mdm.boxwindow.config;

import com.alibaba.fastjson2.JSON;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.tangrun.mdm.boxwindow.service.AppConfigService;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.pojo.ConfigWrapper;
import com.tangrun.mdm.boxwindow.utils.CharUtil;
import com.tangrun.mdm.boxwindow.utils.SerialNumberUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ConfigBean {
    private final Logger logger = LogManager.getLogger(ConfigBean.class);

    @Bean
    public ConfigWrapper configWrapper(AppConfigService appConfigService) {
        ConfigWrapper configWrapper = new ConfigWrapper();
        String content = null;
        {
            URL url = getClass().getResource("license.txt");
            if (url != null) {
                ByteSource byteSource = Resources.asByteSource(url);
                try {
                    content = byteSource.asCharSource(StandardCharsets.UTF_8).read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (content == null) {
                File configFile = new File("license.txt");
                if (configFile.exists() && configFile.isFile()) {
                    try {
                        content = Files.asCharSource(configFile, StandardCharsets.UTF_8).read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (content == null) {
            configWrapper.setMsg("配置文件不存在，请获取license.txt后放到软件运行目录下");
        } else {
            Config config = null;
            try {
                content = content.replaceAll("[\n|\t|\r]", "");
                content = CharUtil.encode(content);
                String read = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                config = JSON.parseObject(read, Config.class);
            } catch (Exception e) {
                logger.error("config parse", e);
            }
            if (config == null) {
                configWrapper.setMsg("license无效");
            } else {
                String machineCode = SerialNumberUtil.getMachineCode();
                if (!machineCode.equals(config.getMachineId())) {
                    configWrapper.setMsg("license在当前设备无法使用");
                } else if (!appConfigService.checkExpireTime(config.getExpireTime())) {
                    configWrapper.setMsg("license已过期，请重新获取");
                } else {
                    configWrapper.setConfig(config);
                }
            }
        }
        return configWrapper;
    }
}
