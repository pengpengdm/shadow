package com.shadow.codecoverage.core.dto;

/**
 * @Classname MethodInformation
 * @Description TODO
 * @Date 2023/2/4 19:31
 * @Created by pepsi
 */
public class MethodInformation {
    private int classId;

    private int access;

    private String className;

    private String jarName;

    private String name;

    private String desc;

    private int startLineNum;

    private int endLineNum;

    private String mkey;


    public MethodInformation(int classId, int access, String className, String jarName, String name, String desc, int startLineNum, int endLineNum, String mkey) {
        this.classId = classId;
        this.access = access;
        this.className = className;
        this.jarName = jarName;
        this.name = name;
        this.desc = desc;
        this.startLineNum = startLineNum;
        this.endLineNum = endLineNum;
        this.mkey = mkey;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStartLineNum() {
        return startLineNum;
    }

    public void setStartLineNum(int startLineNum) {
        this.startLineNum = startLineNum;
    }

    public int getEndLineNum() {
        return endLineNum;
    }

    public void setEndLineNum(int endLineNum) {
        this.endLineNum = endLineNum;
    }

    public String getMkey() {
        return mkey;
    }

    public void setMkey(String mkey) {
        this.mkey = mkey;
    }
}
