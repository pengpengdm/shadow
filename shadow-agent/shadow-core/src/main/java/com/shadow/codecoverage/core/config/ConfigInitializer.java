package com.shadow.codecoverage.core.config;

import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.ObjectHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @Classname ConfigInitializer
 * @Description TODO
 * @Date 2023/1/14 12:43
 * @Created by pepsi
 */
public enum ConfigInitializer {

    INST;


    public void initialize(String configFilePath) throws Exception {
        Properties properties = new Properties();
        try {
            File file = new File(configFilePath);
            if (file.exists() && file.isFile()) {
                properties.load(new InputStreamReader(new FileInputStream(file), "UTf-8"));
            }
        } catch (Throwable throwable) {
            //
        }
        process(properties, AgentConfig.class);
        //TODO 可以获取服务端配置到 AgentConfig
    }


    private void process(Properties properties, Class<AgentConfig> configClass) throws IllegalAccessException {
        for (Field field : configClass.getFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                String confieKey = (field.getName()).toLowerCase();
                String value = properties.getProperty(confieKey);
                if (value != null) {
                    Class<?> type = field.getType();
                    if (type.equals(int.class)) {
                        field.set(null, Integer.valueOf(value));
                    } else if (type.equals(String.class)) {
                        field.set(null, value);
                    } else if (type.equals(long.class)) {
                        field.set(null, Long.valueOf(value));
                    } else if (type.equals(boolean.class)) {
                        field.set(null, Boolean.valueOf(value));
                    } else if (type.equals(List.class)) {
                        field.set(null, covert2List(value));
                    }
                }

            }
        }

    }

    private List covert2List(String value) {
        List result = new LinkedList();
        if (StringUtils.isBlank(value)) {
            return result;
        }
        String[] strs = value.split(",");
        for (String val : strs) {
            String trimVal = val.trim();
            if (StringUtils.isNotBlank(trimVal)) {
                result.add(trimVal);
            }
        }
        return result;
    }

    public void fromArgs(String args) {

    }

    public void flushFromRemoteConfig(Map<String, Object> remoteConfigMap) {
        //todo
    }
}
