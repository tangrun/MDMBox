package com.tangrun.mdm.boxwindow;

import com.tangrun.mdm.boxwindow.jpa.DeviceLogEntity;
import com.tangrun.mdm.boxwindow.jpa.DeviceLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DBTest {

    Logger logger = LogManager.getLogger(DBTest.class);

    @Autowired
    DeviceLogRepository deviceLogRepository;

    @Test
    public void test(){
        DeviceLogEntity deviceLogEntity = new DeviceLogEntity();
        deviceLogEntity.setDeviceId("s");
        deviceLogEntity.setContent("aaa");
        deviceLogEntity.setState(1);
        deviceLogEntity.setContentType(0);
        deviceLogRepository.saveAndFlush(deviceLogEntity);
    }

}
