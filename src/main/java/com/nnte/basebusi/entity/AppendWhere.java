package com.nnte.basebusi.entity;

import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.utils.DateUtils;
import com.nnte.framework.utils.StringUtils;

import java.util.*;

public class AppendWhere {
    public static final String Type_Direct = "direct";
    public static final String Type_like = "like";
    private String whereTxt;
    private String whereType;
    private String colName;
    private Object whereVal;

    public AppendWhere(String type) throws BusiException {
        if (Type_Direct.equals(type) && Type_like.equals(type))
            throw new BusiException(100101,"条件类型设置不正确");
        whereType = type;
    }

    public String getWhereType() {
        return whereType;
    }

    public void setWhereType(String whereType) {
        this.whereType = whereType;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Object getWhereVal() {
        return whereVal;
    }

    public void setWhereVal(Object whereVal) {
        this.whereVal = whereVal;
    }

    public String getWhereTxt() {
        return whereTxt;
    }

    public void setWhereTxt(String whereTxt) {
        this.whereTxt = whereTxt;
    }
    /**
     * 设置日期范围查询条件
     * */
    public static void andDateRange(AppendWhere where,String colName, Date stateDate, Date endDate){
        where.setWhereType(Type_Direct);
        if (stateDate==null && endDate==null)
            return;
        String wTxt = " ";
        if (stateDate!=null)
            wTxt= wTxt + colName + ">='"+ DateUtils.dateToString_full(DateUtils.todayZeroTime(stateDate))+"'";
        if (endDate!=null)
            wTxt= wTxt + " and " + colName + "<='" + DateUtils.dateToString_full(DateUtils.todayNightZeroTime(endDate)) + "'";
        where.setWhereTxt(wTxt);
    }
    /**
     * 向条件队列中增加一条时间范围的条件
     * */
    public static void addDataRangeToWhereList(String[] dateTimes, String colName, List<AppendWhere> appendWhereList) throws BusiException{
        if (dateTimes!=null && dateTimes.length>0){
            AppendWhere dateWhere = new AppendWhere(AppendWhere.Type_Direct);
            Date startTime = DateUtils.todayZeroTime(DateUtils.stringToDate(dateTimes[0]));
            Date endTime = null;
            if (dateTimes.length > 1) {
                endTime = DateUtils.todayNightZeroTime(DateUtils.stringToDate(dateTimes[1]));
            }
            AppendWhere.andDateRange(dateWhere, colName, startTime, endTime);
            appendWhereList.add(dateWhere);
        }
    }
    /**
     * 向条件队列中增加一条Like的文本条件
     * */
    public static void addLikeStringToWhereList(String likeString,String colName, List<AppendWhere> appendWhereList)throws BusiException{
        if (StringUtils.isNotEmpty(likeString)) {
            appendWhereList.add(new AppendWhereLike(colName, likeString));
        }
    }

    private static List<AppendWhere> initParamMapAppendWhereList(Map<String,Object> whereMap){
        List<AppendWhere> appendWhereList = (List<AppendWhere>)whereMap.get("appendWhereList");
        if (appendWhereList==null) {
            appendWhereList = new ArrayList<>();
            whereMap.put("appendWhereList",appendWhereList);
        }
        return appendWhereList;
    }

    public static void andWhereTxtToWhereMap(String wTxt,Map<String,Object> whereMap) throws Exception{
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        AppendWhere where = new AppendWhere(Type_Direct);
        where.setWhereTxt(wTxt);
        appendWhereList.add(where);
    }

    public static void andNumberRangeToWhereMap(Object startNumber,Object endNumber,
                                                String colName,Map<String,Object> whereMap) throws Exception{
        andNumberRangeToWhereMap(startNumber,colName,endNumber,colName,whereMap);
    }
    public static void andNumberRangeToWhereMap(Object startNumber,String startColName,
                                                Object endNumber,String endColName,Map<String,Object> whereMap) throws Exception{
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        if (startNumber!=null){
            String wTxt= startColName + ">="+ startNumber.toString();
            AppendWhere where = new AppendWhere(Type_Direct);
            where.setWhereTxt(wTxt);
            appendWhereList.add(where);
        }
        if (endNumber!=null){
            String wTxt= endColName + "<=" + endNumber.toString();
            AppendWhere where = new AppendWhere(Type_Direct);
            where.setWhereTxt(wTxt);
            appendWhereList.add(where);
        }
    }

    public static void andDateRangeToWhereMap(String colName, Date startDate, Date endDate,Map<String,Object> whereMap) throws Exception{
        andDateRangeToWhereMap(colName,startDate,colName,endDate,whereMap);
    }

    public static void andDateRangeToWhereMap(String startColName, Date startDate,
                                              String endColName, Date endDate,Map<String,Object> whereMap) throws Exception{
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        if (startDate!=null){
            String wTxt= startColName + ">='"+ DateUtils.dateToString_full(DateUtils.todayZeroTime(startDate))+"'";
            AppendWhere where = new AppendWhere(Type_Direct);
            where.setWhereTxt(wTxt);
            appendWhereList.add(where);
        }
        if (endDate!=null){
            String wTxt= endColName + "<='" + DateUtils.dateToString_full(DateUtils.todayNightZeroTime(endDate)) + "'";
            AppendWhere where = new AppendWhere(Type_Direct);
            where.setWhereTxt(wTxt);
            appendWhereList.add(where);
        }
    }

    public static void addEqualsToWhereMap(Object value,String valueObjName, Map<String,Object> whereMap)throws BusiException{
        if (value!=null) {
            if (value instanceof String && value.equals(""))
                return;
            whereMap.put(valueObjName,value);
        }
    }
    /**
     * 向条件队列中增加一条Like的文本条件
     * */
    public static void addLikeToWhereMap(String likeString,String colName, Map<String,Object> whereMap)throws BusiException{
        if (StringUtils.isNotEmpty(likeString)) {
            List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
            appendWhereList.add(new AppendWhereLike(colName, likeString));
        }
    }

    public static void addInToWhereMap(String colName,Map<String,Object> whereMap,Integer... ints)throws BusiException{
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        StringBuilder sb=new StringBuilder();
        sb.append(colName).append(" in (");
        for(int i=0;i<ints.length;i++){
            if (i>0)
                sb.append(",");
            sb.append(ints[i].toString());
        }
        sb.append(")");
        AppendWhere appendWhere=new AppendWhere(Type_Direct);
        appendWhere.setColName(colName);
        appendWhere.setWhereTxt(sb.toString());
        appendWhereList.add(appendWhere);
    }

    public static void addInToWhereMap(String colName,Map<String,Object> whereMap,String... strs)throws BusiException{
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        StringBuilder sb=new StringBuilder();
        sb.append(colName).append(" in ('");
        for(int i=0;i<strs.length;i++){
            if (i>0)
                sb.append("','");
            sb.append(strs[i]).append("'");
        }
        sb.append(")");
        AppendWhere appendWhere=new AppendWhere(Type_Direct);
        appendWhere.setColName(colName);
        appendWhere.setWhereTxt(sb.toString());
        appendWhereList.add(appendWhere);
    }

    public static void addInToWhereMap(String colName, Map<String,Object> whereMap, Set<Object> set)throws BusiException{
        if (set.size()<=0)
            return;
        Object[] os=set.toArray();
        Object o0=os[0];
        String f = "'";
        if (o0 instanceof Integer || o0 instanceof Long)
            f = "";
        List<AppendWhere> appendWhereList = initParamMapAppendWhereList(whereMap);
        StringBuilder sb=new StringBuilder();
        sb.append(colName).append(" in (").append(f);
        for(int i=0;i<set.size();i++){
            if (i>0)
                sb.append(f).append(",").append(f);
            sb.append(os[i].toString()).append(f);
        }
        sb.append(")");
        AppendWhere appendWhere=new AppendWhere(Type_Direct);
        appendWhere.setColName(colName);
        appendWhere.setWhereTxt(sb.toString());
        appendWhereList.add(appendWhere);
    }
}
