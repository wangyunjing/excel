package com.wyj.excel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {

    private static final Logger log = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * 吞噬异常
     */
    public static void engulf(Runnable runnable) {
        engulf(runnable, "");
    }

    /**
     * 吞噬异常
     */
    public static void engulf(Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error(message, e);
        }
    }
}
