package com.shadow.codecoverage.core.utils.matcher.builder;

import com.pepsi.core.api.EnhanceEventWatcher;
import com.pepsi.core.api.EventListener;
import com.pepsi.core.utils.matcher.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname EventWatchBuilder
 * @Description TODO
 * @Date 2023/1/15 10:11
 * @Created by pepsi
 */
public class EventWatchBuilder {


    private final EnhanceEventWatcher eventWatcher;

    private final List<BuildingForClass> bfClasses = new ArrayList<>();

    /**
     * 构造事件观察者构造器
     */
    public EventWatchBuilder(final EnhanceEventWatcher eventWatcher) {
        this.eventWatcher = eventWatcher;
    }

    /**
     * 通配符表达式匹配
     *
     * @param string   目标字符串
     * @param wildcard 通配符匹配模版
     * @return true:目标字符串符合匹配模版;false:目标字符串不符合匹配模版
     */
    public static boolean matching(final String string, final String wildcard) {
        return null != wildcard
                && null != string
                && matching(string, wildcard, 0, 0);
    }

    /**
     * Internal matching recursive function.
     */
    private static boolean matching(String string, String wildcard, int stringStartNdx, int patternStartNdx) {
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = wildcard.length();
        if (pLen == 1) {
            if (wildcard.charAt(0) == '*') {     // speed-up
                return true;
            }
        }
        int sLen = string.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // check if end of string and/or pattern occurred
            if ((sNdx >= sLen)) {   // end of string still may have pending '*' callback pattern
                while ((pNdx < pLen) && (wildcard.charAt(pNdx) == '*')) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            if (pNdx >= pLen) {         // end of pattern, but not end of the string
                return false;
            }
            char p = wildcard.charAt(pNdx);    // pattern char

            // perform logic
            if (!nextIsNotWildcard) {

                if (p == '\\') {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == '?') {
                    sNdx++;
                    pNdx++;
                    continue;
                }
                if (p == '*') {
                    char pnext = 0;           // next pattern char
                    if (pNdx + 1 < pLen) {
                        pnext = wildcard.charAt(pNdx + 1);
                    }
                    if (pnext == '*') {         // double '*' have the same effect as one '*'
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = string.length(); i >= sNdx; i--) {
                        if (matching(string, wildcard, i, pNdx)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            // check if pattern char and string char are equals
            if (p != string.charAt(sNdx)) {
                return false;
            }

            // everything matches for now, continue
            sNdx++;
            pNdx++;
        }
    }

    /**
     * 模版匹配类名称(包含包名)
     *
     * @param pattern 类名匹配模版
     * @return IBuildingForClass
     */
    public IBuildingForClass onClass(final String pattern) {
        BuildingForClass buildingForClass = new BuildingForClass(pattern);
        bfClasses.add(buildingForClass);
        return buildingForClass;
    }

    public List<BuildingForClass> getBfClasses() {
        return bfClasses;
    }

    private EventWatchCondition toEventWatchCondition() {
        final List<Filter> filters = new ArrayList<Filter>();
        for (final BuildingForClass bfClass : bfClasses) {
            final Filter filter = new Filter() {
                @Override
                public boolean doClassFilter(final String javaClassName) {
                    return matching(javaClassName, bfClass.pattern);
                }

                @Override
                public boolean doMethodFilter(final String javaMethodName,
                                              final String[] parameterTypeJavaClassNameArray) {
                    // nothing to matching
                    if (bfClass.bfBehaviors.isEmpty()) {
                        return false;
                    }

                    // matching any behavior
                    for (final BuildingForBehavior bfBehavior : bfClass.bfBehaviors) {
                        if (matching(javaMethodName, bfBehavior.pattern)
                                && bfBehavior.withParameterTypes.patternWith(parameterTypeJavaClassNameArray)) {
                            return true;
                        }//if
                    }
                    return false;
                }

                @Override
                public boolean isIncludeSubClasses() {
                    return bfClass.isIncludeSubClasses;
                }
            };//filter

            filters.add(filter);
        }
        return () -> filters.toArray(new Filter[0]);
    }

    /**
     * 构建类匹配器
     */
    public interface IBuildingForClass {

        /**
         * 类修饰匹配
         */
        IBuildingForClass withAccess(int access);

        /**
         * 构建行为匹配器，匹配任意行为
         *
         * @return IBuildingForBehavior
         */
        IBuildingForBehavior onAnyBehavior();

        /**
         * 构建行为匹配器，匹配符合模版匹配名称的行为
         *
         * @param pattern 行为名称
         * @return IBuildingForBehavior
         */
        IBuildingForBehavior onBehavior(String pattern);

        /**
         * {@link #onClass}所指定的类，检索路径是否包含子类（实现类）
         *
         * @return IBuildingForClass
         */
        IBuildingForClass includeSubClasses();

    }

    /**
     * 构建方法匹配器
     */
    public interface IBuildingForBehavior {

        IBuildingForBehavior withAccess(int access);

        IBuildingForBehavior withEmptyParameterTypes();

        IBuildingForBehavior withParameterTypes(String... patterns);

        IBuildingForBehavior onBehavior(String pattern);

        IBuildingForClass onClass(String pattern);

        /**
         * 插件级别
         *
         * @param eventListener
         */
        void onWatchPluginBehavior(EventListener eventListener);

        /**
         * 行级别
         *
         * @param eventListener
         */
        void onWatchBizBehavior(EventListener eventListener);
    }

    /**
     * 类匹配器实现
     */
    private class BuildingForClass implements IBuildingForClass {

        private final String pattern;
        private final List<BuildingForBehavior> bfBehaviors = new ArrayList<BuildingForBehavior>();
        private int withAccess = 0;
        private boolean isIncludeSubClasses = false;

        /**
         * 构造类构建器
         */
        BuildingForClass(final String pattern) {
            this.pattern = pattern;
        }


        @Override
        public IBuildingForClass includeSubClasses() {
            this.isIncludeSubClasses = true;
            return this;
        }

        @Override
        public IBuildingForClass withAccess(final int access) {
            withAccess |= access;
            return this;
        }

        @Override
        public IBuildingForBehavior onBehavior(final String pattern) {
            BuildingForBehavior behavior = new BuildingForBehavior(this, pattern);
            bfBehaviors.add(behavior);
            return behavior;
        }

        @Override
        public IBuildingForBehavior onAnyBehavior() {
            return onBehavior("*");
        }

        public String getPattern() {
            return pattern;
        }
    }

    private class BuildingForBehavior implements IBuildingForBehavior {

        private final BuildingForClass bfClass;
        private final String pattern;
        private final PatternGroupList withParameterTypes = new PatternGroupList();
        private int withAccess = 0;

        BuildingForBehavior(final BuildingForClass bfClass,
                            final String pattern) {
            this.bfClass = bfClass;
            this.pattern = pattern;
        }

        @Override
        public IBuildingForBehavior withAccess(final int access) {
            withAccess |= access;
            return this;
        }

        @Override
        public IBuildingForBehavior withEmptyParameterTypes() {
            withParameterTypes.add();
            return this;
        }

        @Override
        public IBuildingForBehavior withParameterTypes(final String... patterns) {
            withParameterTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForBehavior onBehavior(final String pattern) {
            return bfClass.onBehavior(pattern);
        }

        @Override
        public IBuildingForClass onClass(final String pattern) {
            return EventWatchBuilder.this.onClass(pattern);
        }


        @Override
        public void onWatchPluginBehavior(EventListener eventListener) {
            eventWatcher.watchPluginBehavior(toEventWatchCondition(), eventListener);
        }

        @Override
        public void onWatchBizBehavior(EventListener eventListener) {
            eventWatcher.watchBizBehavior(eventListener);
        }
    }

    /**
     * 模式匹配组列表
     */
    private class PatternGroupList {

        final List<Group> groups = new ArrayList<Group>();

        /*
         * 添加模式匹配组
         */
        void add(String... patternArray) {
            groups.add(new Group(patternArray));
        }

        /*
         * 是否为空
         */
        boolean isEmpty() {
            return groups.isEmpty();
        }

        /*
         * 模式匹配With
         */
        boolean patternWith(final String[] stringArray) {

            // 如果模式匹配组为空，说明不参与本次匹配
            if (groups.isEmpty()) {
                return true;
            }

            for (final Group group : groups) {
                if (group.matchingWith(stringArray)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * 模式匹配Has
         */
        boolean patternHas(final String[] stringArray) {

            // 如果模式匹配组为空，说明不参与本次匹配
            if (groups.isEmpty()) {
                return true;
            }

            for (final Group group : groups) {
                if (group.matchingHas(stringArray)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * 模式匹配组
     */
    private class Group {

        final String[] patternArray;

        Group(String[] patternArray) {
            this.patternArray = (patternArray == null || patternArray.length == 0)
                    ? new String[0]
                    : patternArray;
        }

        /*
         * stringArray中任意字符串能匹配上匹配模式
         */
        boolean anyMatching(final String[] stringArray,
                            final String pattern) {
            if (stringArray.length == 0) {
                return false;
            }
            for (final String string : stringArray) {
                if (matching(string, pattern)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * 匹配模式组中所有匹配模式都在目标中存在匹配通过的元素
         * 要求匹配组中每一个匹配项都在stringArray中存在匹配的字符串
         */
        boolean matchingHas(final String[] stringArray) {

            for (final String pattern : patternArray) {
                if (anyMatching(stringArray, pattern)) {
                    continue;
                }
                return false;
            }
            return true;
        }

        //
        boolean matchingWith(final String[] stringArray) {

            // 长度不一样就不用不配了
            int length;
            if ((length = stringArray.length) != patternArray.length) {
                return false;
            }
            // 长度相同则逐个位置比较，只要有一个位置不符，则判定不通过
            for (int index = 0; index < length; index++) {
                if (!matching(stringArray[index], patternArray[index])) {
                    return false;
                }
            }
            // 所有位置匹配通过，判定匹配成功
            return true;
        }
    }
}
