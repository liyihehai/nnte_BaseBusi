package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.framework.base.BaseNnte;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


@Component
public class BeanListenerProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String className=bean.getClass().getSimpleName();
        BaseNnte.outConsoleLog("设置组件[" + className + "]创建完成!");
        BusiLogAttr logAttr = bean.getClass().getAnnotation(BusiLogAttr.class);
        if (logAttr!=null && bean instanceof BaseBusiComponent){
            ((BaseBusiComponent) bean).setLoggerName(logAttr.value());
            BaseNnte.outConsoleLog("设置组件["+className+"]日志属性："+logAttr.value());
        }
        return bean;
    }
}
