package com.shadow.codecoverage.core.utils.matcher;

import com.shadow.codecoverage.core.utils.matcher.structure.ClassStructure;

/**
 * @Classname Matcher
 * @Description TODO
 * @Date 2023/1/15 10:00
 * @Created by pepsi
 */
public interface Matcher {


    /**
     * 匹配类结构
     *
     * @param classStructure 类结构
     * @return 匹配结果
     */
    MatchingResult matching(ClassStructure classStructure);

}
