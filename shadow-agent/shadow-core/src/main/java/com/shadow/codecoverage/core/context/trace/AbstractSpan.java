package com.shadow.codecoverage.core.context.trace;

import java.util.Set;

/**
 * @Classname AbstractSpan
 * @Description TODO
 * @Date 2023/2/3 23:16
 * @Created by pepsi
 */
public abstract class AbstractSpan {

    private int spanId;

    private int parentSpanId;

    private String className;

    private String methodSign;

    private String methodName;

    private int startLineNum;

    private int access;

    private Set<Integer> coverLines;

    private int methodId;

    /**
     * @param spanId
     * @param parentSpanId
     */
    protected AbstractSpan(int spanId, int parentSpanId) {
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    public boolean finish(TraceSegment owner) {
        owner.archive(this);
        return true;
    }

    public AbstractSpan start() {
        return this;
    }

    public String genClassMethodSign() {
        String realClassName = className;
        if (realClassName != null && realClassName.contains("$")) {
            realClassName = realClassName.substring(0, realClassName.indexOf("$"));
        }
        return realClassName + "." + this.methodName + ";" + this.access + ";" + this.methodSign + ";" + this.startLineNum;
    }

    /**
     * 判断是否是入口方法
     *
     * @return
     */
    public abstract boolean isEntry();

    public int getSpanId() {
        return spanId;
    }

    public int getParentSpanId() {
        return parentSpanId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public void setMethodSign(String methodSign) {
        this.methodSign = methodSign;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getStartLineNum() {
        return startLineNum;
    }

    public void setStartLineNum(int startLineNum) {
        this.startLineNum = startLineNum;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public Set<Integer> getCoverLines() {
        return coverLines;
    }

    public void setCoverLines(Set<Integer> coverLines) {
        this.coverLines = coverLines;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("spanId=").append(spanId)
                .append(",methodId=").append(methodId)
                .append(",coverLines=").append(coverLines)
                .append(",className=").append(className)
                .append(",methodName=").append(methodName);
        return builder.toString();
    }

    /**
     * 创建每个方法唯一签名
     * @return
     */
    public String getClassMethodSing() {
        return className +"."+this.methodName+";"+this.access+";"+this.methodSign+";"+this.startLineNum;
    }
}
