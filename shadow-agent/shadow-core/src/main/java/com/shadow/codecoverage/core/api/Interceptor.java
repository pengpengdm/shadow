package com.shadow.codecoverage.core.api;

/**
 * @Classname Interceptor
 * @Description TODO
 * @Date 2023/1/14 13:08
 * @Created by pepsi
 */
public interface Interceptor {

    /**
     * @param watcher
     */
    void watch(EnhanceEventWatcher watcher);

    String enhanceClassName();

}
