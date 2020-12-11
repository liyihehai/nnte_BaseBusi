package com.nnte.basebusi.annotation;

import com.nnte.basebusi.entity.MEnter;

import java.util.List;
import java.util.Map;

/**
 * 应用初始化接口
 * */
public interface AppInitInterface {
    void onRegisterFunctions(String appCode, String appName, Map<String, String> moduleMap,List<MEnter> functionModuleList);
}
