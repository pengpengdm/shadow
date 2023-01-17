package com.shadow.codecoverage.core.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @Classname ServiceManager
 * @Description TODO
 * @Date 2023/1/14 12:49
 * @Created by pepsi
 */
public enum ServiceManager {

    INSTANCE;

    /**
     *
     */
    private Map<Class<? extends BootService>, BootService> bootServiceMap = Collections.emptyMap();

    public void start() throws Exception {

        bootServiceMap = loadBootServices();

        init();

        prepare();

        boot();

        complete();
    }

    private void complete() throws Exception {
        for (BootService service : bootServiceMap.values()) {
            try {
                service.complete();
            } catch (Throwable e) {
                //
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        }
    }

    private void boot() throws Exception {
        for (BootService service : bootServiceMap.values()) {
            try {
                service.boot();
            } catch (Throwable e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    private void prepare() throws Exception {
        for (BootService service : bootServiceMap.values()) {
            try {
                service.prepare();
            } catch (Throwable e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    private void init() throws Exception {
        for (BootService service : bootServiceMap.values()) {
            try {
                service.init();
            } catch (Throwable e) {
                throw new Exception(e.getMessage());
            }
        }
    }


    /**
     * 加载所有的 BootService 实现类
     *
     * @return
     */
    private Map<Class<? extends BootService>, BootService> loadBootServices() {
        Map<Class<? extends BootService>, BootService> bootedServiceMap = new LinkedHashMap<>();
        ServiceLoader<BootService> loaded = ServiceLoader.load(BootService.class, this.getClass().getClassLoader());
        for (BootService bootService : loaded) {
            Class<? extends BootService> bootServiceClass = bootService.getClass();
            bootedServiceMap.put(bootServiceClass, bootService);
        }
        return bootedServiceMap;
    }

    /**
     * 统一获取
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public <T extends BootService> T findService(Class<T> serviceClass) {
        return (T) bootServiceMap.get(serviceClass);
    }

}
