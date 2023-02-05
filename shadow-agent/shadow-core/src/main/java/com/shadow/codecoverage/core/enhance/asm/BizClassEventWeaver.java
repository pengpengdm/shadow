package com.shadow.codecoverage.core.enhance.asm;


import com.shadow.codecoverage.core.config.GlobalMetaContext;
import com.shadow.codecoverage.core.enhance.BizMethodVisitor;
import com.shadow.codecoverage.core.utils.AgentUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

/**
 * @Classname BizClassEventWeaver
 * @Description TODO
 * @Date 2023/1/15 23:23
 * @Created by pepsi
 */
public class BizClassEventWeaver extends ClassVisitor implements Opcodes, AsmMethods {

    private final int classId;

    private final int listenerId;

    private final String jarName;
    private final Map<String, BizMethodVisitor> methodVisitorMap;
    private boolean isInterface;
    private String className;

    public BizClassEventWeaver(int api,
                               ClassVisitor classVisitor,
                               int listenerId,
                               int classId,
                               String jarName,
                               Map<String, BizMethodVisitor> methodVisitorMap) {
        super(api, classVisitor);
        this.classId = classId;
        this.listenerId = listenerId;
        this.jarName = jarName;
        this.methodVisitorMap = methodVisitorMap;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.isInterface = AgentUtils.isIn(access, ACC_INTERFACE);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv == null || "<clinit>".equals(name)) {
            return mv;
        }
        boolean isAbstractMethod = AgentUtils.isIn(access, ACC_ABSTRACT);
        if (isAbstractMethod || isAbstractMethod) {
            return mv;
        }
        String internalMethodKey = AgentUtils.hashForMethodKey(className + "." + name + ";" + access + ";" + descriptor);
        BizMethodVisitor bizMethodVisitor = methodVisitorMap.get(internalMethodKey);
        if (bizMethodVisitor == null) {
            return mv;
        }
        String fullyClassName = className;
        if (fullyClassName.contains("$")) {
            fullyClassName = fullyClassName.substring(0, fullyClassName.indexOf("$"));
        }
        String methodKey = AgentUtils.hashForMethodKey(fullyClassName + "." + name + ";" + access + ";" + descriptor + ";" + bizMethodVisitor.getStartLine());
        int methodId = GlobalMetaContext.recordMethodInf(classId, access, name, descriptor, className, jarName, bizMethodVisitor.getStartLine(), bizMethodVisitor.getEndLine(), methodKey);
        return new BizMethodReWrite(api, mv, className, access, name, descriptor, methodId, listenerId, bizMethodVisitor.getStartLine(), bizMethodVisitor.getEndLine());

    }
}
