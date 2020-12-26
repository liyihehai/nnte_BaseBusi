package com.nnte.basebusi.entity;

import lombok.Data;

/**
 * 数据源配置类
 * */

@Data
public class DBSrcConfig {
    private String DBDriverClassName;   //数据连接驱动类名
    private String DBIp;                //数据连接IP
    private String DBPort;              //数据连接端口
    private String DBSchema;            //数据连接DB库名
    private String DBUser;              //数据连接用户名
    private String DBPassword;          //数据连接口令

    private int minimumIdle=0;
    private int maximumPoolSize=20;
    private int idleTimeout=1000*20;
    private String connectionTestQuery="SELECT 1";
}
