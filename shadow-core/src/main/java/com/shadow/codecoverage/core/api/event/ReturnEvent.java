package com.shadow.codecoverage.core.api.event;

import java.util.BitSet;

/**
 * @Classname ReturnRevent
 * @Description TODO
 * @Date 2023/1/14 14:01
 * @Created by pepsi
 */
public class ReturnEvent extends Event {

    public final Object object;

    public Integer methodId;

    public BitSet coverLines;

    public ReturnEvent(Object object) {
        super(Type.RETURN);
        this.object = object;
    }

    public ReturnEvent(Object object, Integer methodId, BitSet coverLines) {
        super(Type.RETURN);
        this.object = object;
        this.methodId = methodId;
        this.coverLines = coverLines;
    }
}
