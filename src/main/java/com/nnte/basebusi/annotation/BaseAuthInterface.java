package com.nnte.basebusi.annotation;

import com.nnte.basebusi.base.BaseBusiComponent;
import com.nnte.basebusi.entity.MEnter;
import com.nnte.basebusi.entity.OperatorInfo;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.entity.AuthTokenDetailsDTO;
import com.nnte.framework.utils.DateUtils;
import com.nnte.framework.utils.JwtUtils;
import com.nnte.framework.utils.LogUtil;
import com.nnte.framework.utils.StringUtils;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * 基础权限接口，需要进行TOKEN权限限制的应用的主组件应该实现它
 * */
public interface BaseAuthInterface {
    /**
     * 创建一个Token的JwtUtils
     * secretKey = 秘钥
     * sign      = 加密类型
     * expireTime= 超时毫秒数 : 默认1小时 60*60*1000
     * */
    default JwtUtils createTokenJwt() {
        String secretKey = "bm50ZSQyMDIwXkhTMjU2KmFiY2RlZmdoaWpAMTIzNDU2Nzg5MA==";
        String sign = "HS256";
        String expireTime = "3600000";
        JwtUtils jwt = new JwtUtils();
        jwt.initJwtParams(SignatureAlgorithm.forName(sign), secretKey, expireTime);
        return jwt;
    }

    default OperatorInfo queryOperatorInfo(OperatorInfo srcOpe){
        srcOpe.setOperatorState(1);
        srcOpe.setOperatorType(1);
        return srcOpe;
    }
    default boolean isOperatorStateValid(OperatorInfo srcOpe){
        return srcOpe.getOperatorState()==1;
    }
    /**
     * 校验请求的Token,拦截器调用
     */
    default Map<String, Object> checkRequestToken(String token, String loginIp) throws BusiException {
        Map ret = BaseNnte.newMapRetObj();
        JwtUtils jwt = createTokenJwt();
        try {
            AuthTokenDetailsDTO atd = jwt.parseAndValidate(token);
            if (!atd.getLoginIp().equals(loginIp))
                throw new BusiException("Ip地址不合法");
            OperatorInfo opeInfo = new OperatorInfo();
            opeInfo.setOperatorCode(atd.getUserCode());
            opeInfo.setOperatorName(atd.getUserName());
            opeInfo.setGroupCode(atd.getMerchantCode());
            Date now = new Date();
            Date preExpTime = new Date((now.getTime() + 60 * 60 * 1000));//计算当前时间之后1小时的时间
            if (preExpTime.after(atd.getExpirationDate())) {
                //如果当前时间向后推1小时Token将要到期，要重新生成Token,此功能实现Token自动延期
                opeInfo.setLoginTime(DateUtils.dateToString(now, DateUtils.DF_YMDHMS));
                opeInfo.setToken(jwt.createJsonWebToken(atd));
            } else {
                opeInfo.setLoginTime(DateUtils.dateToString(new Date(atd.getExpirationDate().getTime() - jwt.getExpiredTime()),
                        DateUtils.DF_YMDHMS));
                opeInfo.setToken(token);
            }
            ret.put("OperatorInfo", opeInfo);
        } catch (Exception e) {
            throw new BusiException(e,1009);
        }
        return ret;
    }
    /**
     * 操作员pfo 是否有进入模块 me的权限，默认为true
     * */
    default boolean isOpeModelValid(OperatorInfo pfo,MEnter me) throws BusiException{
        return true;
    }
    /**
     * 校验操作员是否具备请求的模块的权限,拦截器调用
     */
    default void checkRequestModule(OperatorInfo opeInfo, String path) throws BusiException {
        try {
            MEnter me = BaseBusiComponent.getSystemMEnter(path);
            if (me != null) {
                OperatorInfo pfo = queryOperatorInfo(opeInfo);
                if (!isOperatorStateValid(pfo)) {
                    throw new BusiException("操作员状态不合法");
                }
                //如果模块或权限没有定义，只能是超级系统管理员才能进入
                if (StringUtils.isEmpty(me.getSysRole()) || StringUtils.isEmpty(me.getRoleRuler())) {
                    if (pfo.getOperatorType()==1)
                        return;//如果操作员是超级管理员，直接放行
                    throw new BusiException(10101,"只有超级管理员才能进入模块[" + me.getName() + "]", LogUtil.LogLevel.warn);
                } else {
                    //如果模块有权限限制，当前操作员不是超级管理员，则需要校验权限，否则不需要校验
                    if (!(pfo.getOperatorType()==1)) {
                        if (!isOpeModelValid(pfo,me))
                            throw new BusiException(10101,"当前操作员没有权限进入模块[" + me.getName() + "]",
                                    LogUtil.LogLevel.warn);
                    }
                }
            }
        } catch (Exception e) {
            throw new BusiException(10100, "模块权限校验失败(" + e.getMessage() + ")", LogUtil.LogLevel.warn);
        }
    }
}
