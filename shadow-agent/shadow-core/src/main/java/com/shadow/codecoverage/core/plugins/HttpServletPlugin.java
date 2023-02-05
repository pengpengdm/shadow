package com.shadow.codecoverage.core.plugins;

import com.shadow.codecoverage.core.api.EnhanceEventWatcher;
import com.shadow.codecoverage.core.api.Interceptor;
import com.shadow.codecoverage.core.plugins.interceptor.HttpServletMethodInterceptor;
import com.shadow.codecoverage.core.utils.matcher.builder.EventWatchBuilder;
import org.kohsuke.MetaInfServices;

/**
 * @Classname HttpServletPlugin
 * @Description TODO
 * @Date 2023/2/5 11:25
 * @Created by pepsi
 */
@MetaInfServices(Interceptor.class)
public class HttpServletPlugin implements Interceptor {

    private static final String ENHANCE_CLASS = "javax.servlet.http.HttpServlet";


    @Override
    public void watch(EnhanceEventWatcher watcher) {
        EventWatchBuilder.IBuildingForClass building4Class = new EventWatchBuilder(watcher)
                .onClass(ENHANCE_CLASS);
        EventWatchBuilder.IBuildingForBehavior behavior = building4Class.onBehavior("service")
                .withParameterTypes("javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
        behavior.onWatchPluginBehavior(new HttpServletMethodInterceptor());
    }

    @Override
    public String enhanceClassName() {
        return ENHANCE_CLASS;
    }
}
