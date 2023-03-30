package com.nnte.basebusi.excption;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.framework.base.BaseNnte;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务异常
 * */
@Setter
@Getter
public class BusiException extends Exception{
    private static Logger log = LoggerFactory.getLogger(BusiException.class);
    private String callerLogger;
    private Integer expCode;

    public BusiException(Integer code,String msg){
        super(msg);
        defaultExp();
        if (code!=null)
            expCode = code;
    }

    public BusiException(Exception e){
        super(e);
        defaultExp();
    }

    public BusiException(Exception e,Integer code){
        super(e);
        defaultExp();
        if (code!=null)
            expCode = code;
    }

    public BusiException(String msg){
        super(msg);
        defaultExp();
    }

    private void defaultExp(){
        expCode = 3000;//错误默认为3000
        StackTraceElement[] elements=this.getStackTrace();
        for(int i=0;i<elements.length;i++){
            Class clazz=elements[i].getClass();
            if (clazz.isInstance(BaseNnte.class)){
                BusiLogAttr logAttr = (BusiLogAttr) clazz.getAnnotation(BusiLogAttr.class);
                if (logAttr!=null){
                    setCallerLogger(logAttr.value());
                    break;
                }
            }
        }
        if(callerLogger==null || callerLogger.length()<=0)
            callerLogger = this.getClass().getSimpleName();
    }
    /**
     * 打印异常
     * */
    public void printException(){
        //LogUtil.logExp(callerLogger,expLevel,this);
        log.info("BusiException:"+this.getMessage());
    }
}
