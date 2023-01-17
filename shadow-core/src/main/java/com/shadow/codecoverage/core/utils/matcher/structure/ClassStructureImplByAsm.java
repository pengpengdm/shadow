package com.shadow.codecoverage.core.utils.matcher.structure;

import com.pepsi.core.utils.AgentUtils;
import com.pepsi.core.utils.LazyGet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM7;

class EmptyClassStructure implements ClassStructure {

    @Override
    public String getJavaClassName() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public ClassStructure getSuperClassStructure() {
        return null;
    }

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return Collections.emptyList();
    }

    @Override
    public LinkedHashSet<ClassStructure> getFamilySuperClassStructures() {
        return new LinkedHashSet<ClassStructure>();
    }

    @Override
    public Set<ClassStructure> getFamilyInterfaceClassStructures() {
        return Collections.emptySet();
    }

    @Override
    public Set<ClassStructure> getFamilyTypeClassStructures() {
        return Collections.emptySet();
    }

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return Collections.emptyList();
    }
}

/**
 * JDK原生类型结构体
 */
class PrimitiveClassStructure extends EmptyClassStructure {

    private final Primitive primitive;

    PrimitiveClassStructure(Primitive primitive) {
        this.primitive = primitive;
    }

    static Primitive mappingPrimitiveByJavaClassName(final String javaClassName) {
        for (final Primitive primitive : Primitive.values()) {
            if (primitive.type.equals(javaClassName)) {
                return primitive;
            }
        }
        return null;
    }

    @Override
    public String getJavaClassName() {
        return primitive.type;
    }

    public enum Primitive {
        BOOLEAN("boolean", boolean.class),
        CHAR("char", char.class),
        BYTE("byte", byte.class),
        INT("int", int.class),
        SHORT("short", short.class),
        LONG("long", long.class),
        FLOAT("float", float.class),
        DOUBLE("double", double.class),
        VOID("void", void.class);

        private final String type;

        Primitive(final String type, final Class<?> clazz) {
            this.type = type;
        }
    }
}

class ArrayClassStructure extends EmptyClassStructure {

    private final ClassStructure elementClassStructure;

    ArrayClassStructure(ClassStructure elementClassStructure) {
        this.elementClassStructure = elementClassStructure;
    }

    @Override
    public String getJavaClassName() {
        return elementClassStructure.getJavaClassName() + "[]";
    }
}

/**
 * 用ASM实现的类结构
 *
 * @author luanjia@taobao.com
 */
public class ClassStructureImplByAsm implements ClassStructure {

    private final Logger logger = LoggerFactory.getLogger(ClassStructureImplByAsm.class);
    private final ClassReader classReader;
    private final ClassLoader loader;
    private final LazyGet<ClassStructure> superClassStructureLazyGet = new LazyGet<ClassStructure>() {
        @Override
        protected ClassStructure initialValue() {
            final String superInternalClassName = classReader.getSuperName();
            if ("java/lang/Object".equals(superInternalClassName)) {
                return null;
            }
            return newInstance(AgentUtils.toJavaClassName(superInternalClassName));
        }
    };
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
    private final LazyGet<List<ClassStructure>> interfaceClassStructuresLazyGet
            = new LazyGet<List<ClassStructure>>() {
        @Override
        protected List<ClassStructure> initialValue() {
            return newInstances(classReader.getInterfaces());
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
//    private final static GaLRUCache<Pair, ClassStructure> classStructureCache
//            = new GaLRUCache<Pair, ClassStructure>(1024);
    private final LazyGet<List<BehaviorStructure>> behaviorStructuresLazyGet
            = new LazyGet<List<BehaviorStructure>>() {
        @Override
        protected List<BehaviorStructure> initialValue() {
            final List<BehaviorStructure> behaviorStructures = new ArrayList<>();
            classReader.accept(new ClassVisitor(ASM7) {

                @Override
                public MethodVisitor visitMethod(final int access,
                                                 final String name,
                                                 final String desc,
                                                 final String signature,
                                                 final String[] exceptions) {

                    // 静态方法
                    if (StringUtils.equals("<clinit>", name)) {
                        return super.visitMethod(access, name, desc, signature, exceptions);
                    }

                    return new MethodVisitor(ASM7, super.visitMethod(access, name, desc, signature, exceptions)) {

                        private final Type methodType = Type.getMethodType(desc);

                        private String[] typeArrayToJavaClassNameArray(final Type[] typeArray) {
                            final List<String> javaClassNames = new ArrayList<String>();
                            if (null != typeArray) {
                                for (Type type : typeArray) {
                                    javaClassNames.add(type.getClassName());
                                }
                            }
                            return javaClassNames.toArray(new String[0]);
                        }

                        private List<ClassStructure> getParameterTypeClassStructures() {
                            return newInstances(
                                    typeArrayToJavaClassNameArray(methodType.getArgumentTypes())
                            );
                        }

                        private ClassStructure getReturnTypeClassStructure() {
                            if ("<init>".equals(name)) {
                                return ClassStructureImplByAsm.this;
                            } else {
                                final Type returnType = methodType.getReturnType();
                                return newInstance(returnType.getClassName());
                            }
                        }

                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            final BehaviorStructure behaviorStructure = new BehaviorStructure(
                                    name,
                                    ClassStructureImplByAsm.this,
                                    getReturnTypeClassStructure(),
                                    getParameterTypeClassStructures()
                            );
                            behaviorStructures.add(behaviorStructure);
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return behaviorStructures;
        }
    };

    public ClassStructureImplByAsm(final InputStream classInputStream,
                                   final ClassLoader loader) throws IOException {
        this(IOUtils.toByteArray(classInputStream), loader);
    }

    public ClassStructureImplByAsm(final byte[] classByteArray,
                                   final ClassLoader loader) {
        this.classReader = new ClassReader(classByteArray);
        this.loader = loader;
    }

    private boolean isBootstrapClassLoader() {
        return null == loader;
    }

    // 获取资源数据流
    // 一般而言可以从loader直接获取，如果获取不到那么这个类也会能加载成功
    // 但如果遇到来自BootstrapClassLoader的类就必须从java.lang.Object来获取，但是该方法仅限于jdk8
    // 对于jdk >= 9的版本来说，需要先获取到相关类，然后通过这个类获取自己的resource
    private InputStream getResourceAsStream(final String javaClassName) {

        return isBootstrapClassLoader()
                ? Object.class.getResourceAsStream("/" + javaClassName)
                : loader.getResourceAsStream(javaClassName);
    }

    // 将内部类名称转换为资源名称
    private String internalClassNameToResourceName(final String internalClassName) {
        return internalClassName + ".class";
    }

    // 构造一个类结构实例
    private ClassStructure newInstance(final String javaClassName) {

        // 空载保护
        if (null == javaClassName) {
            return null;
        }

        // 是个数组类型
        if (javaClassName.endsWith("[]")) {
            return new ArrayClassStructure(newInstance(javaClassName.substring(0, javaClassName.length() - 2)));
        }

        // 是个基本类型
        final PrimitiveClassStructure.Primitive primitive = PrimitiveClassStructure.mappingPrimitiveByJavaClassName(javaClassName);
        if (null != primitive) {
            return new PrimitiveClassStructure(primitive);
        }
        // fix for #385
        final InputStream is = getResourceAsStream(internalClassNameToResourceName(AgentUtils.toInternalClassName(javaClassName)));
        if (null != is) {
            try {
                return new ClassStructureImplByAsm(is, loader);
            } catch (Throwable cause) {
                // ignore
                logger.warn("new instance class structure by using ASM failed, will return null. class={};loader={};",
                        javaClassName, loader, cause);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        // 出现异常或者找不到
        return null;
    }

    // 构造一个类结构实例数组
    private List<ClassStructure> newInstances(final String[] javaClassNameArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null == javaClassNameArray) {
            return classStructures;
        }
        for (final String javaClassName : javaClassNameArray) {
            final ClassStructure classStructure = newInstance(javaClassName);
            if (null != classStructure) {
                classStructures.add(classStructure);
            }
        }
        return classStructures;
    }

    @Override
    public String getJavaClassName() {
        return AgentUtils.toJavaClassName(classReader.getClassName());
    }

    @Override
    public ClassLoader getClassLoader() {
        return loader;
    }

    @Override
    public ClassStructure getSuperClassStructure() {
        return superClassStructureLazyGet.get();
    }

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return interfaceClassStructuresLazyGet.get();
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

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return behaviorStructuresLazyGet.get();
    }

    @Override
    public String toString() {
        return "ClassStructureImplByAsm{" +
                "javaClassName='" + getJavaClassName() + '\'' +
                '}';
    }
}
