package com.nnte.basebusi.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
/**
 * 实现了守护接口的组件通过本注解设置守护进程的启动顺序，
 * 默认为0，最后执行,
 * value = name,表示组件的名称，注册时不能重复
 * cron = 时间调度cron表达式
 * execTimes 表示组件的执行次数，<=-1表示执行次数不限，
 *           0表示不执行，会自动注销
 *           >0 表示实际要执行的次数，每执行一次减1，直到减为0时被注销
 * */
public @interface WatchAttr {
    String value();
    String cron();
    int execTimes() default -1;
}
