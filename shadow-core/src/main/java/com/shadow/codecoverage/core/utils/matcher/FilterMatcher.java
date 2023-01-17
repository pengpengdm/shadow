package com.shadow.codecoverage.core.utils.matcher;

import com.pepsi.core.utils.matcher.structure.BehaviorStructure;
import com.pepsi.core.utils.matcher.structure.ClassStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Classname FilterMatch
 * @Description TODO
 * @Date 2023/1/15 10:08
 * @Created by pepsi
 */
public class FilterMatcher implements Matcher {

    private final Filter filter;

    public FilterMatcher(final Filter filter) {
        this.filter = filter;
    }

    /**
     * @param filterArray 过滤器数组
     * @return 兼容的Matcher
     */
    public static Matcher toOrGroupMatcher(final Filter[] filterArray) {
        final Matcher[] matchers = new Matcher[filterArray.length];
        for (int index = 0; index < filterArray.length; index++) {
            matchers[index] = new FilterMatcher(Filter.ExtFilterFactory.make(filterArray[index]));
        }
        return new AbstractGroupMatcher.Or(matchers);
    }

    // 获取需要匹配的类结构
    // 如果要匹配子类就需要将这个类的所有家族成员找出
    private Collection<ClassStructure> getWaitingMatchClassStructures(final ClassStructure classStructure) {
        final Collection<ClassStructure> waitingMatchClassStructures = new ArrayList<ClassStructure>();
        waitingMatchClassStructures.add(classStructure);
        if (filter.isIncludeSubClasses()) {
            waitingMatchClassStructures.addAll(classStructure.getFamilyTypeClassStructures());
        }
        return waitingMatchClassStructures;
    }

    private String[] toJavaClassNameArray(final Collection<ClassStructure> classStructures) {
        if (null == classStructures) {
            return null;
        }
        final List<String> javaClassNames = new ArrayList<String>();
        for (final ClassStructure classStructure : classStructures) {
            javaClassNames.add(classStructure.getJavaClassName());
        }
        return javaClassNames.toArray(new String[0]);
    }

    private boolean matchingClassStructure(ClassStructure classStructure) {
        for (final ClassStructure wmCs : getWaitingMatchClassStructures(classStructure)) {

            // 匹配类结构
            if (filter.doClassFilter(wmCs.getJavaClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MatchingResult matching(final ClassStructure classStructure) {

        try {
            return _matching(classStructure);
        } catch (NoClassDefFoundError error) {
            // 其他情况就直接抛出error
            throw error;
        }

    }

    private MatchingResult _matching(final ClassStructure classStructure) {
        final MatchingResult result = new MatchingResult();
        // 加载Bootstrap的类，遇到就过滤掉
        if (classStructure.getClassLoader() == null) {
            return result;
        }

        // 匹配ClassStructure
        if (!matchingClassStructure(classStructure)) {
            return result;
        }

        // 匹配BehaviorStructure
        for (final BehaviorStructure behaviorStructure : classStructure.getBehaviorStructures()) {
            if (filter.doMethodFilter(
                    behaviorStructure.getName(),
                    toJavaClassNameArray(behaviorStructure.getParameterTypeClassStructures()))) {
                result.getBehaviorStructures().add(behaviorStructure);
            }
        }
        return result;
    }


}
