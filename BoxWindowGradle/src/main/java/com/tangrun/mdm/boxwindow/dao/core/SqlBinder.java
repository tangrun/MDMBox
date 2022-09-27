package com.tangrun.mdm.boxwindow.dao.core;

public class SqlBinder {
    public static String bind(String sql, Object... args) {
        if (args == null || args.length == 0)
            return sql;
        for (Object o : args) {
            if (o instanceof String){
                sql = sql.replaceFirst("\\?", "'"+o+"'");
            }else sql = sql.replaceFirst("\\?", String.valueOf(o));
        }
        return sql;
    }
}
