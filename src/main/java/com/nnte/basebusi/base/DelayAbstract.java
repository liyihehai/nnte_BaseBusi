package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.TriggerInterface;
import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.utils.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * nnte延迟
 * */
public abstract class DelayAbstract<T extends TriggerInterface> extends BaseBusi implements Runnable{
    private static Logger log = LoggerFactory.getLogger(DelayAbstract.class);
    private final List<T> TriggerList = new ArrayList<>();
    private volatile int listenState = 0;
    private volatile Object waitObject = new Object();
    private ExecutorService popProcessPool; //处理时间到期的线程池
    private PersistConfig persistConfig = new PersistConfig();
    /**
     * 目标执行入口函数，子类需要实现
     * */
    public abstract void onRunTriggerMethod(T trigger) throws Exception;
    /**
     * 执行结束后回调，用于返回执行情况的信息
     * */
    public abstract void onTriggerMethodFinished(T trigger,int type,Exception exeExp,Long startTime,Long endTime) throws Exception;
    /**
     * 持续化加载Trigger前返回是否需要加载
     * */
    public abstract boolean onPriLoadTrigger(T trigger) throws Exception;

    public void shutdownPopPrecessPoll(){
        if (popProcessPool!=null)
            popProcessPool.shutdown();
    }

    public boolean isPopPrecessPollShutDown(){
        return popProcessPool.isShutdown();
    }

    public boolean isPersist() {
        return persistConfig.isPersist();
    }

    public String getPersistRoot() {
        return persistConfig.getPersistRoot();
    }

    public String getInstanceName() {
        return persistConfig.getInstanceName();
    }

    public int getWaitCount(){
        return TriggerList.size();
    }

    public enum OpeType{
        Push,Remove,Pop,ReadFirst
    }

    @Data
    public static class PersistConfig{
        private boolean isPersist = false;      //是否需要本地持续
        private String persistRoot = "";        //如果需要本地持续，需要指定根目录
        private String instanceName = "";       //实例名，用于本地持续时构建保存路径
        private String persistPath = null;
    }

    /**
     * 删除一个Trigger的持续化记录
     * */
    public void removePersistRecord(T trigger){
        if (persistConfig!=null && persistConfig.isPersist()) {
            String pathFileName = StringUtils.pathAppend(persistConfig.getPersistPath(),
                    trigger.getPersisFileName());
            try {
                boolean isSuc=FileUtil.delFile(pathFileName);
                outLogDebug("removePersistRecord delFile=" + pathFileName+"..."+((isSuc)?"true":"false"));
            } catch (Exception e) {
                outLogExp(e);
            }
        }
    }

    static class PopProcess<T extends TriggerInterface> extends Thread{

        private T trigger;
        private DelayAbstract delayInstance;
        private String LoggerName;

        public PopProcess(T trigger,DelayAbstract instance){
            this.trigger = trigger;
            this.delayInstance = instance;
        }

        //触发任务: type = 触发任务方式:1自动触发，2手动触发
        private void runInstanceTrigger(T trigger,Integer type){
            Long startTime = (new Date()).getTime();
            Exception exeExp = null;
            try{
                log.debug("task "+trigger.getKey()+" is running");
                delayInstance.onRunTriggerMethod(trigger);
            }catch (Exception e){
                log.error(e.getMessage(),e);
                exeExp = e;
            }finally {
                try {
                    delayInstance.removePersistRecord(trigger);
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
                Long endTime = (new Date()).getTime();
                try{
                    delayInstance.onTriggerMethodFinished(trigger,type,exeExp,startTime,endTime);
                }catch (Exception e){
                    log.error(e.getMessage(),e);;
                }
            }
        }

        @Override
        public void run() {
            //时间到了，需要触发任务执行
            runInstanceTrigger(trigger,1);
        }
    }

    // Addition of doPrivileged added due to RT-19580
    private final ThreadGroup THREAD_GROUP = AccessController.doPrivileged((PrivilegedAction<ThreadGroup>) () -> new ThreadGroup("DelayAbstract thread pool"));
    private final Thread.UncaughtExceptionHandler UNCAUGHT_HANDLER = (thread, throwable) -> {
        // Ignore IllegalMonitorStateException, these are thrown from the ThreadPoolExecutor
        // when a browser navigates away from a page hosting an applet that uses
        // asynchronous tasks. These exceptions generally do not cause loss of functionality.
        if (!(throwable instanceof IllegalMonitorStateException)) {
            outLogWarn("Uncaught throwable in " + THREAD_GROUP.getName());
        }
    };

    // Addition of doPrivileged added due to RT-19580
    private final ThreadFactory THREAD_FACTORY = run -> AccessController.doPrivileged((PrivilegedAction<Thread>) () -> {
        final Thread th = new Thread(THREAD_GROUP, run);
        th.setUncaughtExceptionHandler(UNCAUGHT_HANDLER);
        th.setPriority(Thread.MIN_PRIORITY);
        th.setDaemon(true);
        return th;
    });

    public void initDelayComponent(int processCount,int queueSize,PersistConfig config,Class<T> triggerClass) throws Exception{
        int pc = processCount;
        if (processCount<=0)
            pc = 10;
        int qs = queueSize;
        if (queueSize<=0)
            qs = 50;
        BlockingQueue<Runnable> blockingQueue = null;
        if (queueSize>0)
            blockingQueue = new ArrayBlockingQueue<>(queueSize);
        popProcessPool = new ThreadPoolExecutor(pc, qs, 1, TimeUnit.SECONDS, blockingQueue,
                THREAD_FACTORY,new ThreadPoolExecutor.AbortPolicy());
        if (config!=null){
            persistConfig = config;
            if (persistConfig.isPersist()){
                if (StringUtils.isEmpty(persistConfig.getPersistRoot()) ||
                        StringUtils.isEmpty(persistConfig.getInstanceName()))
                    throw new BusiException("持续路径或实例名没有指定");
                String jarPath= FileUtil.toUNIXpath(System.getProperty("user.dir"));
                String persisRoot=StringUtils.pathAppend(jarPath,persistConfig.getPersistRoot());
                String path = StringUtils.pathAppend(persisRoot,persistConfig.getInstanceName());
                path = StringUtils.pathAppend(path,"delayTriggers");
                if (!FileUtil.isPathExists(path)){
                    if (!FileUtil.makePath(path))
                        throw new BusiException("不能建立持续路径");
                }
                persistConfig.setPersistPath(path);
                File file = new File(path);
                if (!file.exists()) {
                    return;
                }
                if (!file.isDirectory()) {
                    return;
                }
                String[] tempList = file.list();
                for (int i = 0; i < tempList.length; i++) {
                    String filePathName=StringUtils.pathAppend(path,tempList[i]);
                    if (FileUtil.isFileExist(filePathName)) {
                        String json = FileUtil.readFile(filePathName);
                        if (StringUtils.isNotEmpty(json)) {
                            T trigger = JsonUtil.jsonToBean(json, triggerClass);
                            if (onPriLoadTrigger(trigger))
                                operatorList(OpeType.Push, null, trigger);
                        }
                    }
                }
            }
        }
        startListenThread();//启动线程
    }

    private void pushTrigger(T trigger){
        boolean isPushed = false;
        long expTime = trigger.getExpireTime();
        Integer index = -1;
        for(int i=TriggerList.size()-1;i>=0;i--){
            T f=TriggerList.get(i);
            if (f.getExpireTime()<=expTime){
                index = i+1;
                TriggerList.add(index,trigger);
                isPushed=true;
                break;
            }
        }
        if (!isPushed) {//如果之前没有插入，则要插入到第一个
            TriggerList.add(0, trigger);
            index = 0;
        }
        outLogDebug("pushTrigger["+index+"]:"+trigger.getKey());
        //如果需要持续化，需要将信息记录在文件中
        //listenState=0时表示当前正在初始化，不需要记录持续文件
        if (listenState==1 && persistConfig.isPersist()){
            String fileContent = JsonUtil.beanToJson(trigger);
            String pathFileName = StringUtils.pathAppend(persistConfig.getPersistPath(),trigger.getPersisFileName());
            try {
                outLogDebug("record trigger persist:"+pathFileName);
                FileUtil.createFile(pathFileName, fileContent);
            }catch (Exception e){
                outLogExp(e);
            }
        }
        if (index==0){
            if (listenState==1){ //如果插入第一个，需要唤醒线程
                synchronized (waitObject){
                    waitObject.notify();//如果线程在等待，需要唤醒线程
                }
            }
        }
    }

    private void removeTrigger(String triggerKey){
        for(Integer i=0;i<TriggerList.size();i++){
            T f=TriggerList.get(i);
            if (f.getKey().equals(triggerKey)){
                T removeTrigger=TriggerList.remove(i.intValue());
                if (removeTrigger!=null)
                    outLogDebug("removeTrigger["+i+"]:"+f.getKey()+"...success");
                else
                    outLogWarn("removeTrigger["+i+"]:"+f.getKey()+"...failed");
                removePersistRecord(removeTrigger);
                if (i==0){
                    //如果删除了最后一个
                    synchronized (waitObject){
                        waitObject.notify();//通知检测线程不再等待
                    }
                }
                break;
            }
        }
    }

    private T popTrigger(){
        if (TriggerList.size()<=0)
            return null;
        T f=TriggerList.get(0);
        TriggerList.remove(0);
        return f;
    }

    private T readFirstTrigger(){
        if (TriggerList.size()<=0)
            return null;
        T f=TriggerList.get(0);
        return f;
    }

    /**
     * 操作队列的最外层函数，保证所有操作都需要排队
     * */
    public synchronized T operatorList(OpeType opeType,String triggerKey,T trigger){
        switch (opeType){
            case Pop:{return popTrigger();}
            case Push:{pushTrigger(trigger);break;}
            case Remove:{removeTrigger(triggerKey);break;}
            case ReadFirst:{return readFirstTrigger();}
        }
        return null;
    }

    @Override
    public void run() {
        listenState = 1;
        outLogWarn("delay monitor thread："+ThreadUtil.getThreadCode()+"...start!");
        T firstTrigger;
        try {
            while (true) {
                if ((firstTrigger = operatorList(OpeType.ReadFirst, null, null)) == null) {
                    ThreadUtil.Sleep(200);
                    if (isPopPrecessPollShutDown())
                        break;
                    else
                        continue;
                }
                long nowTime = (new Date()).getTime();
                long expTime = firstTrigger.getExpireTime();
                if (expTime<=nowTime){
                    //如果时间到了，应该执行pop
                    firstTrigger=operatorList(OpeType.Pop,null,null);
                    if (firstTrigger!=null){
                        this.onTriggerTimeExpired(firstTrigger);
                    }
                }else{
                    //如果时间没有到，应该等待直到超时
                    synchronized(waitObject) {
                        waitObject.wait(expTime-nowTime);
                    }
                }
            }
        }catch (Exception e){
            outLogExp(e);
        }finally {
            listenState=0;
            outLogWarn("delay monitor thread："+ThreadUtil.getThreadCode()+"...exit!");
        }
    }

    private void startListenThread(){
        if (listenState==0)
            new Thread(this).start();
    }

    //处理Trigger需要启动线程池
    private void onTriggerTimeExpired(T trigger){
        popProcessPool.execute(new PopProcess(trigger,this));
    }
}
