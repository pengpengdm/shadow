package com.shadow.codecoverage.core.enhance.asm;

import com.shadow.codecoverage.implant.Implant;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @Classname BizMethodReWrite
 * @Description TODO
 * @Date 2023/1/15 23:24
 * @Created by pepsi
 */
public class BizMethodReWrite extends AdviceAdapter implements Opcodes, AsmMethods {

    private final int methodId;
    private final int listenerId;
    private final int startLine;
    private final int endLine;
    private final int access;
    private final String desc;
    private final String className;
    private final String methodName;
    private final boolean isConstructor;
    private final List<NewLocation> newReplacementLocations = new ArrayList<>();
    private final Label methodBegin = new Label(), methodEnd = new Label();
    private final Type ASM_TYPE_IMPLANT = Type.getType(Implant.class);
    private final Type BIT_SET_TYPE = Type.getType(BitSet.class);
    private final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    private final boolean catchingException = false;
    private Logger logger = LoggerFactory.getLogger(BizMethodReWrite.class);
    private boolean hasEntered = false, canInstrument = true;
    private boolean isPendingLineTrace = true;
    private int currentLine = 0;
    private boolean trackingLines = false;
    private int lineMapVar;
    private boolean catchingExceptions = false;

    public BizMethodReWrite(int api, MethodVisitor methodVisitor, String className, int access, String name, String desc,
                            int methodId, int listenerId, int startLine, int endLine) {
        super(api, methodVisitor, access, name, desc);
        this.methodId = methodId;
        this.listenerId = listenerId;
        this.startLine = startLine;
        this.endLine = endLine;
        this.access = access;
        this.desc = desc;
        this.className = className;
        this.methodName = name;
        this.isConstructor = name.equals("<init>");
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (isConstructor) {
            initializeLineLevelInstrumentation();
        }
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();

        if (!isConstructor) {
            initializeLineLevelInstrumentation();
        }

        if (canInstrument && !hasEntered) {
            openTryCatchWrap();
            instrumentEntry();
            hasEntered = true;
        } else {
            logger.debug("method instrumentation", String.format("cannot instrument method %s.%s:%s; skipping", className, methodName, desc));
        }
        if (hasEntered && isPendingLineTrace) {
            instrumentLine();
            logger.debug("method instrumentation", String.format("line level coverage for %s lines %d-%d potentially missing", className, methodName, currentLine));
        }
    }

    @Override
    public void visitLineNumber(int line, Label label) {
        super.visitLineNumber(line, label);
        currentLine = line;
        isPendingLineTrace = true;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        instrumentLine();
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInsn(int opcode) {
        instrumentLine();
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        instrumentLine();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        instrumentLine();
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        instrumentLine();
        super.visitJumpInsn(opcode, label);
        if (opcode == GOTO) {
            // todo
        }
        // any jumping in constructors prior to super constructor call is branching, and too
        // complex for us to instrument currently
        if (isConstructor && !hasEntered) {
            canInstrument = false;
            logger.trace("method instrumentation", String.format("jump encountered in constructor %s.%s:%s prior to object initialization; unable to instrument",
                    className, methodName, desc));
        }
    }

    @Override
    public void visitLdcInsn(Object cst) {
        instrumentLine();
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        instrumentLine();
        super.visitLookupSwitchInsn(dflt, keys, labels);

        // this is branching, too complex prior to super constructor call
        if (isConstructor && !hasEntered) {
            canInstrument = false;
            logger.trace("method instrumentation", String.format("lookup switch encountered in constructor %s.%s:%s prior to object initialization; unable to instrument",
                    className, methodName, desc));
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        instrumentLine();
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        instrumentLine();
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        instrumentLine();
        super.visitTableSwitchInsn(min, max, dflt, labels);

        // this is branching, too complex prior to super constructor call
        if (isConstructor && !hasEntered) {
            canInstrument = false;
            logger.trace("method instrumentation", String.format("table switch encountered in constructor %s.%s:%s prior to object initialization; unable to instrument",
                    className, methodName, desc));
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        // stackmap frames may refer to 'NEW' instruction offsets when dealing with uninitialized
        // values. because we're causing the location to change, we need to keep track of this and
        // later rewrite any affected frames

        if (opcode == Opcodes.NEW) {
            // label the original location of the new instruction
            Label original = new Label();
            mv.visitLabel(original);

            // inject line-level instrumentation
            instrumentLine();

            // label the replacement location of the new instruction
            Label replacement = new Label();
            mv.visitLabel(replacement);

            // continue with the new op
            super.visitTypeInsn(opcode, type);

            // record the replacement
            newReplacementLocations.add(new NewLocation(original, replacement));
        } else {
            instrumentLine();
            super.visitTypeInsn(opcode, type);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        instrumentLine();
        super.visitVarInsn(opcode, var);

        // for constructors, a RET call is a form of branching, so this sort of complexity before
        // a super constructor call means we can't easily instrument
        if (isConstructor && !hasEntered && opcode == Opcodes.RET) {
            canInstrument = false;
            logger.trace("method instrumentation", String.format("ret encountered in constructor %s.%s:%s prior to object initialization; unable to instrument",
                    className, methodName, desc));
        }
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        instrumentLine();
        super.visitIincInsn(var, increment);
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);

        // if we're exiting via a throw, our try/catch will handle it
        if (hasEntered && opcode != Opcodes.ATHROW) {
            instrumentExit(false);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // visitMaxs is called after the code is complete, so we close our top level try/catch block here
        if (hasEntered) {
            closeTryCatchWrap();
        }

        // COMPUTE_MAXS will take care of figuring out max stack/locals
        super.visitMaxs(0, 0);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        // rewrite uninitialized local and stack entries (ASM represents these by using labels)
        // that have been impacted by line level coverage

        Object[] localReplacement = new Object[nLocal];
        Object[] stackReplacement = new Object[nStack];

        for (int i = 0; i < nLocal; ++i) {
            localReplacement[i] = local[i];

            if (local[i] instanceof Label) {
                int offset = ((Label) local[i]).getOffset();

                for (NewLocation nl : newReplacementLocations) {
                    if (nl.original.getOffset() == offset)
                        localReplacement[i] = nl.replacement;
                }
            }
        }

        for (int i = 0; i < nStack; ++i) {
            stackReplacement[i] = stack[i];

            if (stack[i] instanceof Label) {
                int offset = ((Label) stack[i]).getOffset();

                for (NewLocation nl : newReplacementLocations) {
                    if (nl.original.getOffset() == offset)
                        stackReplacement[i] = nl.replacement;
                }
            }
        }

        super.visitFrame(type, nLocal, localReplacement, nStack, stackReplacement);
    }

    /**
     * 初始化 bitset
     */
    private void initializeLineLevelInstrumentation() {
        lineMapVar = newLocal(BIT_SET_TYPE);
        mv.visitTypeInsn(Opcodes.NEW, BIT_SET_TYPE.getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                BIT_SET_TYPE.getInternalName(),
                "<init>",
                Type.getMethodDescriptor(
                        Type.VOID_TYPE,
                        Type.INT_TYPE
                ),
                false
        );
        mv.visitVarInsn(Opcodes.ASTORE, lineMapVar);
        trackingLines = true;
    }


    /**
     * instrumentation to observe exceptions bubbling out of the method
     */
    private void openTryCatchWrap() {
        // insert start label for the try block
        mv.visitLabel(methodBegin);
        catchingExceptions = true;
    }

    /**
     * instrumentation to observe exceptions bubbling out of the method
     */
    private void closeTryCatchWrap() {
        if (!catchingExceptions) return;

        // wire up our try/catch around the whole method, so we can observe exception bubbling
        mv.visitTryCatchBlock(methodBegin, methodEnd, methodEnd, null);
        mv.visitLabel(methodEnd);

        // when catching an exception, we need a full frame for the handler
        buildCatchFrame();

        instrumentExit(true);

        // rethrow the exception we caught
        mv.visitInsn(Opcodes.ATHROW);
    }

    /**
     * build a frame for the top-level try/catch exception handler
     */
    private void buildCatchFrame() {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        Object[] locals = new Object[argumentTypes.length + 2]; // at most we have two more locals than the arguments
        int l = 0;

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // not static, so add our own type
            locals[l++] = className;
        }

        // add the argument types
        for (Type argument : argumentTypes) {
            switch (argument.getSort()) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    locals[l++] = Opcodes.INTEGER;
                    break;

                case Type.FLOAT:
                    locals[l++] = Opcodes.FLOAT;
                    break;

                case Type.LONG:
                    locals[l++] = Opcodes.LONG;
                    break;

                case Type.DOUBLE:
                    locals[l++] = Opcodes.DOUBLE;
                    break;

                case Type.ARRAY:
                case Type.OBJECT:
                    locals[l++] = argument.getInternalName();
                    break;
            }
        }

        if (trackingLines) {
            // add our local bitset
            locals[l++] = BIT_SET_TYPE.getInternalName();
        }

        // new frame with our calculated locals, and a stack with throwable
        mv.visitFrame(
                Opcodes.F_NEW,
                l, locals,
                1, new Object[]{THROWABLE_TYPE.getInternalName()}
        );
    }

    /**
     * instrumentation to track method entries
     */
    private void instrumentEntry() {
        ByteCodeUtil.pushInt(mv, listenerId);
        ByteCodeUtil.pushInt(mv, methodId);
        invokeStatic(ASM_TYPE_IMPLANT, ASM_METHOD_Implant$implantMethodOnBefore);
    }

    /**
     * instrumentation to track method exits
     */
    private void instrumentExit(boolean inCatchBlock) {
        ByteCodeUtil.pushInt(mv, listenerId);
        ByteCodeUtil.pushInt(mv, methodId);
        mv.visitIntInsn(Opcodes.BIPUSH, inCatchBlock ? 1 : 0);
        invokeStatic(ASM_TYPE_IMPLANT, ASM_METHOD_Implant$implantMethodOnReturn);
        if (trackingLines) {
            ByteCodeUtil.pushInt(mv, listenerId);
            ByteCodeUtil.pushInt(mv, methodId);
            mv.visitVarInsn(Opcodes.ALOAD, lineMapVar);
            invokeStatic(ASM_TYPE_IMPLANT, ASM_METHOD_Implant$implantRecordMethodCoverLines);
        }
    }

    /**
     * instrumentation to track line-level execution
     */
    private void instrumentLine() {
        if (canInstrument && trackingLines && isPendingLineTrace) {
            mv.visitVarInsn(Opcodes.ALOAD, lineMapVar);
            ByteCodeUtil.pushInt(mv, currentLine);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    BIT_SET_TYPE.getInternalName(),
                    "set",
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.INT_TYPE
                    ),
                    false
            );
        }
        isPendingLineTrace = false;
    }

    private class NewLocation {
        public final Label original, replacement;

        public NewLocation(Label original, Label replacement) {
            this.original = original;
            this.replacement = replacement;
        }
    }
}
