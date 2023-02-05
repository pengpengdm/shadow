package com.shadow.codecoverage.databuffer.buffer;

import com.shadow.codecoverage.databuffer.partition.IDataPartitioner;

/**
 * @Classname Channels
 * @Description TODO
 * @Date 2023/2/4 12:06
 * @Created by pepsi
 */
public class Channels<T> {

    private final Buffer<T>[] bufferChannels;

    private IDataPartitioner<T> dataPartitioner;


    public Channels(int channelSize, int bufferSize, IDataPartitioner<T> dataPartitioner, Strategy strategy) {
        this.dataPartitioner = dataPartitioner;
        bufferChannels = new Buffer[channelSize];
        for (int i = 0; i < channelSize; i++) {
            bufferChannels[i] = new Buffer<>(bufferSize, strategy);
        }
    }

    public boolean save(T data) {
        int index = dataPartitioner.partition(bufferChannels.length, data);
        return bufferChannels[index].save(data);
    }

    public int getChannelSize() {
        return this.bufferChannels.length;
    }

    public IDataPartitioner<T> getDataPartitioner() {
        return dataPartitioner;
    }

    public void setDataPartitioner(IDataPartitioner<T> dataPartitioner) {
        this.dataPartitioner = dataPartitioner;
    }

    public void setStrategy(Strategy strategy) {
        for (Buffer<T> buffer : bufferChannels) {
            buffer.setStrategy(strategy);
        }
    }

    public Buffer getBuffer(int index) {
        return bufferChannels[index];
    }
}
