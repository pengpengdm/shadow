package com.shadow.codecoverage.core.utils.matcher.structure;


import com.shadow.codecoverage.core.utils.LazyGet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 用JDK的反射实现的类结构
 */
public class ClassStructureImplByJDK implements ClassStructure {

    private final Class<?> clazz;
    private final LazyGet<LinkedHashSet<ClassStructure>> familySuperClassStructuresLazyGet
            = new LazyGet<LinkedHashSet<ClassStructure>>() {
        @Override
        protected LinkedHashSet<ClassStructure> initialValue() {
            final LinkedHashSet<ClassStructure> familySuperClassStructures = new LinkedHashSet<ClassStructure>();
            final ClassStructure superClassStructure = getSuperClassStructure();
            if (null != superClassStructure) {
                // 1. 先加自己的父类
                familySuperClassStructures.add(superClassStructure);
                // 2. 再加父类的祖先
                familySuperClassStructures.addAll(superClassStructure.getFamilySuperClassStructures());
            }
            return familySuperClassStructures;
        }
    };
    private final LazyGet<Set<ClassStructure>> familyInterfaceClassStructuresLazyGet
            = new LazyGet<Set<ClassStructure>>() {
        @Override
        protected Set<ClassStructure> initialValue() {
            final Set<ClassStructure> familyInterfaceClassStructures = new HashSet<ClassStructure>();
            for (final ClassStructure interfaceClassStructure : getInterfaceClassStructures()) {
                // 1. 先加自己声明的接口
                familyInterfaceClassStructures.add(interfaceClassStructure);
                // 2. 再加接口所声明的祖先(接口继承)
                familyInterfaceClassStructures.addAll(interfaceClassStructure.getFamilyInterfaceClassStructures());
            }

            // BUGFIX: 修复获取家族接口类结构时忘记考虑自身父类的情况
            // AUTHOR: oldmanpushcart@gmail.com
            for (final ClassStructure superClassStructure : getFamilySuperClassStructures()) {
                familyInterfaceClassStructures.addAll(superClassStructure.getFamilyInterfaceClassStructures());
            }

            return familyInterfaceClassStructures;
        }
    };
    private final LazyGet<Set<ClassStructure>> familyTypeClassStructuresLazyGet
            = new LazyGet<Set<ClassStructure>>() {
        @Override
        protected Set<ClassStructure> initialValue() {
            final Set<ClassStructure> familyClassStructures = new LinkedHashSet<ClassStructure>();

            // 注入家族类&家族类所声明的家族接口
            for (final ClassStructure familySuperClassStructure : getFamilySuperClassStructures()) {
                familyClassStructures.add(familySuperClassStructure);
                familyClassStructures.addAll(familySuperClassStructure.getFamilyInterfaceClassStructures());
            }

            // 注入家族接口
            for (final ClassStructure familyInterfaceClassStructure : getFamilyInterfaceClassStructures()) {
                familyClassStructures.add(familyInterfaceClassStructure);
                familyClassStructures.addAll(familyInterfaceClassStructure.getFamilyInterfaceClassStructures());
            }
            return familyClassStructures;
        }
    };
    private final LazyGet<List<BehaviorStructure>> behaviorStructuresLazyGet
            = new LazyGet<List<BehaviorStructure>>() {
        @Override
        protected List<BehaviorStructure> initialValue() {
            final List<BehaviorStructure> behaviorStructures = new ArrayList<BehaviorStructure>();
            for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                behaviorStructures.add(newBehaviorStructure(constructor));
            }
            for (final Method method : clazz.getDeclaredMethods()) {
                behaviorStructures.add(newBehaviorStructure(method));
            }
            return Collections.unmodifiableList(behaviorStructures);
        }
    };
    private String javaClassName;

    public ClassStructureImplByJDK(final Class<?> clazz) {
        this.clazz = clazz;
    }

    private ClassStructure newInstance(final Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return new ClassStructureImplByJDK(clazz);
    }

    private List<ClassStructure> newInstances(final Class[] classArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null != classArray) {
            for (final Class<?> clazz : classArray) {
                final ClassStructure classStructure = newInstance(clazz);
                if (null != classStructure) {
                    classStructures.add(classStructure);
                }
            }
        }
        return classStructures;
    }

    @Override
    public String getJavaClassName() {
        return null != javaClassName
                ? javaClassName
                : (javaClassName = getJavaClassName(clazz));
    }

    private String getJavaClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    @Override
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    @Override
    public ClassStructure getSuperClassStructure() {
        // 过滤掉Object.class
        return Object.class.equals(clazz.getSuperclass())
                ? null
                : newInstance(clazz.getSuperclass());
    }

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return newInstances(clazz.getInterfaces());
    }

    @Override
    public LinkedHashSet<ClassStructure> getFamilySuperClassStructures() {
        return familySuperClassStructuresLazyGet.get();
    }

    @Override
    public Set<ClassStructure> getFamilyInterfaceClassStructures() {
        return familyInterfaceClassStructuresLazyGet.get();
    }

    @Override
    public Set<ClassStructure> getFamilyTypeClassStructures() {
        return familyTypeClassStructuresLazyGet.get();
    }

    private Class[] getAnnotationTypeArray(final Annotation[] annotationArray) {
        final Collection<Class> annotationTypes = new ArrayList<Class>();
        for (final Annotation annotation : annotationArray) {
            if (annotation.getClass().isAnnotation()) {
                annotationTypes.add(annotation.getClass());
            }
            for (final Class annotationInterfaceClass : annotation.getClass().getInterfaces()) {
                if (annotationInterfaceClass.isAnnotation()) {
                    annotationTypes.add(annotationInterfaceClass);
                }
            }
        }
        return annotationTypes.toArray(new Class[0]);
    }

    private BehaviorStructure newBehaviorStructure(final Method method) {
        return new BehaviorStructure(
                method.getName(),
                this,
                newInstance(method.getReturnType()),
                newInstances(method.getParameterTypes()));
    }

    private BehaviorStructure newBehaviorStructure(final Constructor constructor) {
        return new BehaviorStructure(
                "<init>",
                this,
                this,
                newInstances(constructor.getParameterTypes()));
    }

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return behaviorStructuresLazyGet.get();
    }

    @Override
    public String toString() {
        return "ClassStructureImplByJDK{" + "javaClassName='" + javaClassName + '\'' + '}';
    }
}
