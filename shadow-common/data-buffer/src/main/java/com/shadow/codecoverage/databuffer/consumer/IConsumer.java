package com.shadow.codecoverage.databuffer.consumer;

import java.util.List;

/**
 * @Classname Iconsumer
 * @Description TODO
 * @Date 2023/2/4 18:18
 * @Created by pepsi
 */
public interface IConsumer<T> {

    void init();

    void consumer(List<T> data);

    void onError(List<T> data, Throwable throwable);

    void onExit();


}
