package com.shadow.codecoverage.core;


import com.shadow.codecoverage.core.api.Interceptor;
import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.config.ConfigInitializer;
import com.shadow.codecoverage.core.config.LogbackInitializer;
import com.shadow.codecoverage.core.service.ServiceManager;
import com.shadow.codecoverage.core.utils.matcher.DefaultEventWeavaWatcher;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;

/**
 * @Classname Launch
 * @Description core 入口类,完成instr,拉起各个功能的service.
 * @Date 2023/1/14 11:20
 * @Created by pepsi
 */
public class Launcher {

    private static Launcher singleton;

    private ExecutorService executorService;

    public Launcher(String agentHome) {
        AgentConfig.AGENT_HOME = agentHome;
    }

    public void launch(String args, Instrumentation instr) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        try {
            //初始化日志
            LogbackInitializer.init(getConfigPath() + "logback.xml");
            //解析config文件到全局变量
            ConfigInitializer.INST.initialize(getConfigPath() + "agent.config");
            //解析args到全局变量
            ConfigInitializer.INST.fromArgs(args);
            //拉起所有的组件服务
            ServiceManager.INSTANCE.start();
            //行、方法增强设置
            DefaultEventWeavaWatcher eventWeavaWatcher = new DefaultEventWeavaWatcher(instr);
            ServiceLoader<Interceptor> loader = ServiceLoader.load(Interceptor.class, this.getClass().getClassLoader());
            for (Interceptor interceptor : loader) {
                interceptor.watch(eventWeavaWatcher);
            }
        } catch (Exception e) {

        }


    }

    private String getConfigPath() {
        return AgentConfig.AGENT_HOME + File.separator + "config" + File.separator;
    }

    /**
     * agent shutdown 触发
     */
    private void shutdown() {

    }
}
