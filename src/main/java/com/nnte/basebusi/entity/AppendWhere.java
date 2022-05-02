package com.nnte.basebusi.entity;

import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.utils.DateUtils;
import com.nnte.framework.utils.StringUtils;

import java.util.Date;
import java.util.List;

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
}
