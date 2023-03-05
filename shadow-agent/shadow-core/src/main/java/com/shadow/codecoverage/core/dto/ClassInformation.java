package com.shadow.codecoverage.core.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * @Classname ClassInformation
 * @Description TODO
 * @Date 2023/2/4 19:34
 * @Created by pepsi
 */
public class ClassInformation {

    private String name;

    private String jarName;

    private Set<Integer> methodIds;

    private Integer classId;

    public ClassInformation(String name, String jarName, Integer classId) {
        this.name = name;
        this.jarName = jarName;
        this.classId = classId;
        this.methodIds = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public Set<Integer> getMethodIds() {
        return methodIds;
    }

    public void setMethodIds(Set<Integer> methodIds) {
        this.methodIds = methodIds;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }
}
