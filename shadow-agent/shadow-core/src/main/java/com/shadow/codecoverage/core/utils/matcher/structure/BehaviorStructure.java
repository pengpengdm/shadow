package com.shadow.codecoverage.core.utils.matcher.structure;

import com.pepsi.core.utils.LazyGet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 行为结构
 * <p>
 * 类的构造方法、普通方法、静态方法，统一称呼为类的行为结构
 * </p>
 * <p>
 * 构造方法是一个非常特殊的方法，在JDK实现中通常以{@code <init>}进行命名。
 * 构造方法没有返回类型声明，但这里为了通用性考虑，构造方法的返回类型结构被约定为声明类的类结构
 * </p>
 *
 * @author luanjia@taobao.com
 */
public class BehaviorStructure extends MemberStructure {

    private final ClassStructure returnTypeClassStructure;
    private final List<ClassStructure> parameterTypeClassStructures;
    private final LazyGet<String> signCodeLazyGet = new LazyGet<String>() {
        @Override
        protected String initialValue() {
            return new StringBuilder(256)
                    .append(getDeclaringClassStructure().getJavaClassName())
                    .append("#")
                    .append(getName())
                    .append("(")
                    .append(StringUtils.join(takeJavaClassNames(getParameterTypeClassStructures()), ","))
                    .append(")")
                    .toString();
        }
    };

    BehaviorStructure(
            final String name,
            final ClassStructure declaringClassStructure,
            final ClassStructure returnTypeClassStructure,
            final List<ClassStructure> parameterTypeClassStructures) {
        super(name, declaringClassStructure);
        this.returnTypeClassStructure = returnTypeClassStructure;
        this.parameterTypeClassStructures = Collections.unmodifiableList(parameterTypeClassStructures);
    }

    /**
     * 获取返回类型类结构
     *
     * @return 返回类型类结构
     */
    public ClassStructure getReturnTypeClassStructure() {
        return returnTypeClassStructure;
    }

    /**
     * 获取参数类型结构集合
     *
     * @return 参数类型结构集合
     */
    public List<ClassStructure> getParameterTypeClassStructures() {
        return parameterTypeClassStructures;
    }

    private Collection<String> takeJavaClassNames(final Collection<ClassStructure> classStructures) {
        final Collection<String> javaClassNames = new ArrayList<String>();
        for (final ClassStructure classStructure : classStructures) {
            javaClassNames.add(classStructure.getJavaClassName());
        }
        return javaClassNames;
    }

    /**
     * 获取行为签名
     * <p>
     * 一个行为的签名在整个ClassLoader中唯一，这样就可以通过签名快速定位行为
     * </p>
     *
     * @return 行为签名
     */
    public String getSignCode() {
        return signCodeLazyGet.get();
    }


    @Override
    public int hashCode() {
        return getSignCode().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BehaviorStructure)
                && getSignCode().equals(((BehaviorStructure) obj).getSignCode());
    }
}
