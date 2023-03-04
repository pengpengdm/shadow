package com.shadow.codecoverage.core.enhance;


import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.config.GlobalMetaContext;
import com.shadow.codecoverage.core.enhance.asm.BizClassEventWeaver;
import com.shadow.codecoverage.core.utils.AgentUtils;
import com.shadow.codecoverage.core.utils.ObjectIDs;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.ASM7;

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
        if (AgentConfig.INSTRU_EXCLUDE_PACKAGE != null) {
            for (String str : AgentConfig.INSTRU_EXCLUDE_PACKAGE) {
                Pattern pattern = Pattern.compile(str);
                if (pattern.matcher(internalClassName).lookingAt()) {
                    return false;
                }
            }
        }
        if (AgentConfig.INSTRU_INCLUDE_PACKAGE != null) {
            for (String str : AgentConfig.INSTRU_INCLUDE_PACKAGE) {
                Pattern pattern = Pattern.compile(str);
                if (pattern.matcher(internalClassName).lookingAt()) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] transformBizClass(ClassLoader loader,
                                     Class<?> classBeingRedefined,
                                     String internalClassName,
                                     ProtectionDomain protectionDomain,
                                     byte[] classfileBuffer) {
        int classId = GlobalMetaContext.recordClassInf(internalClassName, "");
        try {
            final ClassReader cr = new ClassReader(classfileBuffer);
            final BizClassVisitor classVisitor = new BizClassVisitor(ASM7);
            cr.accept(classVisitor, 0);
            Map<String, BizMethodVisitor> methodVisitorMap = classVisitor.getMethodVisitorMap();
            final ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            cr.accept(new BizClassEventWeaver(
                    ASM7, classWriter, listenerId, classId, "", methodVisitorMap), ClassWriter.COMPUTE_FRAMES);
            return AgentUtils.dumpCLassIfNecessary(cr.getClassName(), classWriter.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
