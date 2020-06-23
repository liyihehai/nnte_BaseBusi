package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

/**
 * 浮点或双精度检测注解，本注解用于属性;
 * colName字段名称
 * maxVal指定数值不能超过的值
 * minVal指定数值不能小于的值
 * nullvalide指定指端是否能够为null,默认为true
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited

public @interface FloatCheck {
    String colName();
    double maxVal() default Double.MAX_VALUE;
    double minVal() default Double.MIN_VALUE;
    boolean nullValid() default true;
}
