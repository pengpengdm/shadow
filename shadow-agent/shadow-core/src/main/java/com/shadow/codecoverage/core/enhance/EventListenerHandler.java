package com.shadow.codecoverage.core.enhance;


import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.api.event.BeforeEvent;
import com.shadow.codecoverage.core.api.event.Event;
import com.shadow.codecoverage.core.api.event.ReturnEvent;
import com.shadow.codecoverage.implant.Implant;
import com.shadow.codecoverage.implant.ImplantHandler;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname EventListenerHandler
 * @Description TODO
 * @Date 2023/1/15 22:10
 * @Created by pepsi
 */
public class EventListenerHandler implements ImplantHandler {

    /**
     * 直接静态初始化拉倒
     */
    private static EventListenerHandler singleton = new EventListenerHandler();
    private final Map<Integer, EventListener> eventListenerMap = new ConcurrentHashMap<>();

    private EventListenerHandler() {

    }

    public static EventListenerHandler getSingleton() {
        return singleton;
    }


    public void register(final int listenerId, final EventListener eventListener) {
        eventListenerMap.put(listenerId, eventListener);
    }

    @Override
    public void handleOnPluginMethodBefore(int listenerId, Object[] args, String javaClassName, String javaMethodName, String javaMethodDesc, Object target) throws Throwable {
        final BeforeEvent event = new BeforeEvent(javaClassName, javaMethodName, javaMethodDesc, target, args, -1);
        try {
            handleEvent(listenerId, event);
        } catch (Throwable throwable) {

        }
    }

    @Override
    public void handleOnPluginMethodReturn(int listenerId) throws Throwable {
        final Event event = new ReturnEvent(null);
        try {
            handleEvent(listenerId, event);
        } catch (Throwable throwable) {
            //
        }
    }

    @Override
    public void handleOnBefore(int listenerId, int methodId) throws Throwable {
        final Event event = new BeforeEvent(null, null, null, null, null, methodId);
        try {
            handleEvent(listenerId, event);
        } catch (Throwable throwable) {

        }
    }

    @Override
    public void recordMethodCoverLines(int listenerId, int methodId, BitSet coverLines) throws Throwable {
        final Event event = new ReturnEvent(null, methodId, coverLines);
        try {
            handleEvent(listenerId, event);
        } catch (Throwable throwable) {

        }
    }

    /**
     * 统一时间处理
     *
     * @param listenerId
     * @param event
     */
    private void handleEvent(int listenerId, Event event) {
        final EventListener listener = eventListenerMap.get(listenerId);
        try {
            listener.onEvent(event);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
