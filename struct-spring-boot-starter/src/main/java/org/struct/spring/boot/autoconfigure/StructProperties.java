package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConfigurationProperties(prefix = StarterConstant.STRUCT_UTIL)
public class StructProperties {

    private String workspace = "";
    private boolean lazyLoad = false;
    private boolean monitorFileChange = true;
    private long scheduleInitialDelay = 10L;
    private long scheduleDelay = 5L;
    private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;

    public String getWorkspace() {
        return workspace;
    }

    public StructProperties setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public StructProperties setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }

    public boolean isMonitorFileChange() {
        return monitorFileChange;
    }

    public StructProperties setMonitorFileChange(boolean monitorFileChange) {
        this.monitorFileChange = monitorFileChange;
        return this;
    }

    public long getScheduleInitialDelay() {
        return scheduleInitialDelay;
    }

    public StructProperties setScheduleInitialDelay(long scheduleInitialDelay) {
        this.scheduleInitialDelay = scheduleInitialDelay;
        return this;
    }

    public long getScheduleDelay() {
        return scheduleDelay;
    }

    public StructProperties setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
        return this;
    }

    public TimeUnit getScheduleTimeUnit() {
        return scheduleTimeUnit;
    }

    public StructProperties setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
        this.scheduleTimeUnit = scheduleTimeUnit;
        return this;
    }
}
