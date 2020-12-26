package com.nnte.basebusi.entity;

import com.nnte.basebusi.annotation.AppInitInterface;
import com.nnte.framework.entity.KeyValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 应用注册
 * */
public class AppRegistry {
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

    public static void registry(String code,String name,AppInitInterface appInit){
        App_Code = code;
        App_Name = name;
        appInitInterface = appInit;
    }
    /**
     * 定义系统角色编号和名称的对应关系
     * */
    private static Map<String,String> SysRoleNameMap = new HashMap<>();

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
    private static Map<String,String> appModuleNameMap = new HashMap<>();
    public static void setAppModuleName(String modelCode,String modelName){
        appModuleNameMap.put(modelCode,modelName);
    }
    public static String getAppModuleName(String modelCode){
        return appModuleNameMap.get(modelCode);
    }
    public static Map<String,String> getAppModuleNameMap(){
        return appModuleNameMap;
    }
}
