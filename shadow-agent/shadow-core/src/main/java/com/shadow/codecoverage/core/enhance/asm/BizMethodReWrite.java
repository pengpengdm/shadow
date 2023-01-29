package com.shadow.codecoverage.core.enhance.asm;

import com.pepsi.implant.Implant;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.BitSet;

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
    private final List<> newReplacementLocations = new ArrayList<>();
    private final Label methodBegin = new Label(), methodEnd = new Label();
    private Type ASM_TYPE_IMPLANT = Type.getType(Implant.class);
    private Type BIT_SET_TYPE = Type.getType(BitSet.class);
    private Type THROWABLE_TYPE = Type.getType(Throwable.class);
    private boolean hasEntered = false, canInstrument = true;

    private boolean isPendingTrace = true;

    private int currentLine = 0;

    private boolean trackingLines = false;

    private int lineMapVar;

    private boolean catchingException = false;


}
