package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
/***
 * 系统功能入口注解 李毅 2020/5/3
 * 加载方法为 BaseBusiComponent.loadSystemFuntionEnters()
 *
 * 标识业务功能的入口，装载函数通过本注解加载系统的入口函数列表
 */
public @interface ModuleEnter {
    String path();                  //入口函数路径
    String name();                  //入口函数名称
    String params() default "";     //入口函数参数,默认为空
    String desc();                  //入口函数描述
    /**
     * 模块角色权限，只有符合模块角色及权限的操作员才能进入本模块
     * 如果模块入口不定义模块权限，只有系统管理员才能进入本模块
     * */
    String sysRole() default "";    //系统角色 -- 限制进入本模块的系统角色
    String roleRuler() default "";  //角色权限(权限名称与入口函数名称相同)
}
