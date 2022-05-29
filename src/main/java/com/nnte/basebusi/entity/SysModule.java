package com.nnte.basebusi.entity;

public class SysModule {
    public SysModule(String code,String name){
        moduleCode = code;
        moduleName=name;
        frameModule=true;
        setModuleVersion("1.0.1-SNAPSHOT");
    }
    public SysModule(String code,String name,boolean frame){
        moduleCode = code;
        moduleName=name;
        frameModule=frame;
        setModuleVersion("1.0.1-SNAPSHOT");
    }
    public SysModule(String code,String name,boolean frame,String version){
        moduleCode = code;
        moduleName=name;
        frameModule=frame;
        setModuleVersion(version);
    }
    private String moduleCode;
    private String moduleName;
    private boolean frameModule;
    private String moduleVersion;

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isFrameModule() {
        return frameModule;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }
}
