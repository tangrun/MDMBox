package com.tangrun.mdm.boxwindow.dao.mapper;

import com.tangrun.mdm.boxwindow.dao.core.Func;

import java.sql.ResultSet;

public class First2IntMapper implements Func<ResultSet,Integer> {
    @Override
    public Integer apply(ResultSet resultSet) throws Exception {
        return resultSet.getInt(1);
    }
}
