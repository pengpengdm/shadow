package com.shadow.codecoverage.core.enhance;


import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.utils.AgentUtils;
import com.shadow.codecoverage.core.utils.ObjectIDs;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

/**
 * @Classname BizClassFileTransformer
 * @Description TODO
 * @Date 2023/1/15 22:09
 * @Created by pepsi
 */
public class BizClassFileTransformer implements ClassFileTransformer {

    private final int listenerId;

    public BizClassFileTransformer(EventListener eventListener) {
        this.listenerId = ObjectIDs.instance.identity(eventListener);
        EventListenerHandler.getSingleton().register(listenerId, eventListener);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String internalClassName,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (loader == null || AgentUtils.isFromSelfClass(internalClassName, loader)) {
            return null;
        }
        if (AgentUtils.isUnSupportEnhanceClasses(internalClassName)) {
            return null;
        }
        if (isMatchedBizClass(internalClassName)) {
            return transformBizClass(loader, classBeingRedefined, internalClassName, protectionDomain, classfileBuffer);
        }
        return null;
    }

    private boolean isMatchedBizClass(String internalClassName) {
        if (AgentConfig.INSTRU_EXCLUDE_PATTERN != null) {
            for (Pattern pattern : AgentConfig.INSTRU_EXCLUDE_PATTERN) {
                if (pattern.matcher(internalClassName).lookingAt()) {
                    return false;
                }
            }
        }
        if (AgentConfig.INSTRU_INCLUDE_PATTERN != null) {
            for (Pattern pattern : AgentConfig.INSTRU_INCLUDE_PATTERN) {
                if (pattern.matcher(internalClassName).lookingAt()) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] transformBizClass(ClassLoader loader, Class<?> classBeingRedefined, String internalClassName, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return new byte[0];
    }
}
