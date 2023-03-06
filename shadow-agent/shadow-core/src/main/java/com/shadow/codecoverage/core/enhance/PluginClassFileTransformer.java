package com.shadow.codecoverage.core.enhance;

import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.enhance.asm.PluginClassEventWeaver;
import com.shadow.codecoverage.core.utils.AgentUtils;
import com.shadow.codecoverage.core.utils.ObjectIDs;
import com.shadow.codecoverage.core.utils.matcher.Matcher;
import com.shadow.codecoverage.core.utils.matcher.MatchingResult;
import com.shadow.codecoverage.core.utils.matcher.structure.ClassStructure;
import com.shadow.codecoverage.core.utils.matcher.structure.ClassStructureImplByAsm;
import com.shadow.codecoverage.implant.Implant;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

/**
 * @Classname PluginClassFileTransformer
 * @Description TODO
 * @Date 2023/1/15 22:08
 * @Created by pepsi
 */
public class PluginClassFileTransformer implements ClassFileTransformer {

    private final Matcher matcher;

    private final int lisenerId;

    public PluginClassFileTransformer(Matcher matcher, EventListener eventListener) {
        this.matcher = matcher;
        this.lisenerId = ObjectIDs.instance.identity(eventListener);
        EventListenerHandler.getSingleton().register(lisenerId, eventListener);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String internalClassName,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] srcByteCodeArray) throws IllegalClassFormatException {
        if (loader == null || AgentUtils.isFromSelfClass(internalClassName, loader)) {
            return null;
        }
        if (AgentUtils.isUnSupportEnhanceClasses(internalClassName)) {
            return null;
        }
        ClassStructure classStructure = new ClassStructureImplByAsm(srcByteCodeArray, loader);
        MatchingResult matchingResult = matcher.matching(classStructure);
        Set<String> behaviorSignCodes = matchingResult.getBehaviorSignCodes();
        if (matchingResult.isMatched()) {
            return transformPluginClass(loader, internalClassName, srcByteCodeArray, behaviorSignCodes);
        }
        return null;
    }

    private byte[] transformPluginClass(ClassLoader loader,
                                        String internalClassName,
                                        byte[] srcByteCodeArray,
                                        Set<String> behaviorSignCodes) {
        try {
            final ClassReader cr = new ClassReader(srcByteCodeArray);
            final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cr.accept(new PluginClassEventWeaver(Opcodes.ASM7, cw, lisenerId, cr.getClassName(), behaviorSignCodes), ClassReader.EXPAND_FRAMES);
            return AgentUtils.dumpCLassIfNecessary(cr.getClassName(), cw.toByteArray());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
