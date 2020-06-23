package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.*;
import com.nnte.basebusi.entity.MEnter;
import com.nnte.basebusi.entity.SysRole;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.basebusi.excption.ExpLogInterface;
import com.nnte.framework.annotation.ConfigLoad;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.base.ConfigInterface;
import com.nnte.framework.base.DBSchemaBase;
import com.nnte.framework.base.SpringContextHolder;
import com.nnte.framework.entity.KeyValue;
import com.nnte.framework.utils.DateUtils;
import com.nnte.framework.utils.FileLogUtil;
import com.nnte.framework.utils.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseBusiComponent implements ExpLogInterface {
    private String loggername;  //日志位置
    private String logrootpath; //日志路径

    public BaseBusiComponent(){
        setLoggerName(this.getClass().getSimpleName());//路径默认是组件名称
    }
    public BaseBusiComponent(String loggername){
        setLoggerName(loggername);
    }
    /**
     * 定义系统功能入口函数
     * key:功能路径；MEnter：功能对象
     * */
    private static TreeMap<String, MEnter> MEnterMap=new TreeMap();
    /**
     * 定义系统角色及入口功能
     * key:系统角色代码；SysRole：系统角色对象
     * */
    private static TreeMap<String,SysRole> SysRoleRulerMap = new TreeMap<>();
    /**
     * 定义系统权限与功能对象的对应关系
     * */
    private static TreeMap<String,MEnter> SysRulerMEnterMap = new TreeMap<>();
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
     //   logFileMsg2(busiExp.getMessage(),"logException",1);
        busiExp.printException(this,"logException");
    }
    @Override
    public String getLoggername(){return loggername;}
    @Override
    public String getLogrootpath(){return logrootpath;}

    /**
     * 通过本组件输出一条日志信息到文件,同时打印到控制台
     * */
    public void logFileMsg(String logMsg) {
        logFileMsg2(logMsg,"logFileMsg",1);
    }
    public void logFileMsg2(String logMsg,String methodName,int offLine) {
        String toFileMsg=BaseNnte.outConsoleLog(logMsg,methodName,offLine);
        if (StringUtils.isNotEmpty(loggername)) {
            FileLogUtil.WriteLogToFile(loggername, logrootpath, toFileMsg);
        }
    }
    /**
     * 设置有日志属性注解的组件的日志属性
     * */
    public static void loadComponentBusiLogAttr(){
     /*   Map<String,Object> beans = SpringContextHolder.getApplicationContext().getBeansWithAnnotation(BusiLogAttr.class);
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
        BaseNnte.outConsoleLog("设置组件日志属性["+size+"]："+names.toString());*/
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
                }
            }else if (typeName.indexOf(DBSchemaBase.SchemaColType.D.getVal())>=0){

            }
        }
        return true;
    }
    /**
     * 装载系统的模块入口定义及系统权限定义
     * */
    public static void loadSystemFuntionEnters(Map<String, Object> SystemRoleMap) throws BusiException{
        ApplicationContext sch=SpringContextHolder.getApplicationContext();
        String[] names=sch.getBeanDefinitionNames();
        for(String beanName:names){
            Object instanceBody=sch.getBean(beanName);
            Method[] methods=instanceBody.getClass().getDeclaredMethods();
            for(Method m:methods){
                ModuleEnter feAnno=m.getAnnotation(ModuleEnter.class);
                if (feAnno!=null){
                    MEnter fe=new MEnter(feAnno.path(),feAnno.name(),feAnno.desc(),feAnno.sysRole(),
                            feAnno.roleRuler(),feAnno.appCode(),feAnno.moduleCode(),feAnno.moduleVersion());
                    if (StringUtils.isEmpty(fe.getPath()))
                        throw new BusiException("功能模块路径为空");
                    if (MEnterMap.get(fe.getPath())!=null)
                        throw new BusiException("功能模块路径重复:"+fe.getPath());
                    MEnterMap.put(fe.getPath(),fe);
                    if (StringUtils.isEmpty(fe.getRoleRuler()))
                        throw new BusiException("功能模块权限为空:PATH="+fe.getPath());
                    if (SysRulerMEnterMap.get(fe.getRoleRuler())!=null)
                        throw new BusiException("功能模块权限重复:"+fe.getRoleRuler());
                    SysRulerMEnterMap.put(fe.getRoleRuler(),fe);
                    String roleCode = fe.getSysRole();
                    if (StringUtils.isNotEmpty(roleCode)){
                        String roleName=StringUtils.defaultString(SystemRoleMap.get(roleCode));
                        if (StringUtils.isNotEmpty(roleName)){
                            SysRole sr=SysRoleRulerMap.get(roleCode);
                            if (sr==null){
                                SysRole newSr=new SysRole();
                                newSr.setRoleCode(roleCode);
                                newSr.setRoleName(roleName);
                                Map<String,String> rulerMap = new HashMap<>();
                                newSr.setRulerMap(rulerMap);
                                SysRoleRulerMap.put(roleCode,newSr);
                                sr = newSr;
                            }
                            sr.getRulerMap().put(fe.getRoleRuler(),fe.getName());
                        }
                    }
                }
            }
        }
        BaseNnte.outConsoleLog("加载系统入口函数信息......("+MEnterMap.size()+")");
    }
    /**
     * 取得系统功能入口函数定义列表
     * */
    public static List<MEnter> getSystemModuleEnters(){
        if (MEnterMap.size()>0){
            List<MEnter> retList=new ArrayList<>();
            Iterator it=MEnterMap.values().iterator();
            while(it.hasNext()){
                retList.add((MEnter)it.next());
            }
            return retList;
        }
        return null;
    }
    /**
     * 取得模块的定义信息
     * */
    public static MEnter getSystemMEnter(String path){
        if (MEnterMap.size()>0){
            return MEnterMap.get(path);
        }
        return null;
    }
    /**
     * 通过权限码查询功能路径
     * */
    public static String getPathByRuler(String ruler){
        MEnter menter=SysRulerMEnterMap.get(ruler);
        if (menter!=null){
            return menter.getPath();
        }
        return null;
    }
    /**
     * 取得系统角色权限功能列表
     * */
    public static List<KeyValue> getSystemRoleRulerList(String sysRoleCode){
        Map<String,String> rulerMap=getSystemRoleRulerMap(sysRoleCode);
        if (rulerMap!=null) {
            List<KeyValue> retList = new ArrayList<>();
            for (String code : rulerMap.keySet())
                retList.add(new KeyValue(code, rulerMap.get(code)));
            return retList;
        }
        return null;
    }
    /**
     * 取得系统角色权限功能MAP
     * */
    public static Map<String,String> getSystemRoleRulerMap(String sysRoleCode){
        SysRole sr=SysRoleRulerMap.get(sysRoleCode);
        if (sr!=null && sr.getRulerMap()!=null && sr.getRulerMap().size()>0){
            return sr.getRulerMap();
        }
        return null;
    }
    /**
     * 通过接口打印日志：INFO
     * */
    public static void logInfo(ExpLogInterface logInterface,String info){
        if (logInterface!=null){
            String toFileMsg=BaseNnte.outConsoleLog(info,"logInfo",1);
            if (StringUtils.isNotEmpty(logInterface.getLoggername())) {
                FileLogUtil.WriteLogToFile(logInterface.getLoggername(),
                        logInterface.getLogrootpath(), toFileMsg);
            }
        }
    }
    /**
     * 通过接口打印日志：WARN
     * */
    public static void logWarn(ExpLogInterface logInterface,String warn){
        if (logInterface!=null) {
            BusiException be = new BusiException(3000,warn ,BusiException.ExpLevel.WARN);
            be.printException(logInterface,"logWarn");
        }
    }
    /**
     * 通过接口打印日志：ERROR
     * */
    public static void logError(ExpLogInterface logInterface,Exception e){
        if (logInterface!=null) {
            BusiException be = new BusiException(e,3000, BusiException.ExpLevel.ERROR);
            be.printException(logInterface,"logError");
        }
    }
}
