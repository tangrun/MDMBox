package com.tangrun.mdm.boxwindow.dao.core;

public interface Func<T, R> {

    R apply(T t) throws Exception;
}
