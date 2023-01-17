package com.shadow.codecoverage.core.utils.matcher.structure;

import com.pepsi.core.utils.AgentUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;

/**
 * @Classname ClassStructureVisitor
 * @Description TODO
 * @Date 2023/1/15 11:46
 * @Created by pepsi
 */
public class ClassStructureVisitor extends ClassVisitor {

    private final int classAccess;
    public LinkedList<MethodBehaviorVisitor> methodBehaviorVisitors = new LinkedList<>();
    public LinkedList<String> fields = new LinkedList<>();

    public ClassStructureVisitor(int access) {
        super(Opcodes.ASM7);
        this.classAccess = access;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodBehaviorVisitor mv = new MethodBehaviorVisitor(access, name, descriptor);
        if (!AgentUtils.isIn(access, Opcodes.ACC_ABSTRACT)
                && !"<init>".equals(name) && !"<clinit>".equals(name)
                && !AgentUtils.isIn(classAccess, Opcodes.ACC_ENUM)
                && !AgentUtils.isIn(classAccess, Opcodes.ACC_INTERFACE)) {
            methodBehaviorVisitors.add(mv);
        }
        return mv;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldVisitor fieldVisitor = super.visitField(access, name, descriptor, signature, value);
        fields.add(name);
        return fieldVisitor;
    }
}
