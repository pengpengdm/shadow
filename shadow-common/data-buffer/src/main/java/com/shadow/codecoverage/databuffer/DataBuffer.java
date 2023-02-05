package com.shadow.codecoverage.databuffer;

import com.shadow.codecoverage.databuffer.buffer.Channels;
import com.shadow.codecoverage.databuffer.buffer.Strategy;
import com.shadow.codecoverage.databuffer.consumer.ConsumerPool;
import com.shadow.codecoverage.databuffer.consumer.IConsumer;
import com.shadow.codecoverage.databuffer.partition.RollingPartitioner;

/**
 * @Classname DataBuffer
 * @Description TODO
 * @Date 2023/2/4 11:56
 * @Created by pepsi
 */
public class DataBuffer<T> {

    private final int bufferSize;

    private final int channelSize;

    private Channels<T> channels;

    private ConsumerPool<T> consumerPool;

    public DataBuffer(int channelSize, int bufferSize) {
        this.bufferSize = bufferSize;
        this.channelSize = channelSize;
        channels = new Channels<T>(channelSize, bufferSize, new RollingPartitioner<T>(), Strategy.IF_POSSIBLE);
    }

    public boolean produce(T data) {
        if (consumerPool != null && !consumerPool.isRunning()) {
            return false;
        }
        return this.channels.save(data);
    }

    public DataBuffer consume(IConsumer<T> consumer, int num) {
        if (consumerPool != null) {
            consumerPool.close();
        }
        consumerPool = new ConsumerPool<>(this.channels, consumer, num);
        consumerPool.begin();
        return this;
    }

    public void shutdownConsumers() {
        if (consumerPool != null) {
            consumerPool.close();
        }
    }

}
