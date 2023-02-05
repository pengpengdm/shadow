package com.shadow.codecoverage.core.context;

import com.shadow.codecoverage.core.context.trace.TraceSegment;
import com.shadow.codecoverage.core.service.ReportAtmCoverDataService;
import com.shadow.codecoverage.core.service.ReportManualCoverDataService;
import com.shadow.codecoverage.core.service.ServiceManager;
import com.shadow.codecoverage.protoc.report.TraceCoverData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Classname TraceSegmentManager
 * @Description TODO
 * @Date 2023/2/3 23:14
 * @Created by pepsi
 */
public class TraceSegmentManager {

    private Logger logger = LoggerFactory.getLogger(TraceSegmentManager.class);

    public static void afterFinished(TraceSegment segment) {
        TraceCoverData traceVal = segment.transform();
        if (traceVal == null) {
            return;
        }
        try {
            if (StringUtils.isNotBlank(traceVal.getAtmReqFlag())) {
                ServiceManager.INSTANCE.findService(ReportAtmCoverDataService.class).produce(traceVal);
                return;
            }
            ServiceManager.INSTANCE.findService(ReportManualCoverDataService.class).produce(traceVal);
        } catch (Throwable throwable) {
            //todo
        }

    }


}
