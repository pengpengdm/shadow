package com.shadow.codecoverage.core.context;

import com.shadow.codecoverage.core.context.trace.AbstractSpan;
import com.shadow.codecoverage.core.context.trace.EntrySpan;
import com.shadow.codecoverage.core.context.trace.LocalSpan;
import com.shadow.codecoverage.core.context.trace.TraceSegment;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * @Classname TraceContext
 * @Description TODO
 * @Date 2023/2/3 23:14
 * @Created by pepsi
 */
public class TraceContext {

    private Logger logger = LoggerFactory.getLogger(TraceContext.class);

    private TraceSegment segment;

    private LinkedList<AbstractSpan> activeSpanStack = new LinkedList<>();

    private int spanIdGenerator;

    private volatile boolean running;

    TraceContext() {
        this.segment = new TraceSegment();
        this.spanIdGenerator = 0;
        running = true;
    }

    public AbstractSpan createEntrySpan() {
        final AbstractSpan parentSpan = peekEntrySpan();
        final int parentSpanId = parentSpan == null ? -1 : parentSpan.getSpanId();
        EntrySpan entrySpan = new EntrySpan(spanIdGenerator++, parentSpanId);
        if (parentSpan != null && parentSpan.isEntry()) {
            EntrySpan earlyEntrySpan = (EntrySpan) parentSpan;
            if (StringUtils.isNotBlank(earlyEntrySpan.getCaseInf())) {
                entrySpan.setCaseInf(earlyEntrySpan.getCaseInf());
            }
        }
        entrySpan.start();
        return push(entrySpan);
    }


    public AbstractSpan createLocalSpan() {
        AbstractSpan parentSpan = peek();
        if (parentSpan == null) {
            return null;
        }
        final int parentSpanId = parentSpan.getSpanId();
        AbstractSpan span = new LocalSpan(spanIdGenerator++, parentSpanId);
        span.start();
        return push(span);
    }

    public AbstractSpan activeSpan() {
        AbstractSpan span = peek();
        if (span != null) {
            return span;
        }
        return null;
    }

    public boolean stopSpan(AbstractSpan span) {
        try {
            AbstractSpan lastSpan = peek();
            if (lastSpan != null && lastSpan == span) {
                lastSpan.finish(segment);
                pop();
            } else {
                //todo
            }
            finish();
        } catch (Exception e) {
            //ignore
        }
        return activeSpanStack.isEmpty();
    }

    private void finish() {
        try {
            if (activeSpanStack.isEmpty() && running) {
                TraceSegmentManager.afterFinished(segment);
                running = false;
            }
        } catch (Exception e) {

        }
    }

    private AbstractSpan pop() {
        return activeSpanStack.removeLast();
    }

    private AbstractSpan push(AbstractSpan span) {
        activeSpanStack.addLast(span);
        return span;
    }

    private AbstractSpan peek() {
        if (activeSpanStack.isEmpty()) {
            return null;
        }
        return activeSpanStack.getLast();
    }

    private AbstractSpan peekEntrySpan() {
        if (activeSpanStack.isEmpty()) {
            return null;
        }
        for (AbstractSpan span : activeSpanStack) {
            if (span.isEntry()) {
                return span;
            }
        }
        return activeSpanStack.getLast();
    }

    public String getTraceId() {
        return this.segment.getTraceId();
    }
}
