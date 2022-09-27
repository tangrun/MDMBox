package com.tangrun.mdm.boxwindow.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceLogRepository extends JpaRepositoryImplementation<DeviceLogEntity,Long> {

    List<DeviceLogEntity> findAllByDeviceId(String deviceId);

    List<DeviceLogEntity> findAllByDeviceIdAndState(String deviceId,int state);

    DeviceLogEntity findFirstByDeviceIdAndContent(String deviceId,String content);

}
