package com.shadow.codecoverage.databuffer.partition;

/**
 * @Classname IDataPartitioner
 * @Description TODO
 * @Date 2023/2/4 12:12
 * @Created by pepsi
 */
public interface IDataPartitioner<T> {

    int partition(int total, T data);

    int maxRetryCount();

}
