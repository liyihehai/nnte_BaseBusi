package com.nnte.basebusi.annotation;
/**
 * 2022/03/28 李毅
 * 模块接口：每个模块jar都需要先生成一个配置组件，该配置组件应实现
 * 本接口，通过本接口，底层组件可以取得模块的信息，并触发模块初始化
 * */
public interface ModuleInterface {
    void initModule();
    String getModuleJarName();
    String getModuleLoggerName();
}
