package com.shadow.codecoverage.databuffer.buffer;

import com.shadow.codecoverage.databuffer.common.AtomicRangeInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname BUffer
 * @Description TODO
 * @Date 2023/2/4 11:57
 * @Created by pepsi
 */
public class Buffer<T> {

    private final Object[] buffer;

    private Strategy strategy;

    private AtomicRangeInteger index;

    public Buffer(int bufferSize, Strategy strategy) {
        buffer = new Object[bufferSize];
        this.strategy = strategy;
        index = new AtomicRangeInteger(0, bufferSize);
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public boolean save(T data) {
        int i = index.getAndIncrement();
        if (buffer[i] != null) {
            return false;
        }
        buffer[i] = data;
        return true;
    }

    public List<T> obtain(int start, int end) {
        List<T> datas = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            if (buffer[i] != null) {
                datas.add((T) buffer[i]);
                buffer[i] = null;
            }
        }
        return datas;
    }

    public int getBufferSize() {
        return buffer.length;
    }
}
