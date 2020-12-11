package com.nnte.basebusi.annotation;
/**
 * 实现本接口的组件应该在LoadModelLibType()函数中将模块
 * 定义的数据字典类型加载到静态对象SysModel中，SysModel可通过静态
 * 函数 SysModel getSysModel(String modelCode)获得，加载时需要设置
 * SysModel 的libTypeList 和 modelName
 * */
public interface LoadModelLibTypeInterface {
    void LoadModelLibType();
}
