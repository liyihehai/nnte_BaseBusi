package com.nnte.basebusi.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.entity.KeyValue;
import com.nnte.framework.utils.FreeMarkertUtil;
import com.nnte.framework.utils.JsonUtil;
import com.nnte.framework.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class BaseController {

    public void printMsg(HttpServletResponse resp, String json) {
        try {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().print(json);
            resp.getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
                        uhe.printStackTrace();
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
            throw new BusiException(ioe,1005, BusiException.ExpLevel.ERROR);
        }
    }
}
