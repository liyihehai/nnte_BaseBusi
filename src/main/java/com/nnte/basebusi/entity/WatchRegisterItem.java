package com.nnte.basebusi.entity;

import com.nnte.basebusi.annotation.TriggerInterface;
import com.nnte.basebusi.annotation.WatchInterface;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class WatchRegisterItem implements TriggerInterface {
    private WatchInterface itemInterface;
    private String itemName;
    private String itemCron;
    private Integer runTimes;
    private Date lastRunTime;
    private Date nextRunTime;

    @Override
    public String getPersisFileName() {
        return null;
    }

    @Override
    public String getKey() {
        return itemName;
    }

    @Override
    public Long getExpireTime() {
        return nextRunTime.getTime();
    }
}
