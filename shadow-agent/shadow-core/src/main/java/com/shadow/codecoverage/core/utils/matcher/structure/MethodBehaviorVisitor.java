package com.shadow.codecoverage.core.utils.matcher.structure;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

/**
 * @Classname MethodBehaviorVisitor
 * @Description TODO
 * @Date 2023/1/15 11:48
 * @Created by pepsi
 */
public class MethodBehaviorVisitor extends MethodVisitor {

    public final int access;
    public final Set<Integer> usefulLines;
    private final String name, desc;
    public int startLine = 0, endLine = 0;

    public MethodBehaviorVisitor(int access, String name, String descriptor) {
        super(Opcodes.ASM7);
        this.access = access;
        this.name = name;
        this.desc = descriptor;
        this.usefulLines = new HashSet<>();
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        if (line < startLine || startLine == 0) {
            startLine = line;
        }
        if (line > endLine) {
            endLine = line;
        }
        usefulLines.add(line);
    }
}
