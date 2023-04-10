//package com.tangrun.mdm.boxwindow.dao;
//
//import com.tangrun.mdm.boxwindow.dao.core.SqlBinder;
//import com.tangrun.mdm.boxwindow.dao.mapper.First2IntMapper;
//import com.tangrun.mdm.boxwindow.service.DBService;
//
//import java.util.function.Function;
//import java.util.function.IntFunction;
//
//public class VersionService {
//    public static int getVersion() {
//        return DBService.getInstance()
//                .executeQueryFirst(
//                        """
//                        select max("version") from "t_version";
//                        """
//                        , new First2IntMapper()).orElse(0);
//    }
//
//    public static boolean setVersion(int value) {
//        return DBService.getInstance()
//                .executeUpdate(SqlBinder.bind(
//                        """
//                        insert into "t_version" set "version" = ?;
//                        """,
//                        value
//                )).map(integer -> integer > 0).orElse(false);
//    }
//}
