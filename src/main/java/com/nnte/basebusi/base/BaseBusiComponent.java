package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.basebusi.annotation.FloatCheck;
import com.nnte.basebusi.annotation.IntegerCheck;
import com.nnte.basebusi.annotation.StringCheck;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.basebusi.excption.ExpLogInterface;
import com.nnte.framework.annotation.ConfigLoad;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.base.ConfigInterface;
import com.nnte.framework.base.DBSchemaBase;
import com.nnte.framework.base.SpringContextHolder;
import com.nnte.framework.utils.DateUtils;
import com.nnte.framework.utils.FileLogUtil;
import com.nnte.framework.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        int size=0;
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instanceBody=entry.getValue();
            BusiLogAttr logAttr = instanceBody.getClass().getAnnotation(BusiLogAttr.class);
            if (instanceBody instanceof BaseBusiComponent){
                ((BaseBusiComponent) instanceBody).setLoggerName(logAttr.value());
                if (names.length()>0)
                    names.append(",");
                names.append(logAttr.value());
                size++;
            }
        }
        BaseNnte.outConsoleLog("设置组件日志属性["+size+"]："+names.toString());
    }

    /**
     * 检测对象属性的值是否正确，本函数一般用于输入检测
     * */
    public static boolean checkModelFields(Object model) throws BusiException {
        Field[] fields=model.getClass().getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            Field f = fields[i];
            String typeName=f.getGenericType().getTypeName();
            if (typeName.indexOf(DBSchemaBase.SchemaColType.I.getVal())>=0 ||
                    typeName.indexOf(DBSchemaBase.SchemaColType.L.getVal())>=0){
                IntegerCheck integerCheck=f.getAnnotation(IntegerCheck.class);
                if (integerCheck!=null){
                    String colName=integerCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval=f.get(model);
                        if (!integerCheck.nullValid()){//如果字段值不能为null
                            if (fval==null)
                                throw new BusiException(colName+"不能为null");
                        }
                        if (fval!=null){
                            Integer ival=Integer.valueOf(fval.toString());
                            if (ival>integerCheck.maxVal())
                                throw new BusiException(colName+"不能大于"+integerCheck.maxVal());
                            if (ival<integerCheck.minVal())
                                throw new BusiException(colName+"不能小于"+integerCheck.minVal());
                            if (integerCheck.inVals()!=null && integerCheck.inVals().length>0){
                                boolean isfinded =false;
                                for(int iv:integerCheck.inVals()){
                                    if (ival.equals(iv)){
                                        isfinded = true;
                                        break;
                                    }
                                }
                                if (!isfinded)
                                    throw new BusiException(colName+"不在指定范围内");
                            }
                        }
                    }catch (IllegalAccessException ie){
                        throw new BusiException(colName+"访问错误!"+ie.getMessage());
                    }
                    return true;
                }
            }else if (typeName.indexOf(DBSchemaBase.SchemaColType.S.getVal())>=0){
                StringCheck stringCheck=f.getAnnotation(StringCheck.class);
                if (stringCheck!=null){
                    String colName=stringCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval=f.get(model);
                        if (!stringCheck.nullValid()){//如果字段值不能为null
                            if (fval==null || StringUtils.isEmpty(fval.toString()))
                                throw new BusiException(colName+"不能为空");
                        }
                        if (stringCheck.maxLen()>0){//如果字段长度有限制
                            if (fval!=null && fval.toString().length()>stringCheck.maxLen())
                                throw new BusiException(colName+"长度不能超过"+stringCheck.maxLen());
                        }
                        if (StringUtils.isNotEmpty(stringCheck.dateFormat())){
                            //如果需要校验时间格式
                            String reg= DateUtils.DateFmtRegMap.get(stringCheck.dateFormat());
                            if (StringUtils.isEmpty(reg))
                                throw new BusiException(colName+"注解时间格式定义不正确");
                            Pattern p = Pattern.compile(reg);
                            Matcher m = p.matcher(StringUtils.defaultString(fval));
                            if (!m.matches())
                                throw new BusiException(colName+"时间格式不正确");
                        }
                    }catch (IllegalAccessException ie){
                        throw new BusiException(colName+"访问错误!"+ie.getMessage());
                    }
                    return true;
                }
            }else if (typeName.indexOf(DBSchemaBase.SchemaColType.F.getVal())>=0){
                FloatCheck floatCheck=f.getAnnotation(FloatCheck.class);
                if (floatCheck!=null){
                    String colName=floatCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval=f.get(model);
                        if (!floatCheck.nullValid()){//如果字段值不能为null
                            if (fval==null)
                                throw new BusiException(colName+"不能为null");
                        }
                        if (fval!=null){
                            Double dval=Double.valueOf(fval.toString());
                            if (dval>floatCheck.maxVal())
                                throw new BusiException(colName+"不能大于"+floatCheck.maxVal());
                            if (dval<floatCheck.minVal())
                                throw new BusiException(colName+"不能小于"+floatCheck.minVal());
                        }
                    }catch (IllegalAccessException ie){
                        throw new BusiException(colName+"访问错误!"+ie.getMessage());
                    }
                    return true;
                }
            }else if (typeName.indexOf(DBSchemaBase.SchemaColType.D.getVal())>=0){

            }
        }
        return true;
    }
}
