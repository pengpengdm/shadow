package com.shadow.codecoverage.core.api.event;

/**
 * @Classname ThrowsEvent
 * @Description TODO
 * @Date 2023/1/14 14:02
 * @Created by pepsi
 */
public class ThrowsEvent extends Event {

    public final Throwable throwable;

    public ThrowsEvent(final Throwable throwable) {
        super(Type.THROWS);
        this.throwable = throwable;
    }
}
