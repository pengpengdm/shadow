package com.shadow.codecoverage.core.api.event;

/**
 * @Classname Event
 * @Description TODO
 * @Date 2023/1/14 14:01
 * @Created by pepsi
 */
public abstract class Event {

    public final Type eventType;

    public Event(Type type) {
        this.eventType = type;
    }

    public enum Type {

        BEFORE,

        THROWS,

        RETURN;


    }

}
