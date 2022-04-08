package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface RootConfigProperties {
    String fileName();  //配置文件名,系统会自动加上 当前jar路径/congfig/...properties
    String prefix();    //前缀
    boolean superSet() default false;//是否设置父类属性
}
