package com.shadow.codecoverage.core.api;


import com.shadow.codecoverage.core.api.event.Event;

/**
 * @Classname EventListener
 * @Description TODO
 * @Date 2023/1/14 14:00
 * @Created by pepsi
 */
public interface EventListener {

    void onEvent(Event event) throws Throwable;
}
