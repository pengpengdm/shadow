package com.shadow.codecoverage.core.utils.matcher;

/**
 * @Classname Filter
 * @Description TODO
 * @Date 2023/1/15 10:01
 * @Created by pepsi
 */
public interface Filter {


    boolean doClassFilter(String javaClassName);


    boolean doMethodFilter(String javaMethodName,
                           String[] parameterTypeJavaClassNameArray);


    /**
     * 是否含子类、实现类
     *
     * @return
     */
    boolean isIncludeSubClasses();


    /**
     * 增强过滤器工厂类
     */
    class ExtFilterFactory {

        /**
         * 生产增强过滤器
         *
         * @param filter 原生过滤器
         * @return 增强过滤器
         */
        public static Filter make(final Filter filter) {

            return new Filter() {

                @Override
                public boolean isIncludeSubClasses() {
                    return filter.isIncludeSubClasses();
                }


                @Override
                public boolean doClassFilter(final String javaClassName) {
                    return filter.doClassFilter(javaClassName);
                }

                @Override
                public boolean doMethodFilter(final String javaMethodName,
                                              final String[] parameterTypeJavaClassNameArray) {
                    return filter.doMethodFilter(javaMethodName,
                            parameterTypeJavaClassNameArray
                    );
                }
            };
        }

    }
}
