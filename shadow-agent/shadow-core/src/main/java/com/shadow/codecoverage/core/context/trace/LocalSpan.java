package com.shadow.codecoverage.core.context.trace;

/**
 * @Classname LocalSpan
 * @Description TODO
 * @Date 2023/2/3 23:16
 * @Created by pepsi
 */
public class LocalSpan extends AbstractSpan {

    public LocalSpan(int spanId, int parentSpanId) {
        super(spanId, parentSpanId);
    }

    @Override
    public boolean isEntry() {
        return false;
    }
}
