package com.tangrun.mdm.boxwindow.service;

import com.tangrun.mdm.boxwindow.core.LifecycleEventListener;
import com.tangrun.mdm.boxwindow.core.LifecycleState;
import com.tangrun.mdm.boxwindow.dao.DeviceLogService;
import com.tangrun.mdm.boxwindow.dao.core.Func;
import com.tangrun.mdm.boxwindow.dao.VersionService;
import com.tangrun.mdm.boxwindow.dao.entity.DeviceLogEntity;
import lombok.extern.log4j.Log4j2;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.ConnectionPoolDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class DBService implements LifecycleEventListener {

    private static DBService sDBService;

    public static DBService getInstance() {
        if (sDBService == null)
            sDBService = new DBService();
        return sDBService;
    }

    private JdbcConnectionPool connectionPool;

    private void initConnectPool() {
        connectionPool = JdbcConnectionPool.create("jdbc:h2:file:./app/data;FILE_LOCK=NO", "cdblue", "Cdblue123123!!");
    }

    public Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> executeQueryFirst(String sql, Func<ResultSet, T> rowMapper) {
        return executeQueryFirst(sql, rowMapper, null);
    }

    public <T> Optional<T> executeQueryFirst(String sql, Func<ResultSet, T> rowMapper, T defaultValue) {
        Optional<List<T>> list = executeQuery(sql, rowMapper);
        return list.flatMap(ts -> ts.stream().findFirst());
    }

    public <T> Optional<List<T>> executeQuery(String sql, Func<ResultSet, T> rowMapper) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);
        ) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                T apply = rowMapper.apply(resultSet);
                list.add(apply);
            }
            return Optional.of(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Boolean> executeUpdateResult(String sql) {
        return executeUpdate(sql).map(integer -> integer > 0);
    }

    public Optional<Integer> executeUpdate(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();) {

            return Optional.of(statement.executeUpdate(sql));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Boolean> execute(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();) {
            return Optional.of(statement.execute(sql));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public boolean initTable() {
        if (execute("""
                create table if not exists "t_version"(
                    "version" int primary key
                );
                """).isEmpty()) {
            log.error("create version table error.");
            return false;
        }
        int version = VersionService.getVersion();
        log.debug("db version {}", version);
        if (version == 0) {
            log.debug("update db to version 1");
            if (execute("""
                    create table if not exists "t_device_log"(
                        "id" bigint primary key auto_increment,
                        "content" varchar(255) not null ,
                        "content_type" int ,
                        "create_time" timestamp,
                        "device_id" varchar(64),
                        "state" int,
                        "update_time" timestamp
                    );
                    """
            ).isEmpty()) {
                return false;
            }
            if (execute("""
                    create index if not exists idx_content on "t_device_log"("content");
                    """).isEmpty()) {
                return false;
            }
            if (execute("""
                    create index if not exists idx_device_id on "t_device_log"("device_id");
                    """).isEmpty()) {
                return false;
            }
            if (execute("""
                    create table if not exists "t_app_config"(
                        "id" bigint primary key,
                        "value" varchar(255)
                    );
                    """
            ).isEmpty()) {
                return false;
            }
            version++;
            if (!VersionService.setVersion(version)) {
                return false;
            }
            log.debug("update db to version 1 completed");
        }
        if (version == 1) {

        }

        return true;
    }


    @Override
    public void onEvent(LifecycleState state) {
        if (state == LifecycleState.OnInit) {
            long time = System.currentTimeMillis();
            initConnectPool();
            log.debug("db server init time {}", System.currentTimeMillis() - time);
        } else if (state == LifecycleState.OnClosed) {
            log.debug("db stopping");
            connectionPool.dispose();
            log.debug("db stopped");
        }
    }

    public static void main(String[] args) {
        DBService instance = DBService.getInstance();
        instance.onEvent(LifecycleState.OnInit);
        instance.onEvent(LifecycleState.OnClosed);
    }
}
