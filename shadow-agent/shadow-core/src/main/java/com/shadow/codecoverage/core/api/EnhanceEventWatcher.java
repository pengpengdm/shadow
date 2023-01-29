package com.shadow.codecoverage.core.api;


import com.shadow.codecoverage.core.utils.matcher.builder.EventWatchCondition;

/**
 * @Classname EnhanceEventWatcher
 * @Description TODO
 * @Date 2023/1/14 13:57
 * @Created by pepsi
 */
public interface EnhanceEventWatcher {

    /**
     * 插件行为拦截
     *
     * @param condition
     * @param listener
     */
    void watchPluginBehavior(EventWatchCondition condition, EventListener listener);

    /**
     * 业务代码 行增强
     *
     * @param listener
     */
    void watchBizBehavior(EventListener listener);
}
