package com.nnte.basebusi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 系统模块对象
 * */
@Getter
@Setter
@NoArgsConstructor
public class SysModel {
    private String modelCode;   //模块代码
    private String modelVersion;//模块版本
    private String modelName;   //模块名称--通过数据字典加载
    private List<LibType> libTypeList;  //模块数据字典分类定义
}
