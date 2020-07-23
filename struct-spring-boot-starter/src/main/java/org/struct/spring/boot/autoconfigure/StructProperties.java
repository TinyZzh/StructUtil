/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
