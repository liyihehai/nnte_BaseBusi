package com.nnte.basebusi.base;

import com.nnte.basebusi.excption.BusiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseLog {
    private static Logger log = LoggerFactory.getLogger(BaseLog.class);
    public static void outLogExp(BusiException be){
        be.printException();
    }
    public static void outLogExp(Exception e){
        BusiException be = new BusiException(e);
        be.printException();
    }

    public static void logTrace(String msg){
        log.trace(msg);
    }
    public static void logDebug(String msg){
        log.debug(msg);
    }
    public static void logInfo(String msg){
        log.info(msg);
    }
    public static void logWarn(String msg){
        log.warn(msg);
    }
    public static void logError(String msg){
        log.error(msg);
    }
}
