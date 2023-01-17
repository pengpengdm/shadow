package com.shadow.codecoverage.core.enhance.asm;

import com.pepsi.implant.Implant;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.Set;

/**
 * @Classname PluginClassEventWeaver
 * @Description TODO
 * @Date 2023/1/15 23:24
 * @Created by pepsi
 */
public class PluginClassEventWeaver extends ClassVisitor implements Opcodes, AsmMethods {

    private final int listenerId;
    private final String targetJavaClassName;
    private final Set<String> singCodes;
    private Type ASM_TYPE_IMPLANT = Type.getType(Implant.class);

    public PluginClassEventWeaver(int api, ClassVisitor cv, int listenerId, String targetJavaClassName, Set<String> singCodes) {
        super(api, cv);
        this.listenerId = listenerId;
        this.targetJavaClassName = targetJavaClassName;
        this.singCodes = singCodes;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        final String signCode = getBehaviorSignCode(name, desc);
        if (!isMatchedBehavior(signCode)) {
            return mv;
        }
        return new PluginMethodReWrite(api, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc) {
            private final Label beginLabel = new Label();

            @Override
            protected void onMethodEnter() {
                mark(beginLabel);
                loadArgArray();
                dup();
                push(listenerId);
                push(targetJavaClassName);
                push(name);
                push(desc);
                loadThisOrPushNullIfIsStatic();
                invokeStatic(ASM_TYPE_IMPLANT, ASM_METHOD_Implant$implantPluginMethodOnBefore);
                storeArgArray();
                pop();
            }

            @Override
            protected void onMethodExit(int opcode) {
                push(listenerId);
                invokeStatic(ASM_TYPE_IMPLANT, ASM_METHOD_Implant$implantPluginMethodOnReturn);
            }

            @Override
            public void visitInsn(int opcode) {
                super.visitInsn(opcode);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }


    private boolean isMatchedBehavior(String signCode) {
        return singCodes.contains(signCode);
    }

    private String getBehaviorSignCode(String name, String desc) {
        StringBuilder builder = new StringBuilder(256)
                .append(targetJavaClassName).append("#").append(name).append("(");
        Type[] methodTypes = Type.getMethodType(desc).getArgumentTypes();
        if (methodTypes.length != 0) {
            builder.append(methodTypes[0].getClassName());
            for (int i = 1; i < methodTypes.length; i++) {
                builder.append(",").append(methodTypes[i].getClassName());
            }
        }
        return builder.toString();
    }

}
