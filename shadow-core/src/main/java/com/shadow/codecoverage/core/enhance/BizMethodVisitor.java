package com.shadow.codecoverage.core.enhance;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @Classname BizMethodVisitor
 * @Description TODO
 * @Date 2023/1/15 22:09
 * @Created by pepsi
 */
public class BizMethodVisitor extends MethodVisitor {

    private int startLine, endLine = 0;


    public BizMethodVisitor(int api) {
        super(api);
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
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartLine() {
        return startLine;
    }
}
