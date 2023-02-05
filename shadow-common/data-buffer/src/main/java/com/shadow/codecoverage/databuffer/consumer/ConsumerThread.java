package com.shadow.codecoverage.databuffer.consumer;

import com.shadow.codecoverage.databuffer.buffer.Buffer;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname ConsumerThread
 * @Description TODO
 * @Date 2023/2/4 18:18
 * @Created by pepsi
 */
public class ConsumerThread<T> extends Thread {

    private volatile boolean running;

    private IConsumer<T> consumer;

    private List<DataSource> dataSources;

    public ConsumerThread(String name, IConsumer<T> consumer) {
        super(name);
        this.consumer = consumer;
        running = false;
        dataSources = new LinkedList<>();
    }


    void addSource(Buffer<T> buffer) {
        this.dataSources.add(new DataSource(buffer, 0, buffer.getBufferSize()));
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            boolean hasData = consume();
            if (!hasData) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        consume();
        consumer.onExit();
    }

    private boolean consume() {
        boolean hasData = false;
        for (DataSource dataSource : dataSources) {
            List<T> data = dataSource.obtain();
            if (data.size() == 0) {
                continue;
            }
            hasData = true;
            try {
                consumer.consumer(data);
            } catch (Throwable throwable) {
                consumer.onError(data, throwable);
            }
        }
        return hasData;
    }

    public void shutdown() {
        running = false;
    }

    class DataSource {
        private Buffer<T> buffer;

        private int start;

        private int end;

        public DataSource(Buffer<T> buffer, int start, int end) {
            this.buffer = buffer;
            this.start = start;
            this.end = end;
        }

        List<T> obtain() {
            return buffer.obtain(start, end);
        }
    }
}
