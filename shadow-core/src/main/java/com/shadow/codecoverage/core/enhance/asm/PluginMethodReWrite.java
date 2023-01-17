package com.shadow.codecoverage.core.enhance.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @Classname PluginMethodReWrite
 * @Description TODO
 * @Date 2023/1/15 23:25
 * @Created by pepsi
 */
public class PluginMethodReWrite extends AdviceAdapter implements Opcodes, AsmMethods {

    private final Type[] argumentTypeArray;

    private final Type ASM_TYPE_OBJECT = Type.getType(Object.class);

    protected PluginMethodReWrite(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.argumentTypeArray = Type.getArgumentTypes(desc);
    }

    final protected void pushNull() {
        push((Type) null);
    }

    final protected boolean isStaticMethod() {
        return ((methodAccess & ACC_STATIC) != 0);
    }


    final protected void loadThisOrPushNullIfIsStatic() {
        if (isStaticMethod()) {
            pushNull();
        } else {
            loadThis();
        }
    }

    final protected void storeArgArray() {
        for (int i = 0; i < argumentTypeArray.length; i++) {
            dup();
            push(i);
            arrayLoad(ASM_TYPE_OBJECT);
            unbox(argumentTypeArray[i]);
            storeArg(i);
        }
    }
}
