package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.basebusi.excption.ExpLogInterface;
import com.nnte.framework.annotation.ConfigLoad;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.base.ConfigInterface;
import com.nnte.framework.base.SpringContextHolder;
import com.nnte.framework.utils.FileLogUtil;
import com.nnte.framework.utils.StringUtils;

import java.util.Map;

public abstract class BaseBusiComponent implements ExpLogInterface {
    private String loggername;  //日志位置
    private String logrootpath; //日志路径

    public BaseBusiComponent(){
        setLoggerName(this.getClass().getName());//路径默认是组件名称
    }
    public BaseBusiComponent(String loggername){
        setLoggerName(loggername);
    }
    @ConfigLoad
    public ConfigInterface appConfig;//取本地配置接口
    /**
     * 设置组件日志打印路径
     * */
    public void setLoggerName(String loggerName){
        loggername = loggerName;
        logrootpath= System.getProperty("user.home")+"/logs/"+loggername;
    }

    @Override
    public void logException(BusiException busiExp) {
        logFileMsg(busiExp.getMessage());
    }

    /**
     * 通过本组件输出一条日志信息到文件,同时打印到控制台
     * */
    public void logFileMsg(String logMsg) {
        if (StringUtils.isNotEmpty(loggername))
            FileLogUtil.WriteLogToFile(loggername,logrootpath,logMsg);
        BaseNnte.outConsoleLog(logMsg);
    }
    /**
     * 设置有日志属性注解的组件的日志属性
     * */
    public static void loadComponentBusiLogAttr(){
        Map<String,Object> beans = SpringContextHolder.getApplicationContext().getBeansWithAnnotation(BusiLogAttr.class);
        StringBuffer names=new StringBuffer();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instanceBody=entry.getValue();
            BusiLogAttr logAttr = instanceBody.getClass().getAnnotation(BusiLogAttr.class);
            if (instanceBody instanceof BaseBusiComponent){
                ((BaseBusiComponent) instanceBody).setLoggerName(logAttr.value());
                if (names.length()>0)
                    names.append(",");
                names.append(logAttr.value());
            }
        }
        BaseNnte.outConsoleLog("设置组件日志属性["+names.length()+"]："+names.toString());
    }
}
