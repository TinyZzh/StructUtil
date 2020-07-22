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

package org.struct.spring.support;

import java.util.concurrent.TimeUnit;

/**
 * @author TinyZ.
 * @date 2020-07-22.
 */
public class StructConfig {

    /**
     * set {@link org.struct.core.StructWorker}'s workspace.
     */
    private String workspace = "";
    /**
     * Lazy load struct data before user use it.
     *
     * @see GenericStructMapper#initialize()
     */
    private boolean lazyLoad = false;
    /**
     * Monitor struct file changed event.
     */
    private boolean monitorFileChange = true;
    /**
     * Set the scheduled job's initial delay.
     */
    private long scheduleInitialDelay = 10L;
    /**
     * Set the scheduled job's delay.
     */
    private long scheduleDelay = 5L;
    /**
     * Set schedule job's {@link TimeUnit}
     */
    private TimeUnit scheduleTimeUnit;


    public String getWorkspace() {
        return workspace;
    }

    public StructConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public StructConfig setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }

    public boolean isMonitorFileChange() {
        return monitorFileChange;
    }

    public StructConfig setMonitorFileChange(boolean monitorFileChange) {
        this.monitorFileChange = monitorFileChange;
        return this;
    }

    public long getScheduleInitialDelay() {
        return scheduleInitialDelay;
    }

    public StructConfig setScheduleInitialDelay(long scheduleInitialDelay) {
        this.scheduleInitialDelay = scheduleInitialDelay;
        return this;
    }

    public long getScheduleDelay() {
        return scheduleDelay;
    }

    public StructConfig setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
        return this;
    }

    public TimeUnit getScheduleTimeUnit() {
        return scheduleTimeUnit;
    }

    public StructConfig setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
        this.scheduleTimeUnit = scheduleTimeUnit;
        return this;
    }
}
