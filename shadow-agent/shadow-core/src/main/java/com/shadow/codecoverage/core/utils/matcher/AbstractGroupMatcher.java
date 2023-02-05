package com.shadow.codecoverage.core.utils.matcher;


import com.shadow.codecoverage.core.utils.matcher.structure.BehaviorStructure;
import com.shadow.codecoverage.core.utils.matcher.structure.ClassStructure;

import java.util.LinkedHashSet;

/**
 * @Classname AbstractGroupMatcher
 * @Description TODO
 * @Date 2023/1/15 22:04
 * @Created by pepsi
 */
public abstract class AbstractGroupMatcher implements Matcher {
    final Matcher[] matcherArray;

    private AbstractGroupMatcher(final Matcher... matcherArray) {
        this.matcherArray = matcherArray;
    }


    public static final class Or extends AbstractGroupMatcher {

        public Or(Matcher... matcherArray) {
            super(matcherArray);
        }

        @Override
        public MatchingResult matching(final ClassStructure classStructure) {
            final MatchingResult result = new MatchingResult();
            if (null == matcherArray) {
                return result;
            }
            for (final Matcher subMatcher : matcherArray) {
                result.getBehaviorStructures().addAll(subMatcher.matching(classStructure).getBehaviorStructures());
            }
            return result;
        }

    }

    public static final class And extends AbstractGroupMatcher {

        public And(Matcher... matcherArray) {
            super(matcherArray);
        }

        @Override
        public MatchingResult matching(ClassStructure classStructure) {
            boolean isFirst = true;
            final MatchingResult result = new MatchingResult();
            final LinkedHashSet<BehaviorStructure> found = new LinkedHashSet<BehaviorStructure>();
            if (null == matcherArray) {
                return result;
            }
            for (final Matcher subMatcher : matcherArray) {
                final MatchingResult subResult = subMatcher.matching(classStructure);

                // 只要有一次匹配失败，剩下的是取交集运算，所以肯定也没戏，就不用花这个计算了
                if (!subResult.isMatched()) {
                    return result;
                }

                if (isFirst) {
                    found.addAll(subResult.getBehaviorStructures());
                    isFirst = false;
                } else {
                    found.retainAll(subResult.getBehaviorStructures());
                }
            }
            if (!found.isEmpty()) {
                result.getBehaviorStructures().addAll(found);
            }
            return result;
        }

    }
}
