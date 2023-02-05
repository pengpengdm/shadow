package com.shadow.codecoverage.core.config;

import com.shadow.codecoverage.core.dto.ClassInformation;
import com.shadow.codecoverage.core.dto.MethodInformation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname GlobalMetaContext
 * @Description TODO
 * @Date 2023/1/16 00:32
 * @Created by pepsi
 */
public class GlobalMetaContext {


    private static final AtomicInteger METHOD_SEQUECE = new AtomicInteger(1000);

    private static final AtomicInteger CLASS_SEQUECE = new AtomicInteger(1000);

    private static ConcurrentHashMap<String, ClassInformation> CLASS_META_INFO = new ConcurrentHashMap<>(128);

    private static ConcurrentHashMap<Integer, MethodInformation> METHOD_META_INFO = new ConcurrentHashMap<>(512);

    private static ConcurrentHashMap<Integer, boolean[]> METHOD_COVERED_LINES = new ConcurrentHashMap<>(512);


    public static ConcurrentHashMap<String, ClassInformation> getClassMetaInfo() {
        return CLASS_META_INFO;
    }

    public static MethodInformation getMethodMetaInfo(Integer methodId) {
        return METHOD_META_INFO.get(methodId);
    }

    public static boolean[] getMethodCoveredLines(int methodId) {
        return METHOD_COVERED_LINES.get(methodId);
    }

    public static int setClassMetaInfo(ClassInformation classMetaInfo) {
        CLASS_META_INFO.put(classMetaInfo.getName(), classMetaInfo);
        return classMetaInfo.getClassId();
    }

    public static int setMethodMetaInfo(Integer methodId, MethodInformation methodMetaInfo) {
        METHOD_META_INFO.put(methodId, methodMetaInfo);
        ClassInformation classInformation = CLASS_META_INFO.get(methodMetaInfo.getClassName());
        classInformation.getMethodIds().add(methodId);
        return methodId;
    }

    public static int recordClassInf(String className, String jarName) {
        int classId = CLASS_SEQUECE.getAndIncrement();
        return setClassMetaInfo(new ClassInformation(className, jarName, classId));
    }


    public static int recordMethodInf(int classId, int access, String name, String descriptor, String className, String jarName, int startLine, int endLine, String methodKey) {
        return setMethodMetaInfo(METHOD_SEQUECE.getAndIncrement(),
                new MethodInformation(classId, access, name, descriptor, className, jarName, startLine, endLine, methodKey));
    }


    public static synchronized void recordCoveredLines(List<Integer> coverLines, int methodId) {
        if (coverLines == null || coverLines.isEmpty()) {
            return;
        }
        MethodInformation methodInformation = getMethodMetaInfo(methodId);
        if (methodInformation == null) {
            return;
        }
        boolean[] coveredLines = getMethodCoveredLines(methodId);
        if (coveredLines == null) {
            initMethodCoverArr(methodId, methodInformation.getStartLineNum(), methodInformation.getEndLineNum());
            return;
        }
        int startLineNum = methodInformation.getStartLineNum();
        Iterator<Integer> it = coverLines.iterator();
        while (it.hasNext()) {
            Integer coverLine = it.next();
            int index = coverLine - startLineNum;
            coveredLines[index] = true;
        }
    }

    public static void filterCoveredLines(Set<Integer> coverLines, int methodId) {
        MethodInformation methodInformation = getMethodMetaInfo(methodId);
        if (methodInformation == null) {
            return;
        }
        boolean[] coveredLines = getMethodCoveredLines(methodId);
        if (coveredLines == null) {
            initMethodCoverArr(methodId, methodInformation.getStartLineNum(), methodInformation.getEndLineNum());
            return;
        }
        Iterator<Integer> it = coverLines.iterator();
        while (it.hasNext()) {
            Integer coverLineNum = it.next();
            int index = coverLineNum - methodInformation.getStartLineNum();
            if (coveredLines[index]) {
                it.remove();
            }
        }
    }

    private static boolean[] initMethodCoverArr(int methodId, int startLineNum, int endLineNum) {
        int len = endLineNum - startLineNum + 1;
        boolean[] initData = new boolean[len];
        METHOD_COVERED_LINES.put(methodId, initData);
        return initData;
    }
}
