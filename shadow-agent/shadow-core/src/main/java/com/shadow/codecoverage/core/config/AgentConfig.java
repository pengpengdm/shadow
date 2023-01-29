package com.shadow.codecoverage.core.config;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Classname AgentConfig
 * @Description TODO
 * @Date 2023/1/14 11:31
 * @Created by pepsi
 */
public class AgentConfig {

    /**
     * 排除增强的类
     */
    public static List<Pattern> INSTRU_EXCLUDE_PATTERN;

    /**
     * 需要行增强的类
     */
    public static List<Pattern> INSTRU_INCLUDE_PATTERN;

    /**
     * agent-home
     */
    public static String AGENT_HOME = "";

    /**
     * 后台服务地址,
     */
    public static String BANKEND_SERVER = "";


}
