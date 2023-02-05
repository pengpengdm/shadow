package com.shadow.codecoverage.databuffer.partition;

/**
 * @Classname RollingPartitioner
 * @Description TODO
 * @Date 2023/2/4 12:12
 * @Created by pepsi
 */
public class RollingPartitioner<T> implements IDataPartitioner<T> {

    private volatile int i = 0;

    @Override
    public int partition(int total, T data) {
        return Math.abs(i++ % total);
    }

    @Override
    public int maxRetryCount() {
        return 3;
    }
}
