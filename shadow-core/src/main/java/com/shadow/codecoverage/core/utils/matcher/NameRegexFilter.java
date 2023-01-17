package com.shadow.codecoverage.core.utils.matcher;

/**
 * @Classname NameRegexFilter
 * @Description TODO
 * @Date 2023/1/15 21:54
 * @Created by pepsi
 */
public class NameRegexFilter implements Filter {

    // 类名正则表达式
    private final String javaNameRegex;

    // 方法名正则表达式
    private final String javaMethodRegex;

    /**
     * 构造名称正则表达式过滤器
     *
     * @param javaNameRegex   类名正则表达式
     * @param javaMethodRegex 方法名正则表达式
     */
    public NameRegexFilter(String javaNameRegex, String javaMethodRegex) {
        this.javaNameRegex = javaNameRegex;
        this.javaMethodRegex = javaMethodRegex;
    }

    @Override
    public boolean doClassFilter(final String javaClassName) {
        return javaClassName.matches(javaNameRegex);
    }

    @Override
    public boolean doMethodFilter(final String javaMethodName,
                                  final String[] parameterTypeJavaClassNameArray) {
        return javaMethodName.matches(javaMethodRegex);
    }

    @Override
    public boolean isIncludeSubClasses() {
        return false;
    }
}
