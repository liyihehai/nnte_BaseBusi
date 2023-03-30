package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.basebusi.annotation.RootConfigProperties;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.utils.BeanUtils;
import com.nnte.framework.utils.FileUtil;
import com.nnte.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.lang.reflect.Field;


@Component
public class BeanListenerProcessor extends BaseNnte implements BeanPostProcessor {
    private static Logger log = LoggerFactory.getLogger(BeanListenerProcessor.class);
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void setFieldParamValue(Object property,String masked,Field f,Object bean,String fName) throws Exception{
        String propertyString = property.toString();
        String propertyValue = propertyString;
        if (propertyString.indexOf(masked) >= 0) {
            propertyValue = propertyString.replaceAll(masked, "");
            propertyString = masked;
        }
        f.setAccessible(true); // 设置属性是可以访问的
        BeanUtils.setFeildValue(propertyValue, f, bean);
        outLogInfo("加载配置参数" + fName + "=" + propertyString);
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        String className = clazz.getSimpleName();
        outLogDebug("设置组件[" + className + "]创建完成!");
        final String masked = "<masked>";
        RootConfigProperties rootConfigProperties = bean.getClass().getAnnotation(RootConfigProperties.class);
        if (rootConfigProperties != null) {
            //如果组件设置了从配置文件自动获取参数
            String jarPath = FileUtil.toUNIXpath(System.getProperty("user.dir"));
            String configFile = jarPath + "/config/" + rootConfigProperties.fileName();
            outLogInfo("config properties File=" + configFile);
            try {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(configFile));
                if (resource.exists()) {
                    EncodedResource encodedResource = new EncodedResource(resource, "utf-8");// UTF-8
                    ResourcePropertySource rps = new ResourcePropertySource(encodedResource);
                    if (rootConfigProperties.superSet()) {//如果含父类字段
                        for (String propertyName : rps.getPropertyNames()) {
                            String prefix = rootConfigProperties.prefix();
                            if (propertyName.indexOf(prefix)!=0)//此处需要判断前缀
                                    continue;
                            Object property = rps.getProperty(propertyName);
                            if (property != null) {
                                String fName= StringUtils.right(propertyName,propertyName.length()-prefix.length()-1);
                                Field f = BeanUtils.getFieldByName(clazz, fName);
                                if (f != null) {
                                    setFieldParamValue(property,masked,f,bean,fName);
                                }
                            }
                        }
                    } else {
                        Field[] fields = clazz.getDeclaredFields();//不含父类字段
                        for (int i = 0; i < fields.length; i++) {
                            String param = fields[i].getName();
                            String paramName = rootConfigProperties.prefix() + "." + param;
                            Object property = rps.getProperty(paramName);
                            if (property != null) {
                                setFieldParamValue(property,masked,fields[i],bean,param);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                outLogExp(e);
            }
        }
        if (bean instanceof BaseNnte) {
            BusiLogAttr logAttr = bean.getClass().getAnnotation(BusiLogAttr.class);
            if (logAttr != null) {
                log.debug("设置组件[" + className + "]日志属性：" + logAttr.value());
            }
        }
        return bean;
    }
}
