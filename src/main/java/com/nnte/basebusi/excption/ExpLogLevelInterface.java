package com.nnte.basebusi.excption;
/**
 * 异常通过本接口查询系统临时业务日志等级
 * */
public interface ExpLogLevelInterface {
    BusiException.ExpLevel getTempExpLevel();
}
