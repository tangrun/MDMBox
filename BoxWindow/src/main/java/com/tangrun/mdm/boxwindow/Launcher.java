package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.impl.AppUseTimeSetter;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.H2Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;
@Slf4j
public class Launcher {


    public static void main(String[] args) throws InterruptedException {
        SpringApplication.getShutdownHandlers().add(AppUseTimeSetter.sInstance);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        log.debug("launcher start springboot");
        SpringApplication springApplication = new SpringApplication(SpringBootApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> countDownLatch.countDown());
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(args);
        SpringBootApplication.setConfigurableApplicationContext(configurableApplicationContext);
        countDownLatch.await();

        log.debug("launcher start javafx");
        Application.launch(JavaFXApplication.class,args);
        log.debug("launcher exit");
    }
}
