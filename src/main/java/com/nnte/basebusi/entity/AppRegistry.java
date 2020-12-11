package com.nnte.basebusi.entity;

import com.nnte.basebusi.annotation.AppInitInterface;

import java.util.HashMap;
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
    /**
     * 系统模块定义
     * 模块定义为可独立交付给客户的一组功能
     * */
    private static Map<String,String> moduleMap = new HashMap<>();

    public static String getAppCode(){return App_Code;}
    public static String getAppName(){return App_Name;}
    public static AppInitInterface getAppInitInterface(){return appInitInterface;}

    public static void registry(String code,String name,AppInitInterface appInit){
        App_Code = code;
        App_Name = name;
        appInitInterface = appInit;
    }
    public static Map<String,String> getModuleMap(){
        return moduleMap;
    }

    /**
     * 注册一个模块定义
     * */
    public static void registryModel(String modelCode,String modelName){
        moduleMap.put(modelCode,modelName);
    }
}
