package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.basebusi.annotation.WatchInterface;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.utils.LogUtil;
import com.nnte.framework.utils.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Component
@BusiLogAttr("WatchComponent")
public class WatchComponent extends BaseComponent implements Runnable{

    @Getter
    private int runState = 0;   //运行时状态：0未运行，1：运行时，2：停顿时

    private boolean isContinue = true;              //是否继续执行
    private int sleepSeconds = 1000 * 60 * 3;//3分钟执行一次
    private TreeMap<Integer, WatchRegisterItem> watchRegMap = new TreeMap<>();

    public void setSleepSeconds(int sleepSeconds) {this.sleepSeconds = sleepSeconds;}
    public void setIsContinue(boolean isContinue) {this.isContinue = isContinue;}

    @AllArgsConstructor
    @Getter
    private class WatchRegisterItem {
        private WatchInterface itemInterface;
        private int itemIndex;
        private String itemName;
        @Setter
        private int execTimes=0;
    }

    /**
     * 注册一个需要执行守护的组件
     * */
    public void registerWatchItem(WatchInterface inter, int index,int execTimes) throws BusiException {
        if (watchRegMap.get(index) != null)
            throw new BusiException(1, "项目组件序号已存在", LogUtil.LogLevel.warn);
        WatchRegisterItem item = new WatchRegisterItem(inter,index,inter.getClass().getName(),execTimes);
        watchRegMap.put(index, item);
    }
    /**
     * 注销一个组件
     * */
    public void unRegisterWatchItem(int index){
        WatchRegisterItem item=watchRegMap.get(index);
        if (item != null){
            outLogInfo("守护组件注销："+item.getItemName());
            watchRegMap.remove(index);
        }
    }

    /**
     * 启动守护线程
     * */
    public void startWatch(){
        if (runState==0)
            new Thread(this).start();
    }

    @Override
    public void run() {
        BaseLog.logInfo("---- 平台守护线程启动  ----");
        while (isContinue) {
            runState = 1;
            watchProcess();
            runState = 2;
            if (isContinue)
                ThreadUtil.Sleep(sleepSeconds);
            else
                break;
        }
        runState = 0;
        BaseLog.logInfo("---- 平台守护线程结束  ----");
    }

    /**
     * 平台守护组件的主函数
     */
    public void watchProcess() {
        if (watchRegMap!=null && watchRegMap.size()>0){
            List<WatchRegisterItem> list = new ArrayList<>(watchRegMap.values());
            for (WatchRegisterItem item : list) {
                if (item!=null && item.itemInterface!=null && item.getExecTimes()!=0) {
                    try {
                        item.itemInterface.runWatch();
                    }catch (Exception e){
                        BusiException be=new BusiException(e,3999, LogUtil.LogLevel.error);
                        BaseLog.outLogExp(be);
                    }
                    int exect=item.getExecTimes();
                    if (exect>0){
                        exect--;
                        item.setExecTimes(exect);
                        if (exect==0)
                            unRegisterWatchItem(item.getItemIndex());
                    }
                }
            }
        }
    }
}
