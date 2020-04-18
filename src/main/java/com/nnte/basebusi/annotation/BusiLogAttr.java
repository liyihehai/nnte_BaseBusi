package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
/***
 * 业务日志属性注解 李毅 2020/4/19
 * 加载方法为 BaseBusiComponent.loadComponentBusiLogAttr()
 *
 * 标识业务组件日志属性,用于自定义日志的打印位置，不定义本
 * 注解的组件默认日志打印位置是其组件名称，本注解目的是
 * 汇总有限组件的日志到同一文件内
 */
public @interface BusiLogAttr {
    String value() default  "BusiCommonLog";
}
