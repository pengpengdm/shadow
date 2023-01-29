package com.shadow.codecoverage.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * @Classname JavaAgent
 * @Description TODO
 * @Date 2023/1/14 10:57
 * @Created by pepsi
 */
public class JavaAgent {


    private static final String LUANCHER_CLASS = "";

    private static final String CORE_LAUNCHER_CLASS = "";


    private static final String AGENT_HOME = new File(JavaAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParent();

    public static void premain(String[] args, Instrumentation instr) {
        try {
            instr.appendToBootstrapClassLoaderSearch(new JarFile(new File(getImplantJarPath(AGENT_HOME))));

            System.setProperty("agent.home", AGENT_HOME);

            final ClassLoader agentClassLoader = defineClassLoader(getCoreJarPath(AGENT_HOME));

            final Class<?> coreLauncherClass = agentClassLoader.loadClass(CORE_LAUNCHER_CLASS);

            final Object coreLauncherInstance = coreLauncherClass.getMethod("newInstance", String.class)
                    .invoke(null, args, instr);

            coreLauncherClass.getMethod("launch", String.class, Instrumentation.class)
                    .invoke(coreLauncherInstance, args, instr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ClassLoader defineClassLoader(String coreJarPath) throws Exception {
        return new ShadowAgentClassLoader(coreJarPath);
    }

    private static String getCoreJarPath(String agentHome) {
        return agentHome + File.separator + "lib" + File.separator + "agent-core.jar";
    }

    private static String getImplantJarPath(String agentHome) {
        return agentHome + File.separator + "lib" + File.separator + "agent-implant.jar";
    }


}
