package com.shadow.codecoverage.core.utils.matcher;


import com.shadow.codecoverage.core.api.EnhanceEventWatcher;
import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.enhance.BizClassFileTransformer;
import com.shadow.codecoverage.core.enhance.EventListenerHandler;
import com.shadow.codecoverage.core.enhance.PluginClassFileTransformer;
import com.shadow.codecoverage.core.utils.matcher.builder.EventWatchCondition;
import com.shadow.codecoverage.implant.Implant;

import java.lang.instrument.Instrumentation;

/**
 * @Classname DefaultEventWeavaWatcher
 * @Description TODO
 * @Date 2023/1/15 22:07
 * @Created by pepsi
 */
public class DefaultEventWeavaWatcher implements EnhanceEventWatcher {

    private final Instrumentation instr;

    public DefaultEventWeavaWatcher(Instrumentation instr) {
        this.instr = instr;
    }


    @Override
    public void watchPluginBehavior(EventWatchCondition condition, EventListener listener) {
        Matcher matcher = FilterMatcher.toOrGroupMatcher(condition.getOrFilterArray());
        final PluginClassFileTransformer classFileTransformer = new PluginClassFileTransformer(matcher, listener);
        instr.addTransformer(classFileTransformer, true);
    }

    @Override
    public void watchBizBehavior(EventListener listener) {
        final BizClassFileTransformer classFileTransformer = new BizClassFileTransformer(listener);
        instr.addTransformer(classFileTransformer, true);
    }
}
