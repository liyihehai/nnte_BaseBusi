package com.nnte.basebusi.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.nnte.framework.entity.KeyValue;
import com.nnte.framework.utils.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public Object getRequestParam(HttpServletRequest request, Object reqParamObj, String paramName) {
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
}
