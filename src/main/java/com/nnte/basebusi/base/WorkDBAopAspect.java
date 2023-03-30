package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.DBSrcTranc;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.base.BaseService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class WorkDBAopAspect extends BaseNnte{
    /*
    @Autowired
    private DynamicDatabaseSourceHolder dynamicDSHolder;
    */
    /*切点条件：
     * 1:组件必须有@WorkDBAspect注解，
     * 2:方法必须有@DBSrcTranc注解，
     * 3:返回值必须是Map<String,Object> -- （！！！该条件已取消）
     */
    /*
    @Pointcut("@target(com.nnte.framework.annotation.WorkDBAspect) && @annotation(com.nnte.basebusi.annotation.DBSrcTranc) && args(pMap,..) && execution(public java.util.Map<String,Object> *(..))")
    public void WorkDBAopPointCut(Map<String,Object> pMap){ }
    @Around(value = "WorkDBAopPointCut(pMap)")
    public Object doAround(ProceedingJoinPoint pjp, Map<String,Object> pMap) throws Throwable {
    */
    //@Pointcut("@target(com.nnte.framework.annotation.WorkDBAspect) && @annotation(com.nnte.basebusi.annotation.DBSrcTranc) && execution(public java.util.Map<String,Object> *(..))")
    @Pointcut("@target(com.nnte.framework.annotation.WorkDBAspect) && @annotation(com.nnte.basebusi.annotation.DBSrcTranc)")
    public void WorkDBAopPointCut(){ }

    @Around(value = "WorkDBAopPointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        boolean isSetDB = false;
        boolean isNeedRollback =false;
        try {
            Method method = getMethod(pjp);
            DBSrcTranc dbSrcTranc = method.getAnnotation(DBSrcTranc.class);
            String value = dbSrcTranc.value();
            boolean autocommit=dbSrcTranc.autocommit();
            outLogDebug("WorkDBAopPointCut dataSrcName="+value+" start ...");
            BaseService.setThreadLocalSession(value,autocommit);
            isSetDB = true;
            return pjp.proceed();//执行原函数
        }catch (Exception e){
            //遇到任何异常都需要rollback;
            isNeedRollback = true;
            throw e;
        }
        finally {
            if (isSetDB) {
                BaseService.removeThreadLocalSession(isNeedRollback);
                outLogDebug("WorkDBAopPointCut finally ...");
            }
        }
    }

    private Method getMethod(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        try {
            return targetClass.getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
