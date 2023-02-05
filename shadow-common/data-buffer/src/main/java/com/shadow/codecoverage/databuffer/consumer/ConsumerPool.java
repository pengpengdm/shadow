package com.shadow.codecoverage.databuffer.consumer;

import com.shadow.codecoverage.databuffer.buffer.Channels;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Classname ConsumerPool
 * @Description TODO
 * @Date 2023/2/4 18:18
 * @Created by pepsi
 */
public class ConsumerPool<T> {

    private boolean running;

    private ConsumerThread[] consumerThreads;

    private Channels<T> channels;

    private ReentrantLock lock;

    public ConsumerPool(Channels<T> channels, IConsumer<T> consumer, int num) {
        this(channels, num);
        consumer.init();
        for (int i = 0; i < num; i++) {
            consumerThreads[i] = new ConsumerThread<>("DEFAULT-THREAD", consumer);
        }
    }

    public ConsumerPool(Channels<T> channels, int num) {
        running = false;
        this.channels = channels;
        consumerThreads = new ConsumerThread[num];
        lock = new ReentrantLock();
    }

    public void begin() {
        if (running) {
            return;
        }
        try {
            lock.lock();
            this.allocateBuffer2Thread();
            for (ConsumerThread thread : consumerThreads) {
                thread.start();
            }
            running = true;
        } finally {
            lock.unlock();
        }
    }

    private void allocateBuffer2Thread() {
        int channelSize = this.channels.getChannelSize();
        for (int channelIndex = 0; channelIndex < channelSize; channelIndex++) {
            int consumerIndex = channelIndex % consumerThreads.length;
            consumerThreads[consumerIndex].addSource(channels.getBuffer(channelIndex));
        }
    }

    public void close() {
        try {
            lock.lock();
            this.running = false;
            for (ConsumerThread thread : consumerThreads) {
                thread.shutdown();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
