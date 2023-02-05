package com.shadow.codecoverage.databuffer.consumer;

import java.util.List;

/**
 * @Classname DefaultConsumer
 * @Description TODO
 * @Date 2023/2/4 18:18
 * @Created by pepsi
 */
public abstract class DefaultConsumer<T> implements IConsumer<T> {
    @Override
    public void init() {
        //todo
    }

    @Override
    public void onError(List<T> data, Throwable throwable) {

    }

    @Override
    public void onExit() {
        //todo
    }
}
