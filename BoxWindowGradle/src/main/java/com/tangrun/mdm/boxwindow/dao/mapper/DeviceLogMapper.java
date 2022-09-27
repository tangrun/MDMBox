package com.tangrun.mdm.boxwindow.dao.mapper;

import com.tangrun.mdm.boxwindow.dao.core.Func;
import com.tangrun.mdm.boxwindow.dao.entity.DeviceLogEntity;

import java.sql.ResultSet;

public class DeviceLogMapper implements Func<ResultSet, DeviceLogEntity> {
    @Override
    public DeviceLogEntity apply(ResultSet resultSet) throws Exception {
        DeviceLogEntity entity = new DeviceLogEntity();
        entity.setId(resultSet.getLong("id"));
        entity.setDeviceId(resultSet.getString("device_id"));
        entity.setContent(resultSet.getString("content"));
        entity.setContentType(resultSet.getInt("content_type"));
        entity.setState(resultSet.getInt("state"));
        entity.setCreateTime(resultSet.getTimestamp("create_time"));
        entity.setUpdateTime(resultSet.getTimestamp("update_time"));
        return entity;
    }
}
