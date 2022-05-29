package com.nnte.basebusi.base;

import com.nnte.basebusi.entity.WatchRegisterItem;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.utils.CronExpression;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.TreeMap;

@Component
public class LocalTaskComponent extends DelayAbstract<WatchRegisterItem>{

    private TreeMap<String, WatchRegisterItem> watchRegMap = new TreeMap<>();

    /**
     * 注册一个需要执行守护的组件
     * */
    public void registerWatchItem(WatchRegisterItem item) throws Exception {
        if (watchRegMap.get(item.getItemName()) != null)
            throw new BusiException("守护组件名已存在");
        if (!CronExpression.isValidExpression(item.getItemCron()))
            throw new BusiException("守护组件时间调度表达式错误");
        if (item.getRunTimes().equals(0))
            throw new BusiException("守护组件可执行次数不能为0");
        Date nextValidTime = new CronExpression(item.getItemCron()).getNextValidTimeAfter(new Date());
        item.setNextRunTime(nextValidTime);
        watchRegMap.put(item.getItemName(), item);
        operatorList(OpeType.Push, null, item);
    }
    /**
     * 注销一个组件
     * */
    public void unRegisterWatchItem(String itemName){
        WatchRegisterItem item=watchRegMap.get(itemName);
        if (item != null){
            outLogInfo("守护组件注销："+item.getItemName());
            operatorList(OpeType.Remove,item.getKey(),null);
            watchRegMap.remove(itemName);
        }
    }
    /**
     * 启动自动任务(本应用的自动任务都不在本地持续化)
     * */
    public void startWatchMonitor(Integer processCount,Integer queueSize) throws Exception {
        initDelayComponent(processCount,queueSize, null, WatchRegisterItem.class);
    }

    @Override
    public void onRunTriggerMethod(WatchRegisterItem trigger) throws Exception {
        if (trigger!=null && trigger.getItemInterface()!=null){
            trigger.getItemInterface().runWatch();
        }
    }

    @Override
    public void onTriggerMethodFinished(WatchRegisterItem trigger, int type, Exception exeExp, Long startTime, Long endTime) throws Exception {
        if (trigger.getRunTimes().equals(1)||trigger.getRunTimes().equals(0)){
            unRegisterWatchItem(trigger.getKey());
            return;
        }else{
            if (trigger.getRunTimes()>1){
                trigger.setRunTimes(trigger.getRunTimes()-1);
            }
        }
        trigger.setLastRunTime(new Date(startTime));
        Date nextValidTime = new CronExpression(trigger.getItemCron()).getNextValidTimeAfter(new Date(startTime));
        trigger.setNextRunTime(nextValidTime);
        operatorList(OpeType.Push, null, trigger);
    }

    @Override
    public boolean onPriLoadTrigger(WatchRegisterItem trigger) throws Exception {
        return false;
    }
}
