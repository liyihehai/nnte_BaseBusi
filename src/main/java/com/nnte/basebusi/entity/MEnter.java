package com.nnte.basebusi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 函数入口对象
 * */
public class MEnter {
    private String path;        //入口函数路径
    private String name;        //入口函数名称
    private String params;      //入口函数参数,默认为空
    private String desc;        //入口函数描述
    private String sysRole;     //系统角色 -- 限制进入本模块的系统角色
    private String roleRuler;   //角色权限
}
