package com.shadow.codecoverage.core.utils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

/**
 * @Classname AgentUtils
 * @Description TODO
 * @Date 2023/1/15 09:59
 * @Created by pepsi
 */
public class AgentUtils {


    private static List<String> SELF_CLASS_PREFIXS = new ArrayList<>();

    static {
        SELF_CLASS_PREFIXS.add("com/pepsi/");
    }


    public static String toInternalClassName(String javaClassName) {
        if (StringUtils.isEmpty(javaClassName)) {
            return javaClassName;
        }
        return javaClassName.replace('.', '/');
    }

    /**
     * java/lang/String to java.lang.String
     */
    public static String toJavaClassName(String internalClassName) {
        if (StringUtils.isEmpty(internalClassName)) {
            return internalClassName;
        }
        return internalClassName.replace('/', '.');
    }


    /**
     * 修饰符
     *
     * @param target
     * @param maskArray
     * @return
     */
    public static boolean isIn(int target, int... maskArray) {
        if (maskArray == null || maskArray.length < 1) {
            return false;
        }
        for (int mask : maskArray) {
            if ((target & mask) == mask) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFromSelfClass(String className, ClassLoader loader) {
        if (null != className) {
            for (String prefix : SELF_CLASS_PREFIXS) {
                if (className.contains(prefix)) {
                    return true;
                }
            }
        }
        if (loader.getClass().getName().startsWith("com.pepsi")) {
            return true;
        }
        return false;
    }

    /**
     * 目前不支持的增强
     *
     * @param className
     * @return
     */
    public static boolean isUnSupportEnhanceClasses(String className) {
        return className == null
                || className.contains("$$Lambda$")
                || className.contains("$$FastClassBySpringCGLIB$$")
                || className.contains("$$EnhancerBySpringCGLIB$$")
                || className.contains("$$EnhancerByCGLIB$$")
                || className.contains("$$FastClassByCGLIB$$");
    }

    public static String hashForMethodKey(String key) {
        String cacheKey;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;

    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0XFF & bytes[i]);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public static byte[] dumpCLassIfNecessary(String className, byte[] data) {
        File dumpClassFile = new File(getDumpClassPath() + className + ".class");
        File classPath = new File(dumpClassFile.getParent());
        if (!classPath.mkdirs() && !classPath.exists()) {
            return data;
        }
        try {
            writeByteArrayToFile(dumpClassFile, data);
        } catch (Exception e) {
            //ignore
        }
        return data;
    }

    private static String getDumpClassPath() {
        return System.getProperty("agent.home") + File.separator + "dump" + File.separator;
    }

    public static List<Integer> splitMergedNum(List<String> mergedNums) {
        List<Integer> coverLines = new ArrayList<>();
        if (mergedNums == null || mergedNums.isEmpty()) {
            return coverLines;
        }
        for (int i = 0; i < mergedNums.size(); i++) {
            String line = mergedNums.get(i);
            int index = line.indexOf("-");
            if (index > 0) {
                int startNum = Integer.parseInt(line.substring(0, index));
                int endNum = Integer.parseInt(line.substring(index + 1));
                while (endNum >= startNum) {
                    coverLines.add(startNum);
                    startNum++;
                }
            } else {
                coverLines.add(Integer.valueOf(line));
            }
        }
        return coverLines;
    }


    public static List<String> coverNumMerge(Set<Integer> coverLineSet) {
        List<Integer> coverLines = new ArrayList<>(coverLineSet);
        List<String> merges = new ArrayList<>();
        if (coverLines.size() == 1) {
            merges.add(coverLines.get(0).toString());
            return merges;
        }
        Collections.sort(coverLines, Comparator.comparingInt(o -> o));
        mergeSeriaNum(coverLines, 0, merges);
        return merges;
    }

    public static void mergeSeriaNum(List<Integer> ints, int index, List<String> coverList) {
        int end = index;
        if (ints.size() == index) {
            return;
        } else {
            for (int i = index; i < ints.size(); i++) {
                if (i < ints.size() - 1) {
                    if (ints.get(i) + 1 == ints.get(i + 1)) {
                        end = i;
                    } else {
                        if (i > index)
                            end = end + 1;
                        break;
                    }
                } else {
                    if (end == ints.size() - 2) {
                        end = ints.size() - 1;
                        break;
                    }
                }
            }
            if (index == end) {
                coverList.add(ints.get(index).toString());
                mergeSeriaNum(ints, end + 1, coverList);
            } else {
                coverList.add(ints.get(index) + "-" + ints.get(end));
                mergeSeriaNum(ints, end + 1, coverList);
            }
        }
    }

}
