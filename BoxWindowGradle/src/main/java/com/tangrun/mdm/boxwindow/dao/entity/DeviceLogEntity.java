package com.tangrun.mdm.boxwindow.dao.entity;

import lombok.*;
import java.util.Date;

//@Table(name = "t_device_log",
//        indexes = {
//                @Index(name = "idx_device_id", columnList = "device_id"),
//                @Index(name = "idx_content", columnList = "content"),
//        }
//)
@Data
public class DeviceLogEntity {

    private Long id;

    private String deviceId;

    /**
     * 0 包名
     * 1 组件名
     */
    private Integer contentType;

    private String content;

    /**
     * 0 enable
     * 1 disable
     */
    private Integer state;

    private Date createTime;

    private Date updateTime;

}
