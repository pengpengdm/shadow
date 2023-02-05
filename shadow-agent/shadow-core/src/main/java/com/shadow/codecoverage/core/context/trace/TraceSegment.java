package com.shadow.codecoverage.core.context.trace;

import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.config.GlobalMetaContext;
import com.shadow.codecoverage.core.context.util.IdGenerator;
import com.shadow.codecoverage.core.utils.AgentUtils;
import com.shadow.codecoverage.protoc.report.SpanCoverData;
import com.shadow.codecoverage.protoc.report.TraceCoverData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
        return traceId;
    }


    public TraceCoverData transform() {
        if(spans == null || spans.size() <=1){
            return null;
        }
        TraceCoverData.Builder traceVal = TraceCoverData.newBuilder();
        traceVal.setTraceId(traceId);
        traceVal.setMachineInf(AgentConfig.MACHINE_INF);
        traceVal.setAppId(AgentConfig.APP_ID);
        traceVal.setStartTime(beginTime);
        traceVal.setEndTime(endTime);
        for(Map.Entry<Integer,AbstractSpan> entry: spans.entrySet()){
            AbstractSpan span = entry.getValue();
            if(span.isEntry()){
                EntrySpan entrySpan = (EntrySpan) span;
                if(StringUtils.isNotBlank(entrySpan.getCaseInf())){
                    traceVal.setAtmReqFlag(entrySpan.getCaseInf());
                    traceVal.setUri(entrySpan.getUri());
                    break;
                }
            }
        }
        boolean isAtmRequest = StringUtils.isNotBlank(traceVal.getAtmReqFlag());
        List<SpanCoverData> spanList = compressCoverLine(isAtmRequest);
        traceVal.addAllSpanVals(spanList);
        return traceVal.build();
    }


    private List<SpanCoverData> compressCoverLine(boolean isAtmRequest) {
        List<SpanCoverData> mergeCoverDatas = new ArrayList<>();
        for (Map.Entry<Integer,AbstractSpan> entry:spans.entrySet()){
            AbstractSpan span = entry.getValue();
            if(span.isEntry() || span.getCoverLines() == null){
                continue;
            }
            if(!isAtmRequest){
                GlobalMetaContext.filterCoveredLines(span.getCoverLines(),span.getMethodId());
            }
            if(span.getCoverLines().isEmpty()){
                continue;
            }
            mergeCoverDatas.add(SpanCoverData.newBuilder()
                    .setClassName(span.getClassName())
                    .setMethodName(span.getMethodName())
                    .setMethodKey(AgentUtils.hashForMethodKey(span.getClassMethodSing()))
                    .setSpanId(span.getMethodId())
                    .setParentId(span.getParentSpanId())
                    .addAllCoverLines(AgentUtils.coverNumMerge(span.getCoverLines()))
                    .build());
        }
        return mergeCoverDatas;


    }
}
