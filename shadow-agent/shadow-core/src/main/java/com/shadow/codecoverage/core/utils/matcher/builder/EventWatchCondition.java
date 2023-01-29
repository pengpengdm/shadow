package com.shadow.codecoverage.core.utils.matcher.builder;

import com.pepsi.core.utils.matcher.Filter;

/**
 * @Classname EventWatchCondition
 * @Description TODO
 * @Date 2023/1/15 10:11
 * @Created by pepsi
 */
public interface EventWatchCondition {


    /**
     * 获取 or 的 filters
     *
     * @return
     */
    Filter[] getOrFilterArray();

}
