package com.shadow.codecoverage.core.enhance;

import com.pepsi.core.utils.AgentUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname BizClassVisitor
 * @Description TODO
 * @Date 2023/1/15 22:09
 * @Created by pepsi
 */
public class BizClassVisitor extends ClassVisitor {

    private int api;
    private String className;
    private Map<String, BizMethodVisitor> methodVisitorMap = new HashMap<>();


    public BizClassVisitor(int api) {
        super(api);
        this.api = api;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final BizMethodVisitor mv = new BizMethodVisitor(api);
        String mkey = AgentUtils.hashForMethodKey(className + "." + name + ";" + access + ";" + descriptor);
        methodVisitorMap.put(mkey, mv);
        return mv;
    }

    public Map<String, BizMethodVisitor> getMethodVisitorMap() {
        return methodVisitorMap;
    }
}
