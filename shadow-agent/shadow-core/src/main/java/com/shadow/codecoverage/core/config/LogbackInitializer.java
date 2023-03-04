package com.shadow.codecoverage.core.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Classname LogbackInitializer
 * @Description TODO
 * @Date 2023/1/14 11:32
 * @Created by pepsi
 */
public class LogbackInitializer {

    public static void init(String logbackCfgFilePath) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        final File configFile = new File(logbackCfgFilePath);
        configurator.setContext(context);
        context.reset();
        final Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
        try {
            InputStream in = new FileInputStream(configFile);
            configurator.doConfigure(in);
            logger.info("init logback success");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void destroy() {
        try {
            ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
        } catch (Throwable throwable) {
            //
        }
    }
}
