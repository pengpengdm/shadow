package com.shadow.codecoverage.core.context.trace;

import com.shadow.codecoverage.core.context.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @Classname TraceSegment
 * @Description TODO
 * @Date 2023/2/3 23:16
 * @Created by pepsi
 */
public class TraceSegment {

    private Logger logger = LoggerFactory.getLogger(TraceSegment.class);

    private String traceId;

    private Map<Integer, AbstractSpan> spans;

    private long beginTime;

    private long endTime;

    public TraceSegment() {
        this.traceId = IdGenerator.traceIdGenerator();
    }

    /***
     * 方法归档，覆盖行过滤
     * @param finishSpan
     */
    public void archive(AbstractSpan finishSpan) {
        AbstractSpan span = spans.get(finishSpan.getMethodId());
        if (span == null) {
            spans.put(finishSpan.getMethodId(), finishSpan);
        } else {
            Set<Integer> exsistCoverLines = span.getCoverLines();
            Set<Integer> coverLines = finishSpan.getCoverLines();
            if (exsistCoverLines == null || coverLines == null) {
                //
                return;
            }
            exsistCoverLines.addAll(coverLines);
        }
    }


    public String getTraceId() {
        return null;
    }
}
