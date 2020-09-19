package org.struct.spring.support;

/**
 * @author TinyZ.
 * @version 2020.09.19
 */
class Options {

    private String workspace = StructConstant.STRUCT_WORKSPACE;

    private boolean lazyLoad = false;

    private boolean waitForInit = false;

    public static Options generate(org.struct.spring.annotation.StructStoreOptions annotation) {
        Options controller = new Options();
        controller.setWorkspace(annotation.workspace());
        controller.setLazyLoad(annotation.lazyLoad());
        controller.setWaitForInit(annotation.waitForInit());
        return controller;
    }

    public static Options generate(StructStoreConfig config) {
        Options controller = new Options();
        controller.setWorkspace(config.getWorkspace());
        controller.setLazyLoad(config.isLazyLoad());
        controller.setWaitForInit(config.isSyncWaitForInit());
        return controller;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public boolean isWaitForInit() {
        return waitForInit;
    }

    public void setWaitForInit(boolean waitForInit) {
        this.waitForInit = waitForInit;
    }
}
