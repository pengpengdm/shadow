package com.shadow.codecoverage.core.enhance.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @Classname ByteCodeUtil
 * @Description TODO
 * @Date 2023/1/15 23:24
 * @Created by pepsi
 */
public class ByteCodeUtil {

    public ByteCodeUtil() {

    }

    public static void pushInt(final MethodVisitor mv, final int value) {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }
}
