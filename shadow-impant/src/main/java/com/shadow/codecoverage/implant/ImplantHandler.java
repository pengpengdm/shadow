package com.shadow.codecoverage.implant;

import java.util.BitSet;

/**
 * @Classname ImplantHandler
 * @Description TODO
 * @Date 2023/1/15 22:14
 * @Created by pepsi
 */
public interface ImplantHandler {
    /**
     * 插件类调用方法before
     *
     * @param listenerId 事件监听器ID
     */
    void handleOnPluginMethodBefore(int listenerId, Object[] args, String javaClassName, String javaMethodName, String methodDesc, Object target) throws Throwable;

    /**
     * 插件类调用方法return
     *
     * @param listenerId 事件监听器ID
     * @throws Throwable 处理{调用方法:正常返回}失败
     */
    void handleOnPluginMethodReturn(int listenerId) throws Throwable;

    /**
     * 处理方法调用:调用之前
     * <p>BEFORE</p>
     *
     * @param listenerId 事件监听器ID
     * @param methodId   methodInf
     * @throws Throwable 处理{方法调用:调用之前}失败
     */
    void handleOnBefore(int listenerId, int methodId) throws Throwable;

    /**
     * 记录 coverLines
     *
     * @param listenerId 事件监听器ID
     * @param coverLines cc
     * @throws Throwable 处理{方法调用:正常返回}失败
     */
    void recordMethodCoverLines(int listenerId, int methodId, BitSet coverLines) throws Throwable;


}
