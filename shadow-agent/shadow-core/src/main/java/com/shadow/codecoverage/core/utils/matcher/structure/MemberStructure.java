package com.shadow.codecoverage.core.utils.matcher.structure;

/**
 * 成员结构
 *
 * @author luanjia@taobao.com
 */
public class MemberStructure {
    private final String name;
    private final ClassStructure declaringClassStructure;

    public MemberStructure(
            final String name,
            final ClassStructure declaringClassStructure) {
        this.name = name;
        this.declaringClassStructure = declaringClassStructure;
    }

    public String getName() {
        return name;
    }

    public ClassStructure getDeclaringClassStructure() {
        return declaringClassStructure;
    }
}
