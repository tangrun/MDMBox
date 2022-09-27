package com.tangrun.mdm.boxwindow.dao.mapper;

import com.tangrun.mdm.boxwindow.dao.core.Func;

import java.sql.ResultSet;

public class First2StringMapper implements Func<ResultSet,String> {
    @Override
    public String apply(ResultSet resultSet) throws Exception {
        return resultSet.getString(1);
    }
}
