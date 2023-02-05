package com.shadow.codecoverage.core.context;

import com.shadow.codecoverage.core.context.trace.AbstractSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Classname ContextManager
 * @Description TODO
 * @Date 2023/2/3 23:12
 * @Created by pepsi
 */
public class ContextManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class);

    private static ThreadLocal<TraceContext> CONTEXT = new ThreadLocal<>();

    /**
     * @return
     */
    protected static TraceContext getOrCreate() {
        TraceContext context = CONTEXT.get();
        if (context == null) {
            context = new TraceContext();
            CONTEXT.set(context);
        }
        return context;
    }

    public static AbstractSpan createEntrySpan() {
        TraceContext context = getOrCreate();
        return context.createEntrySpan();
    }

    public static AbstractSpan createLocalSpan() {
        TraceContext context = getOrCreate();
        return context.createLocalSpan();
    }

    public static AbstractSpan getActiveSpan() {
        TraceContext context = CONTEXT.get();
        if (context == null) {
            return null;
        }
        return context.activeSpan();
    }

    public static void stopSpan() {
        final TraceContext context = CONTEXT.get();
        stopSpan(context.activeSpan(), context);
    }

    private static void stopSpan(AbstractSpan span, final TraceContext context) {
        if (context.stopSpan(span)) {
            CONTEXT.remove();
        }
    }

    public static String getTraceId() {
        TraceContext context = CONTEXT.get();
        if (context != null) {
            return context.getTraceId();
        }
        return null;
    }

}
