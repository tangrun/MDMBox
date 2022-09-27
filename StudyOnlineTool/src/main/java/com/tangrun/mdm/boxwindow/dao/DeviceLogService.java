package com.tangrun.mdm.boxwindow.dao;

import com.tangrun.mdm.boxwindow.dao.core.SqlBinder;
import com.tangrun.mdm.boxwindow.dao.entity.DeviceLogEntity;
import com.tangrun.mdm.boxwindow.dao.mapper.DeviceLogMapper;
import com.tangrun.mdm.boxwindow.service.DBService;
import lombok.extern.log4j.Log4j2;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DeviceLogService {

    public static DeviceLogEntity getDeviceLog(String deviceId, String content){
        return DBService.getInstance()
                .executeQueryFirst(SqlBinder.bind(
                        """
                        select * from "t_device_log"
                            where "device_id"=? and "content"=?;
                        """,
                        deviceId,content
                ),new DeviceLogMapper()).orElse(null);
    }
    public static List<DeviceLogEntity> getDeviceLogList(String deviceId) {
        return DBService.getInstance()
                .executeQuery(SqlBinder.bind(
                        """
                        select * from "t_device_log"
                            where "device_id" = ?;
                        """,
                        deviceId
                ),new DeviceLogMapper()).orElse(new ArrayList<>());
    }

    public static void save(DeviceLogEntity deviceLogEntity) {
        if (deviceLogEntity.getId() == null){
            DBService.getInstance()
                    .executeUpdate(SqlBinder.bind(
                            """
                            insert into "t_device_log"
                                set "content" = ?,"content_type"=?,"device_id"=?,  "state" = ?,"create_time"=?,"update_time"=?;
                            """,
                            deviceLogEntity.getContent(),deviceLogEntity.getContentType(),
                            deviceLogEntity.getDeviceId(),deviceLogEntity.getState(),
                            new Timestamp(System.currentTimeMillis()).toString(),new Timestamp(System.currentTimeMillis()).toString()
                    ));
        }else {
            DBService.getInstance()
                    .executeUpdate(SqlBinder.bind(
                            """
                            update "t_device_log" 
                                set "content" = ?,"content_type"=?,"device_id"=?,  "state" = ?,"update_time"=? 
                            where "id"=?;
                            """,
                            deviceLogEntity.getContent(),deviceLogEntity.getContentType(),
                            deviceLogEntity.getDeviceId(),deviceLogEntity.getState(),
                            new Timestamp(System.currentTimeMillis()).toString(),deviceLogEntity.getId()
                    ));
        }
    }

}
