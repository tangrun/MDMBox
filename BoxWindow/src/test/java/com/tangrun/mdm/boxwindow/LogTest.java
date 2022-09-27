package com.tangrun.mdm.boxwindow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTest {
    Logger logger = LogManager.getLogger(LogTest.class);
    public static void main(String[] args) {
        new LogTest();
    }

    public LogTest( ) {
        logger.debug("dddddd");
        logger.info("dddddd");
        logger.warn("dddddd");
        logger.error("dddddd");

        logger.debug("Logging in user {} with birthday {}","user.game()", 0.12315f);
    }
}
