package com.tangrun.mdm.boxwindow.jpa;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Table(name = "t_device_log",
        indexes = {
                @Index(name = "idx_device_id", columnList = "device_id"),
                @Index(name = "idx_content", columnList = "content"),
        }
)
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class DeviceLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    /**
     * 0 包名
     * 1 组件名
     */
    @Column(name = "content_type", nullable = false)
    private Integer contentType;

    @Column(name = "content", nullable = false)
    private String content;

    /**
     * 0 enable
     * 1 disable
     */
    @Column(name = "state", nullable = false)
    private Integer state;

    @Column(name = "create_time", nullable = false)
    @CreationTimestamp
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @UpdateTimestamp
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DeviceLogEntity that = (DeviceLogEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
