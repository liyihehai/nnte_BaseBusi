package com.nnte.basebusi.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.nnte.basebusi.entity.ResponseResult;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.entity.KeyValue;
import com.nnte.framework.utils.FreeMarkertUtil;
import com.nnte.framework.utils.JsonUtil;
import com.nnte.framework.utils.MapUtil;
import com.nnte.framework.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseController extends BaseBusi{

    public void printMsg(HttpServletResponse resp, String json) {
        try {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().print(json);
            resp.getWriter().close();
        } catch (Exception e) {
            onException(e);
        }
        return;
    }

    public void printLoadListMsg(HttpServletResponse resp, Integer sEcho, Integer total, String msg) {
        try {
            if (total != null) {
                msg = "{\"sEcho\":" + sEcho + ",\"iTotalRecords\":" + total + ",\"iTotalDisplayRecords\":" + total + ",\"aaData\":" + msg + "}";
            }
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().println(msg);
            resp.getWriter().close();
        } catch (Exception e) {
            onException(e);
        }
        return;
    }

    public static String getKeyValListOption(List<KeyValue> list, String selctOption) {
        if (list == null)
            return "";
        StringBuffer retBuf = new StringBuffer();
        for (KeyValue kv : list) {
            String selOption = "";
            if (kv.getKey().equals(selctOption))
                selOption = " selected=\"selected\"";
            retBuf.append("<option value=\"").append(kv.getKey()).append("\">")
                    .append(kv.getValue()).append(selOption).append("</option>\r\n");
        }
        return retBuf.toString();
    }

    /**
     * 从请求中按参数名获取参数值，先从reqParamObj中获取
     * reqParamObj可以是Map<String,Object>或JSONObject
     */
    public static Object getRequestParam(HttpServletRequest request, Object reqParamObj, String paramName) {
        if (reqParamObj != null) {
            String classname = reqParamObj.getClass().getSimpleName();
            if (classname.equalsIgnoreCase("LinkedHashMap")) {
                Map<String, Object> reqMap = (Map<String, Object>) reqParamObj;
                return reqMap.get(paramName);
            } else if (classname.equalsIgnoreCase("JsonNode")) {
                JsonNode reqJson = (JsonNode) reqParamObj;
                JsonUtil.JNode jnode=JsonUtil.createJNode(reqJson);
                return jnode.get(paramName);
            }
        }
        if (request != null)
            return request.getParameter(paramName);
        return null;
    }
    /**
     * @Title: getIpAddr
     * @Description: 获得用户真实IP地址
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {

            ipAddress = request.getHeader("X-Real-IP");
            if(StringUtils.isEmpty(ipAddress)){
                ipAddress = request.getRemoteAddr();
            }
            if(!StringUtils.isEmpty(ipAddress)){
                if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                    //根据网卡取本机配置的IP
                    InetAddress inet=null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException uhe) {
                        BaseLog.outLogExp(uhe);
                        return request.getRemoteAddr();
                    }
                    ipAddress= inet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
    /**
     * 直接通过FTL渲染页面返回给请求端
     * */
    public static void ResponsByFtl(HttpServletRequest request,HttpServletResponse response,
                             Map<String,Object> paramMap,String ftlName) throws BusiException{
        try {
            String content = FreeMarkertUtil.getFreemarkerFtl(request, request.getServletContext(),
                    FreeMarkertUtil.pathType.cls, paramMap, ftlName);
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(content);
            response.getWriter().close();
        }catch (IOException ioe){
            throw new BusiException(ioe,1005);
        }
    }
    /**
     * 打印对象的json给请求端
     * */
    public static void printJsonObject(HttpServletResponse response,Object obj) throws BusiException{
        try {
            String json = JsonUtil.beanToJson(obj);
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(json);
            response.getWriter().close();
        } catch (IOException ioe) {
            throw new BusiException(ioe,1005);
        }
    }

    public static void setParamMapDataEnv(HttpServletRequest request, Map<String, Object> paramMap) {
        paramMap.put("envData", request.getAttribute("envData"));
    }

    /**
     * 从请求参数中拷贝属性值，依据_dim字段名称进行拷贝
     * _dim属性不能是对象属性
     * */
    public static void copyFromRequestParams(HttpServletRequest request, Object _dim) throws BusiException{
        Field[] fields = _dim.getClass().getDeclaredFields();
        Map<String,Object> tmpMap = new HashMap<>();
        try {
            for (Field field : fields) {
                field.setAccessible(true); // 设置属性是可以访问的
                Object paramObj=getRequestParam(request,null,field.getName());
                tmpMap.put(field.getName(),paramObj);
            }
            if (tmpMap.size()>0){
                MapUtil.copyFromSrcMap(tmpMap,_dim);
            }
        } catch (Exception e) {
            throw new BusiException(e,9999);
        }
    }

    public static ResponseResult error(String message){
        return error("1",message);
    }

    public static ResponseResult error(String errCode,String message){
        ResponseResult ret = new ResponseResult();
        ret.setSuccess(false);
        ret.setShowType(0);
        ret.setErrorCode(errCode);
        ret.setErrorMessage(message);
        return ret;
    }

    public static ResponseResult success(String message){
        ResponseResult ret = new ResponseResult();
        ret.setSuccess(true);
        ret.setShowType(0);
        ret.setErrorCode("0");
        ret.setErrorMessage(message);
        return ret;
    }

    public static ResponseResult success(String message,Object data){
        ResponseResult ret = success(message);
        ret.setData(data);
        return ret;
    }

    /**
     * 统一返回错误结果
     * */
    public ResponseResult onException(Exception e){
        if (e instanceof BusiException){
            BusiException be = (BusiException)e;
            BaseLog.outLogExp(be);
            return error(be.getExpCode().toString(),e.getMessage());
        }else{
            BusiException newBe = new BusiException(e);
            BaseLog.outLogExp(newBe);
            return error("-1",e.getMessage());
        }
    }

    public static void setCors(HttpServletRequest request, HttpServletResponse response){
        // 不使用*，自动适配跨域域名，避免携带Cookie时失效
        String origin = request.getHeader("Origin");
        if(StringUtils.isNotEmpty(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        // 自适应所有自定义头
        String headers = request.getHeader("Access-Control-Request-Headers");
        if(StringUtils.isNotEmpty(headers)) {
            response.setHeader("Access-Control-Allow-Headers", headers);
            response.setHeader("Access-Control-Expose-Headers", headers);
        }

        // 允许跨域的请求方法类型
        response.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,OPTIONS,DELETE");
        // 预检命令（OPTIONS）缓存时间，单位：秒
        response.setHeader("Access-Control-Max-Age", "1800");
        // 明确许可客户端发送Cookie，不允许删除字段即可
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
