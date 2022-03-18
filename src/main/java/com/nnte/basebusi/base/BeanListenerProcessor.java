package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.framework.base.BaseNnte;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


@Component
@BusiLogAttr(BaseBusi.Logger_Name)
public class BeanListenerProcessor extends BaseNnte implements BeanPostProcessor {
    public BeanListenerProcessor(){
        setFrame_loggerName(BaseBusi.Logger_Name);
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class clazz=bean.getClass();
        String className=clazz.getSimpleName();
        outLogInfo("设置组件[" + className + "]创建完成!");
        if (bean instanceof BaseNnte) {
            BaseNnte BaseNntebean = (BaseNnte) bean;
            BusiLogAttr logAttr = bean.getClass().getAnnotation(BusiLogAttr.class);
            if (logAttr != null) {
                String loggerName = logAttr.value();
                BaseNntebean.setFrame_loggerName(loggerName);
                BaseNntebean.outLogInfo("设置组件[" + className + "]日志属性：" + logAttr.value());
            }else
                BaseNntebean.setFrame_loggerName(className);
        }
        return bean;
    }
}
