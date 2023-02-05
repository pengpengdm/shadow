package com.shadow.codecoverage.core.plugins;

import com.shadow.codecoverage.core.api.EnhanceEventWatcher;
import com.shadow.codecoverage.core.api.Interceptor;
import com.shadow.codecoverage.core.plugins.interceptor.BizCodeLineMethodInterceptor;
import com.shadow.codecoverage.core.utils.matcher.builder.EventWatchBuilder;
import org.kohsuke.MetaInfServices;

/**
 * @Classname BizCodeLineEnhancePlugin
 * @Description 行级别增强，需要增强的类来自配置、服务下发等,最终于AgentConfig.
 * @Date 2023/2/5 11:36
 * @Created by pepsi
 */
@MetaInfServices(Interceptor.class)
public class BizCodeLineEnhancePlugin implements Interceptor {

    @Override
    public void watch(EnhanceEventWatcher watcher) {
        EventWatchBuilder.IBuildingForClass buildingForClass = new EventWatchBuilder(watcher)
                .onClass("-1");
        EventWatchBuilder.IBuildingForBehavior behavior = buildingForClass
                .onAnyBehavior();
        behavior.onWatchBizBehavior(new BizCodeLineMethodInterceptor());
    }

    @Override
    public String enhanceClassName() {
        return "biz-class";
    }
}
