package com.shadow.codecoverage.core.api.event;

/**
 * @Classname BeforeEvent
 * @Description TODO
 * @Date 2023/1/14 14:01
 * @Created by pepsi
 */
public class BeforeEvent extends Event {

    public final String javaClassName;

    public final String javaMethodName;

    public final String javaMethodDesc;

    public final Object target;

    public final Object[] args;

    public final Integer methodId;


    public BeforeEvent(String javaClassName,
                       String javaMethodName,
                       String javaMethodDesc,
                       Object target,
                       Object[] args,
                       Integer methodId) {
        super(Type.BEFORE);
        this.javaClassName = javaClassName;
        this.javaMethodName = javaMethodName;
        this.javaMethodDesc = javaMethodDesc;
        this.target = target;
        this.args = args;
        this.methodId = methodId;
    }

}
