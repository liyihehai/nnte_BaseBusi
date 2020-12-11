package com.nnte.basebusi.annotation;
/**
 * 守护接口，实现runWatch并将组件在守护组件注册，守护
 * 组件将定期调用runWatch
 * */
public interface WatchInterface {
    void runWatch();
}
