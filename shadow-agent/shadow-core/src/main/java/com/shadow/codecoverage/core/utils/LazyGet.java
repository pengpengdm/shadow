package com.shadow.codecoverage.core.utils;

/**
 * @Classname LazyGet
 * @Description TODO
 * @Date 2023/1/15 09:59
 * @Created by pepsi
 */
public abstract class LazyGet<T> {

    private volatile boolean isInit = false;
    private volatile T object;

    abstract protected T initialValue() throws Throwable;

    public T get() {

        if (isInit) {
            return object;
        }

        // lazy get
        try {
            object = initialValue();
            isInit = true;
            return object;
        } catch (Throwable throwable) {
            throw new LazyGetUnCaughtException(throwable);
        }

    }

    private static class LazyGetUnCaughtException extends RuntimeException {
        LazyGetUnCaughtException(Throwable cause) {
            super(cause);
        }
    }
}
