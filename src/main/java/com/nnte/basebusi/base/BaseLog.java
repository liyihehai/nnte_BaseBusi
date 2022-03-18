package com.nnte.basebusi.base;

import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.utils.LogUtil;
import com.nnte.framework.utils.ThreadUtil;

public class BaseLog {

    private static String getCallerClassName(){
        String className=ThreadUtil.getStackTraceClassName(BaseNnte.class,true);
        if (className==null||className.length()<=0){
            className = BaseBusi.Logger_Name;
        }
        return className;
    }
    public static void outLogExp(BusiException be){
        be.printException();
    }
    public static void outLogExp(Exception e){
        BusiException be = new BusiException(e);
        be.printException();
    }

    public static void log(LogUtil.LogLevel level, String msg){
        LogUtil.log(getCallerClassName(),level,msg);
    }
    public static void logTrace(String msg){
        log(LogUtil.LogLevel.trace,msg);
    }
    public static void logDebug(String msg){
        log(LogUtil.LogLevel.debug,msg);
    }
    public static void logInfo(String msg){
        log(LogUtil.LogLevel.info,msg);
    }
    public static void logWarn(String msg){
        log(LogUtil.LogLevel.warn,msg);
    }
    public static void logError(String msg){
        log(LogUtil.LogLevel.error,msg);
    }
}
