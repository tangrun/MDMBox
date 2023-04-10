package com.tangrun.mdm.boxwindow.dao;


import com.tangrun.mdm.boxwindow.dao.core.SqlBinder;
import com.tangrun.mdm.boxwindow.dao.entity.AppConfigEntity;
import com.tangrun.mdm.boxwindow.dao.mapper.First2StringMapper;
import com.tangrun.mdm.boxwindow.service.ConfigService;
//import com.tangrun.mdm.boxwindow.service.DBService;

import java.util.Optional;

public class AppConfigService {

    private static final int id_last_app_use_time = 0x01;
    private static final int id_last_install_apk_path = 0x02;
    private static final int id_last_expire_time = 0x03;


    public static String getInstallApkPath() {
        return getConfigString(id_last_install_apk_path);
    }

    public static void setInstallApkPath(String path) {
        setConfig(id_last_install_apk_path, path);
    }

    public static void setAppUseTime() {
        Long lastOpenTime = getConfigLong(id_last_app_use_time);
        if (lastOpenTime == null) lastOpenTime = 0L;

        setConfig(id_last_app_use_time, Math.max(lastOpenTime, System.currentTimeMillis()));
    }

    public static boolean checkExpireTime(Long time) {
        if (time == null) return false;
        Long lastExpireTime = getConfigLong(id_last_expire_time);
        if (!time.equals(lastExpireTime)) {
            setConfig(id_last_app_use_time, System.currentTimeMillis());
            setConfig(id_last_expire_time, time);
        }
        Long lastOpenTime = getConfigLong(id_last_app_use_time);
        if (lastOpenTime == null) {
            lastOpenTime = 0L;
        }
        return Math.max(System.currentTimeMillis(), lastOpenTime) < time;
    }


    private static void setConfig(int id, Object value) {
        ConfigService.getInstance().getMap().put(String.valueOf(id),String.valueOf(value));
        ConfigService.getInstance().saveMap();
//        AppConfigEntity appConfigEntity = new AppConfigEntity();
//        appConfigEntity.setId((long) id);
//        appConfigEntity.setValue(String.valueOf(value));
//
//        DBService.getInstance()
//                .executeUpdateResult(SqlBinder.bind(
//                        """
//                                update "t_app_config" set "value" = ? where "id" = ?;
//                                """,
//                        value, id
//                ));
//        DBService.getInstance().openSession(new Consumer<Session>() {
//            @Override
//            public void accept(Session session) {
//                session.merge(appConfigEntity);
//            }
//        });
    }

    private static Optional<String> getConfig(int id) {
        String s = ConfigService.getInstance().getMap().get(String.valueOf(id));
        return Optional.ofNullable(s);
//        return DBService.getInstance()
//                .executeQueryFirst(SqlBinder.bind(
//                        """
//                                select "value" from "t_app_config" where "id" = ?;
//                                """,
//                        id
//                ), new First2StringMapper());
    }

    private static String getConfigString(int id) {
        return getConfig(id)
                .orElse(null);
    }

    private static Integer getConfigInteger(int id) {
        return getConfig(id)
                .map(Integer::parseInt)
                .orElse(null);
    }

    private static Long getConfigLong(int id) {
        return getConfig(id)
                .map(Long::parseLong)
                .orElse(null);
    }

    private static Boolean getConfigBoolean(int id) {
        return getConfig(id)
                .map(Boolean::parseBoolean)
                .orElse(null);
    }

    private static Float getConfigFloat(int id) {
        return getConfig(id)
                .map(Float::parseFloat)
                .orElse(null);
    }

    private static Double getConfigDouble(int id) {
        return getConfig(id)
                .map(Double::parseDouble)
                .orElse(null);
    }


}
