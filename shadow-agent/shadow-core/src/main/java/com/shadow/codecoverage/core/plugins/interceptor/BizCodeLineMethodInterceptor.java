package com.shadow.codecoverage.core.plugins.interceptor;

import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.api.event.BeforeEvent;
import com.shadow.codecoverage.core.api.event.Event;
import com.shadow.codecoverage.core.api.event.ReturnEvent;
import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.config.GlobalMetaContext;
import com.shadow.codecoverage.core.context.ContextManager;
import com.shadow.codecoverage.core.context.trace.AbstractSpan;
import com.shadow.codecoverage.core.context.util.IdGenerator;
import com.shadow.codecoverage.core.dto.MethodInformation;
import com.shadow.codecoverage.core.service.ReportManualCoverDataService;
import com.shadow.codecoverage.core.service.ServiceManager;
import com.shadow.codecoverage.core.utils.AgentUtils;
import com.shadow.codecoverage.protoc.report.SpanCoverData;
import com.shadow.codecoverage.protoc.report.TraceCoverData;
import sun.management.Agent;

import java.util.*;

/**
 * @Classname BizCodeLineMethodInterceptor
 * @Description TODO
 * @Date 2023/2/5 11:38
 * @Created by pepsi
 */
public class BizCodeLineMethodInterceptor implements EventListener {


    @Override
    public void onEvent(Event event) throws Throwable {
        Event.Type type = event.eventType;
        switch (type) {
            case BEFORE:
                beforeMethod((BeforeEvent) event);
                break;
            case RETURN:
                afterMethod((ReturnEvent) event);
                break;
            default:
                break;
        }
    }

    private void afterMethod(ReturnEvent event) {
        int methodId = event.methodId;
        MethodInformation methodInformation = GlobalMetaContext.getMethodMetaInfo(methodId);

        Set<Integer> lines = new HashSet<>();

        BitSet coverLines = event.coverLines;

        for (int i = coverLines.nextClearBit(0); i >= 0; i = coverLines.nextSetBit(i + 1)) {
            lines.add(i);
        }
        //todo 可以用这个数据做其他事情

        if (ContextManager.getTraceId() != null) {
            try {
                AbstractSpan span = ContextManager.getActiveSpan();
                if (span == null) {
                    //
                    return;
                }
                span.setCoverLines(lines);
                span.setMethodId(methodId);
            } catch (Throwable throwable) {
                //
            } finally {
                ContextManager.stopSpan();
            }
            return;
        }
        //
        GlobalMetaContext.filterCoveredLines(lines, methodId);
        if (lines.isEmpty()) {
            return;
        }
        ServiceManager.INSTANCE.findService(ReportManualCoverDataService.class)
                .produce(assemblyCommonCoverTrace(methodId, lines));
    }

    private TraceCoverData assemblyCommonCoverTrace(int methodId, Set<Integer> coverLines) {
        if(coverLines.isEmpty()){
            return null;
        }
        MethodInformation methodInformation = GlobalMetaContext.getMethodMetaInfo(methodId);
        TraceCoverData.Builder  coverTrace = TraceCoverData.newBuilder();
        coverTrace.setAppId(AgentConfig.APP_ID)
                .setMachineInf(AgentConfig.MACHINE_INF)
                .setTraceId(IdGenerator.traceIdGenerator());
        List<SpanCoverData> spans =new ArrayList<>();
        SpanCoverData.Builder spanBuild = SpanCoverData.newBuilder();
        String methodSign = methodInformation.getClassName()+"."+methodInformation.getName()+";"+methodInformation.getAccess()+";"
                +methodInformation.getDesc()+";"+methodInformation.getStartLineNum();
        spanBuild.setMethodKey(AgentUtils.hashForMethodKey(methodSign))
                .setClassName(methodInformation.getClassName())
                .setJarName(methodInformation.getJarName())
                .setMethodName(methodInformation.getName())
                .addAllCoverLines(AgentUtils.coverNumMerge(coverLines))
                .setSpanId(methodId)
                .setParentId(-1);
        spans.add(spanBuild.build());
        coverTrace.addAllSpanVals(spans);
        return coverTrace.build();
    }

    private void beforeMethod(BeforeEvent event) {
        MethodInformation methodInformation = GlobalMetaContext.getMethodMetaInfo(event.methodId);

        if (ContextManager.getTraceId() != null && methodInformation != null) {
            AbstractSpan span = ContextManager.createLocalSpan();
            if (span == null) {
                //
                return;
            }
            span.setClassName(methodInformation.getClassName());
            span.setMethodSign(methodInformation.getDesc());
            span.setMethodName(methodInformation.getName());
            span.setStartLineNum(methodInformation.getStartLineNum());
        }
    }
}
