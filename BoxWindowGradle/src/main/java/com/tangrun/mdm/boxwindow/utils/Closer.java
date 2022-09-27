package com.tangrun.mdm.boxwindow.utils;

import lombok.extern.log4j.Log4j2;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Closer implements Closeable {
    private final List<Closeable> closeableList = new ArrayList<>();

    public <T extends Closeable> T register(T closeable){
        closeableList.add(closeable);
        return closeable;
    }

    @Override
    public void close(){
        for (Closeable closeable : closeableList) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }
}
