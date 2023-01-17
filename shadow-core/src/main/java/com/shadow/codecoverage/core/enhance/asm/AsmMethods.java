package com.shadow.codecoverage.core.enhance.asm;

import com.shadow.codecoverage.implant.Implant;
import org.objectweb.asm.commons.Method;

import java.util.BitSet;

/**
 * @Classname AsmMethods
 * @Description TODO
 * @Date 2023/1/15 23:23
 * @Created by pepsi
 */
public interface AsmMethods {

    /**
     * asm method of {@link com.shadow.codecoverage.implant.Implant#implantMethodOnBefore(int, int)}
     */
    Method ASM_METHOD_Implant$implantMethodOnBefore = InnerHelper.getAsmMethod(
            Implant.class,
            "implantMethodOnBefore",
            int.class, int.class
    );
    /**
     * asm method of {@link com.shadow.codecoverage.implant.Implant#recordMethodCoverLines(int, int, BitSet)}
     */
    Method ASM_METHOD_Implant$recordMethodCoverLines = InnerHelper.getAsmMethod(
            Implant.class,
            "recordMethodCoverLines",
            String.class, int.class, BitSet.class
    );
    /**
     * asm method of {@link com.shadow.codecoverage.implant.Implant#implantMethodOnReturn(int, int, int)}
     */
    Method ASM_METHOD_Implant$implantMethodOnReturn = InnerHelper.getAsmMethod(
            Implant.class,
            "implantMethodOnReturn",
            int.class, int.class, int.class
    );
    /**
     * asm method of {@link com.shadow.codecoverage.implant.Implant#implantPluginMethodOnBefore(Object[], int, String, String, String, Object)}`
     */
    Method ASM_METHOD_Implant$implantPluginMethodOnBefore = InnerHelper.getAsmMethod(
            Implant.class,
            "implantPluginMethodOnBefore",
            Object[].class, int.class, String.class, String.class, String.class, Object.class
    );
    /**
     * asm method of {@link com.shadow.codecoverage.implant.Implant#implantPluginMethodOnReturn(int)}
     */
    Method ASM_METHOD_Implant$implantPluginMethodOnReturn = InnerHelper.getAsmMethod(
            Implant.class,
            "implantPluginMethodOnReturn",
            int.class
    );

    class InnerHelper {
        private InnerHelper() {
        }

        static Method getAsmMethod(final Class<?> clazz,
                                   final String methodName,
                                   final Class<?>... parameterClassArray) {
            java.lang.reflect.Method method = null;
            try {
                method = clazz.getDeclaredMethod(methodName, parameterClassArray);
            } catch (NoSuchMethodException e) {
                //
            }
            return Method.getMethod(method);
        }
    }
}
