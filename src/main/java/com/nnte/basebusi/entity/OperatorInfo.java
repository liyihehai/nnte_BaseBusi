package com.nnte.basebusi.entity;

import lombok.Data;

@Data
public class OperatorInfo {
    private String operatorCode;
    private String operatorName;
    private String groupCode;
    private int operatorState=0;    //操作员状态：1表示可用
    private int operatorType=0;     //操作员类型：1表示超级管理员，可执行所有操作
    private String token;
    private String expireTime;      //YYYY-MM-DD hh:mm:ss
    private String loginTime;       //YYYY-MM-DD hh:mm:ss
    public boolean isSupperOpe(){
        return operatorType==1;
    }
}
