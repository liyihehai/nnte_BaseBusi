package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

/**
 * 整形或LONG检测注解，本注解用于属性;
 * colName字段名称
 * maxVal指定数值不能超过的值
 * minVal指定数值不能小于的值
 * inVals如果不为空，指定数值必须在数组范围内
 * nullvalide指定指端是否能够为null,默认为true
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited

public @interface IntegerCheck {
    String colName();
    int maxVal() default Integer.MAX_VALUE;
    int minVal() default Integer.MIN_VALUE;
    int[] inVals() default {};
    boolean nullValid() default true;
}
