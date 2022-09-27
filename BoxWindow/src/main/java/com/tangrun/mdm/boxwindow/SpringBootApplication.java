package com.tangrun.mdm.boxwindow;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@org.springframework.boot.autoconfigure.SpringBootApplication
public class SpringBootApplication implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static ConfigurableApplicationContext configurableApplicationContext;

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBootApplication.applicationContext = applicationContext;
    }

    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    public static void setConfigurableApplicationContext(ConfigurableApplicationContext configurableApplicationContext) {
        SpringBootApplication.configurableApplicationContext = configurableApplicationContext;
    }
}
