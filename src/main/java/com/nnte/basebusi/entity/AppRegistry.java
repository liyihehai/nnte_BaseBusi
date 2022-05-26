package com.nnte.basebusi.entity;

import com.nnte.basebusi.annotation.AppInitInterface;
import com.nnte.framework.entity.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用注册
 * */
public final class AppRegistry {
    /**
     * 定义应用代码及应用名称
     * */
    private static String App_Code;     //应用代码
    private static String App_Name;     //应用名称
    private static AppInitInterface appInitInterface;   //应用初始化接口

    //App相关功能
    public static String getAppCode(){return App_Code;}
    public static String getAppName(){return App_Name;}
    public static AppInitInterface getAppInitInterface(){return appInitInterface;}

    /**
     * 应用初始化前应首先调用本函数
     * */
    public static void registry(String code,String name,AppInitInterface appInit){
        App_Code = code;
        App_Name = name;
        appInitInterface = appInit;
    }
    /**
     * 定义系统角色编号和名称的对应关系
     * */
    private static ConcurrentHashMap<String,String> SysRoleNameMap = new ConcurrentHashMap<>();

    public static void setSysRoleName(String roleCode,String roleName){
        SysRoleNameMap.put(roleCode,roleName);
    }
    public static String getSysRoleName(String roleCode){
        return SysRoleNameMap.get(roleCode);
    }

    public static List<KeyValue> getSysRoleNameList(){
        List<KeyValue> sysRoleNameList = new ArrayList<>();
        SysRoleNameMap.forEach((code,name)->{
            sysRoleNameList.add(new KeyValue(code,name));
        });
        return sysRoleNameList;
    }

    /**
     * 系统模块定义
     * 模块定义为可独立交付给客户的一组功能
     * */
    private static ConcurrentHashMap<String,SysModule> appModuleNameMap = new ConcurrentHashMap<>();

    public static void setAppModuleName(String modelCode,String modelName,boolean isFrameModule){
        SysModule module = new SysModule(modelCode,modelName,isFrameModule);
        appModuleNameMap.put(modelCode,module);
    }

    public static void setAppModuleName(String modelCode,String modelName){
        SysModule module = new SysModule(modelCode,modelName);
        appModuleNameMap.put(modelCode,module);
    }
    public static String getAppModuleName(String modelCode){
        SysModule module=appModuleNameMap.get(modelCode);
        if (module!=null)
            return appModuleNameMap.get(modelCode).getModuleName();
        return "";
    }
    public static SysModule getSysModule(String moduleCode){
        return appModuleNameMap.get(moduleCode);
    }
    public static ConcurrentHashMap<String,SysModule> getAppModuleNameMap(){
        return appModuleNameMap;
    }
}
