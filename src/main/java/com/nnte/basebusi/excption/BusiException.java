package com.nnte.basebusi.excption;

import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.utils.FileLogUtil;
import com.nnte.framework.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常
 * */
@Setter
@Getter
public class BusiException extends Exception{
    private static ExpLogLevelInterface tmpLogLevelInter;
    public enum ExpLevel{
        INFO,
        WARN,
        ERROR
    }
    private Integer expCode;
    private ExpLevel expLevel;

    public BusiException(Integer code,String msg,ExpLevel level){
        super(msg);
        defaultExp();
        if (code!=null)
            expCode = code;
        if (level!=null)
            expLevel = level;
    }

    public BusiException(Exception e){
        super(e);
        defaultExp();
    }

    public BusiException(Exception e,Integer code,ExpLevel level){
        super(e);
        defaultExp();
        if (code!=null)
            expCode = code;
        if (level!=null)
            expLevel = level;
    }

    public BusiException(String msg){
        super(msg);
        defaultExp();
    }

    private void defaultExp(){
        expCode = 3000;//错误默认为3000
        expLevel = ExpLevel.WARN; //默认为警告
    }
    /**
     * 打印异常，如果要输出到日志，需要提供日志接口
     * 如果异常等级为ERROR，控制台会打印程序堆栈
     * 如果等级临时接口有效，按临时异常等级执行异常输出
     * */
    public void printException(ExpLogInterface log,String callMethodName){
        ExpLevel tmpLevel=null;
        if (tmpLogLevelInter!=null)
            tmpLevel=tmpLogLevelInter.getTempExpLevel();
        if (log!=null)
        {
            ExpLevel srcLevel=this.expLevel;
            if (tmpLevel!=null)
                expLevel = tmpLevel;
            String toFileMsg;
            if (StringUtils.isEmpty(callMethodName))
                toFileMsg=BaseNnte.outConsoleLog(getMessage(),"printException",1);
            else
                toFileMsg=BaseNnte.outConsoleLog(getMessage(),callMethodName,1);
            if (StringUtils.isNotEmpty(log.getLoggername())) {
                FileLogUtil.WriteLogToFile(log.getLoggername(), log.getLogrootpath(), toFileMsg);
            }
            expLevel = srcLevel;
        }
        if (expLevel!=null && !expLevel.equals(ExpLevel.INFO) ||
                tmpLevel!=null && !tmpLevel.equals(ExpLevel.INFO)){
            StringBuffer outBuffer = new StringBuffer();
            for(StackTraceElement ste:this.getStackTrace()){
                outBuffer.append(ste.getFileName()).append(":").append(ste.getLineNumber()).append("\r\n");
            }
            String traceMgs=outBuffer.toString();
            System.out.print(traceMgs);
            if (StringUtils.isNotEmpty(log.getLoggername())) {
                FileLogUtil.WriteLogToFile(log.getLoggername(), log.getLogrootpath(), traceMgs);
            }
        }
    }
}
