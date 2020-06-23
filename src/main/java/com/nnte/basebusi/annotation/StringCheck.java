package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

/**
 * 文本检测注解，本注解用于属性;
 * colName字段名称
 * maxLen指定字符串长度不能超过的值
 * nullvalide指定指端是否能够为null,默认为true
 * dateFormat如果字符串代表日期，如果填了格式限制，需要校验时间格式是否合法
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited

public @interface StringCheck {
    String colName();
    int maxLen() default 0;
    String dateFormat() default "";
    boolean nullValid() default true;
}
