package com.shadow.codecoverage.core.context.util;

import com.shadow.codecoverage.core.config.AgentConfig;

/**
 * @Classname IdGenerator
 * @Description TODO
 * @Date 2023/2/3 23:51
 * @Created by pepsi
 */
public class IdGenerator {

    private static int reqNum = 0;

    public static String traceIdGenerator() {
        return AgentConfig.MACHINE_4_INT + "_" + AgentConfig.APP_ID + "_" + nextSeq();
    }

    private static synchronized long nextSeq() {
        if (reqNum >= 1000) {
            reqNum = 0;
        }
        reqNum += 1;
        return System.currentTimeMillis() * 1000 + reqNum;
    }
}
