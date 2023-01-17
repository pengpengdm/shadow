package com.shadow.codecoverage.core.service;

/**
 * @Classname BootService
 * @Description TODO
 * @Date 2023/1/14 12:45
 * @Created by pepsi
 */
public interface BootService {

    void init() throws Throwable;


    void prepare() throws Throwable;

    void boot() throws Throwable;

    void complete() throws Throwable;

    void shutdown() throws Throwable;

}
