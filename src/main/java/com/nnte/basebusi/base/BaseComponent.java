package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.*;
import com.nnte.basebusi.entity.*;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.annotation.ConfigLoad;
import com.nnte.framework.annotation.DBSchemaInterface;
import com.nnte.framework.base.*;
import com.nnte.framework.entity.KeyValue;
import com.nnte.framework.utils.*;
import com.zaxxer.hikari.HikariConfig;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseComponent extends BaseBusi {
    /**
     * 定义系统功能入口函数
     * key:功能路径；MEnter：功能对象
     */
    private static TreeMap<String, MEnter> MEnterMap = new TreeMap();
    /**
     * 定义系统角色及入口功能
     * key:系统角色代码；SysRole：系统角色对象
     */
    private static TreeMap<String, SysRole> SysRoleRulerMap = new TreeMap<>();
    /**
     * 定义系统权限与功能对象的对应关系
     */
    private static TreeMap<String, MEnter> SysRulerMEnterMap = new TreeMap<>();
    /**
     * 定义系统模块集合
     */
    private static TreeMap<String, SysModel> SysModelMap = new TreeMap<>();
    /**
     * 取APP应用程序本地配置接口，通过本接口使组件可以反向取得应用程序的配置数据
     */
    @ConfigLoad
    public ConfigInterface appConfig;

    /**
     * 检测对象属性的值是否正确，本函数一般用于输入检测
     */
    public static boolean checkModelFields(Object model) throws BusiException {
        Field[] fields = model.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String typeName = f.getGenericType().getTypeName();
            if (typeName.indexOf(DBSchemaBase.SchemaColType.I.getVal()) >= 0 ||
                    typeName.indexOf(DBSchemaBase.SchemaColType.L.getVal()) >= 0) {
                IntegerCheck integerCheck = f.getAnnotation(IntegerCheck.class);
                if (integerCheck != null) {
                    String colName = integerCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval = f.get(model);
                        if (!integerCheck.nullValid()) {//如果字段值不能为null
                            if (fval == null)
                                throw new BusiException(colName + "不能为null");
                        }
                        if (fval != null) {
                            Integer ival = Integer.valueOf(fval.toString());
                            if (ival > integerCheck.maxVal())
                                throw new BusiException(colName + "不能大于" + integerCheck.maxVal());
                            if (ival < integerCheck.minVal())
                                throw new BusiException(colName + "不能小于" + integerCheck.minVal());
                            if (integerCheck.inVals() != null && integerCheck.inVals().length > 0) {
                                boolean isfinded = false;
                                for (int iv : integerCheck.inVals()) {
                                    if (ival.equals(iv)) {
                                        isfinded = true;
                                        break;
                                    }
                                }
                                if (!isfinded)
                                    throw new BusiException(colName + "不在指定范围内");
                            }
                        }
                    } catch (IllegalAccessException ie) {
                        throw new BusiException(colName + "访问错误!" + ie.getMessage());
                    }
                }
            } else if (typeName.indexOf(DBSchemaBase.SchemaColType.S.getVal()) >= 0) {
                StringCheck stringCheck = f.getAnnotation(StringCheck.class);
                if (stringCheck != null) {
                    String colName = stringCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval = f.get(model);
                        if (!stringCheck.nullValid()) {//如果字段值不能为null
                            if (fval == null || StringUtils.isEmpty(fval.toString()))
                                throw new BusiException(colName + "不能为空");
                        }
                        if (stringCheck.maxLen() > 0) {//如果字段长度有限制
                            if (fval != null && fval.toString().length() > stringCheck.maxLen())
                                throw new BusiException(colName + "长度不能超过" + stringCheck.maxLen());
                        }
                        if (StringUtils.isNotEmpty(stringCheck.dateFormat())) {
                            //如果需要校验时间格式
                            String reg = DateUtils.DateFmtRegMap.get(stringCheck.dateFormat());
                            if (StringUtils.isEmpty(reg))
                                throw new BusiException(colName + "注解时间格式定义不正确");
                            Pattern p = Pattern.compile(reg);
                            Matcher m = p.matcher(StringUtils.defaultString(fval));
                            if (!m.matches())
                                throw new BusiException(colName + "时间格式不正确");
                        }
                    } catch (IllegalAccessException ie) {
                        throw new BusiException(colName + "访问错误!" + ie.getMessage());
                    }
                }
            } else if (typeName.indexOf(DBSchemaBase.SchemaColType.F.getVal()) >= 0) {
                FloatCheck floatCheck = f.getAnnotation(FloatCheck.class);
                if (floatCheck != null) {
                    String colName = floatCheck.colName();
                    try {
                        f.setAccessible(true);
                        Object fval = f.get(model);
                        if (!floatCheck.nullValid()) {//如果字段值不能为null
                            if (fval == null)
                                throw new BusiException(colName + "不能为null");
                        }
                        if (fval != null) {
                            Double dval = Double.valueOf(fval.toString());
                            if (dval > floatCheck.maxVal())
                                throw new BusiException(colName + "不能大于" + floatCheck.maxVal());
                            if (dval < floatCheck.minVal())
                                throw new BusiException(colName + "不能小于" + floatCheck.minVal());
                        }
                    } catch (IllegalAccessException ie) {
                        throw new BusiException(colName + "访问错误!" + ie.getMessage());
                    }
                }
            } else if (typeName.indexOf(DBSchemaBase.SchemaColType.D.getVal()) >= 0) {

            }
        }
        return true;
    }

    /**
     * 装载系统的模块入口定义及系统权限定义
     */
    public static void loadSystemFuntionEnters() throws BusiException {
        ApplicationContext sch = SpringContextHolder.getApplicationContext();
        String[] names = sch.getBeanDefinitionNames();
        for (String beanName : names) {
            Object instanceBody = sch.getBean(beanName);
            Method[] methods = instanceBody.getClass().getDeclaredMethods();
            for (Method m : methods) {
                ModuleEnter feAnno = m.getAnnotation(ModuleEnter.class);
                if (feAnno != null) {
                    MEnter fe = new MEnter(feAnno.path(), feAnno.name(), feAnno.desc(), feAnno.sysRole(),
                            feAnno.roleRuler(), AppRegistry.getAppCode(), feAnno.moduleCode(), feAnno.moduleVersion());
                    if (StringUtils.isEmpty(fe.getPath()))
                        throw new BusiException("功能模块路径为空");
                    if (MEnterMap.get(fe.getPath()) != null)
                        throw new BusiException("功能模块路径重复:" + fe.getPath());
                    MEnterMap.put(fe.getPath(), fe);
                    if (StringUtils.isEmpty(fe.getRoleRuler()))
                        throw new BusiException("功能模块权限为空:PATH=" + fe.getPath());
                    if (SysRulerMEnterMap.get(fe.getRoleRuler()) != null)
                        throw new BusiException("功能模块权限重复:" + fe.getRoleRuler());
                    SysRulerMEnterMap.put(fe.getRoleRuler(), fe);
                    String roleCode = fe.getSysRole();
                    if (StringUtils.isNotEmpty(roleCode)) {
                        String roleName = StringUtils.defaultString(AppRegistry.getSysRoleName(roleCode));
                        if (StringUtils.isEmpty(roleName)) {
                            roleName = roleCode;
                            AppRegistry.setSysRoleName(roleCode, roleName);
                        }
                        SysRole sr = SysRoleRulerMap.get(roleCode);
                        if (sr == null) {
                            SysRole newSr = new SysRole();
                            newSr.setRoleCode(roleCode);
                            newSr.setRoleName(roleName);
                            Map<String, String> rulerMap = new HashMap<>();
                            newSr.setRulerMap(rulerMap);
                            SysRoleRulerMap.put(roleCode, newSr);
                            sr = newSr;
                        }
                        sr.getRulerMap().put(fe.getRoleRuler(), fe.getName());
                    }
                    if (SysModelMap.get(fe.getModuleCode()) == null) {
                        //需要初始化模块定义
                        String modelName = AppRegistry.getAppModuleName(fe.getModuleCode());
                        if (StringUtils.isEmpty(modelName))
                            throw new BusiException("模块编号" + fe.getModuleCode() + "没有通过应用注册!");
                        SysModel newSysModel = new SysModel();
                        newSysModel.setModelCode(fe.getModuleCode());
                        newSysModel.setModelVersion(fe.getModuleVersion());
                        newSysModel.setModelName(modelName);
                        SysModelMap.put(fe.getModuleCode(), newSysModel);
                    }
                }
            }
        }
        //守护线程组件自动注册
        BaseLog.logInfo("守护线程组件自动注册......");
        for (String beanName : names) {
            WatchComponent watchComponent = SpringContextHolder.getBean(WatchComponent.class);
            Object instanceBody = sch.getBean(beanName);
            if (instanceBody instanceof WatchInterface) {
                WatchAttr watchAttr = instanceBody.getClass().getAnnotation(WatchAttr.class);
                int index = 0;
                int execTimes = -1;
                if (watchAttr != null) {
                    index = watchAttr.value();
                    execTimes = watchAttr.execTimes();
                }
                watchComponent.registerWatchItem((WatchInterface) instanceBody, index, execTimes);
            }
        }
        //启动实现了LoadModelLibTypeInterface接口的组件加载模块LibType
        BaseLog.logInfo("组件加载模块LibType......");
        for (String beanName : names) {
            Object instanceBody = sch.getBean(beanName);
            if (instanceBody instanceof LoadModelLibTypeInterface) {
                ((LoadModelLibTypeInterface) instanceBody).LoadModelLibType();
            }
        }
        //AppInitInterface接口启动模块注册回调
        BaseLog.logInfo("接口启动模块注册回调......");
        if (AppRegistry.getAppInitInterface() != null) {
            AppRegistry.getAppInitInterface().onRegisterFunctions(AppRegistry.getAppCode(),
                    AppRegistry.getAppName(), AppRegistry.getAppModuleNameMap(), getSystemModuleEnters());
        }
        //各个模块执行初始化
        BaseLog.logInfo("各个模块执行初始化......");
        for (String beanName : names) {
            Object instanceBody = sch.getBean(beanName);
            if (instanceBody instanceof ModuleInterface) {
                ModuleInterface mi = (ModuleInterface) instanceBody;
                BaseLog.logInfo("模块["+mi.getModuleJarName()+"]执行初始化......");
                mi.initModule();
            }
        }
        BaseLog.logInfo("加载系统入口函数信息......(" + MEnterMap.size() + ")");
    }

    /**
     * 取得系统功能入口函数定义列表
     */
    public static List<MEnter> getSystemModuleEnters() {
        if (MEnterMap.size() > 0) {
            List<MEnter> retList = new ArrayList<>(MEnterMap.values());
            return retList;
        }
        return null;
    }

    /**
     * 取得模块的定义信息
     */
    public static MEnter getSystemMEnter(String path) {
        if (MEnterMap.size() > 0) {
            return MEnterMap.get(path);
        }
        return null;
    }

    /**
     * 通过权限码查询功能路径
     */
    public static String getPathByRuler(String ruler) {
        MEnter menter = SysRulerMEnterMap.get(ruler);
        if (menter != null) {
            return menter.getPath();
        }
        return null;
    }

    /**
     * 取得系统角色权限功能列表
     */
    public static List<KeyValue> getSystemRoleRulerList(String sysRoleCode) {
        Map<String, String> rulerMap = getSystemRoleRulerMap(sysRoleCode);
        if (rulerMap != null) {
            List<KeyValue> retList = new ArrayList<>();
            for (String code : rulerMap.keySet())
                retList.add(new KeyValue(code, rulerMap.get(code)));
            return retList;
        }
        return null;
    }

    /**
     * 取得系统角色权限功能MAP
     */
    public static Map<String, String> getSystemRoleRulerMap(String sysRoleCode) {
        SysRole sr = SysRoleRulerMap.get(sysRoleCode);
        if (sr != null && sr.getRulerMap() != null && sr.getRulerMap().size() > 0) {
            return sr.getRulerMap();
        }
        return null;
    }

    /**
     * 取得模块序列
     */
    public static List<SysModel> getSysModelList() {
        return new ArrayList<>(SysModelMap.values());
    }

    /**
     * 取得模块MAP
     */
    public static Map<String, SysModel> getSysModelMap() {
        return new HashMap<>(SysModelMap);
    }

    /**
     * 取得模块定义
     */
    public static SysModel getSysModel(String modelCode) {
        return SysModelMap.get(modelCode);
    }

    /**
     * 创建一个数据源
     */
    public static void createDataBaseSource(DBSchemaInterface DBBase,
                                            String DBSrcName,
                                            boolean isDefault,
                                            DBSrcConfig srcConfig) throws BusiException {
        DynamicDatabaseSourceHolder dynamicDatabaseSourceHolder = SpringContextHolder.getBean(DynamicDatabaseSourceHolder.class);
        String[] dbTypes = dynamicDatabaseSourceHolder.queryDBTypes();
        if (dbTypes == null || dbTypes.length <= 0)
            dynamicDatabaseSourceHolder.loadDBSchemaInterface();
        dbsourceSqlSessionFactory dbsf = dynamicDatabaseSourceHolder.getDBsrcSSF(DBSrcName);
        if (dbsf != null)
            throw new BusiException(10002, "已经存在名称为：" + DBSrcName + "的数据源", LogUtil.LogLevel.error);
        if (isDefault) {
            dbsf = dynamicDatabaseSourceHolder.getDefaultDBsrcSSF();
            if (dbsf != null)
                throw new BusiException(10003, "应用不能定义多个默认数据源", LogUtil.LogLevel.error);
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(srcConfig.getDBDriverClassName());
        config.setJdbcUrl(DBBase.makeJDBCUrl(srcConfig.getDBIp(), NumberUtil.getDefaultLong(srcConfig.getDBPort()),
                srcConfig.getDBSchema()));
        config.setUsername(srcConfig.getDBUser());
        config.setPassword(srcConfig.getDBPassword());
        config.setMinimumIdle(srcConfig.getMinimumIdle());
        config.setMaximumPoolSize(srcConfig.getMaximumPoolSize());
        config.setIdleTimeout(srcConfig.getIdleTimeout());
        config.setConnectionTestQuery(srcConfig.getConnectionTestQuery());
        dynamicDatabaseSourceHolder.initDataBaseSource(DBSrcName, config, isDefault);
    }
    /**
     * 给配置文件加上默认的config路径
     * */
    public static String getRootConfigPropertiesPath(String properties){
        String jarPath= FileUtil.toUNIXpath(System.getProperty("user.dir"));
        if (jarPath.indexOf(0)!='/')
            jarPath = "/" + jarPath;
        String path = StringUtils.pathAppend(jarPath,"config");
        return StringUtils.pathAppend(path,properties);
    }
}
