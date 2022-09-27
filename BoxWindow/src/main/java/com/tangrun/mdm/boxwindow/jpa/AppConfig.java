package com.tangrun.mdm.boxwindow.jpa;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Table(name = "t_app_config")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AppConfig {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "value")
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AppConfig appConfig = (AppConfig) o;
        return id != null && Objects.equals(id, appConfig.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
