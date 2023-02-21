package com.shadow.codecoverage.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Classname AgentConfig
 * @Description TODO
 * @Date 2023/1/14 11:31
 * @Created by pepsi
 */
public class AgentConfig {

    public static List<String> AUTOMATIC_REQUEST_FLAG_KEYS = new ArrayList<>();
    public static String ROCKETMQ_NAME_SRV_ADDR;
    public static String REPORT_ATM_COVERDATA_TOPIC;

    public static String REPORT_MANUAL_COVERDATA_TOPIC;
    public static String APP_ID;

    /**
     * ip / k8 pod info
     */
    public static String MACHINE_INF;

    /**
     * MACHINE_INF -> int
     */
    public static String MACHINE_4_INT;

    /**
     * 排除增强的类
     */
    public static List<String> INSTRU_EXCLUDE_PATTERN;

    /**
     * 需要行增强的类
     */
    public static List<String> INSTRU_INCLUDE_PATTERN;

    /**
     * agent-home
     */
    public static String AGENT_HOME = "";

    /**
     * 后台服务地址,
     */
    public static String BANKEND_SERVER = "";


}
