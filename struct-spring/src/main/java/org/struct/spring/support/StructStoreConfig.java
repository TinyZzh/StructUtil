/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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
public class StructStoreConfig {

    /**
     * set {@link org.struct.core.StructWorker}'s workspace.
     */
    private String workspace = "";
    /**
     * Lazy load struct data before user use it.
     *
     * @see StructStore#initialize()
     */
    private boolean lazyLoad = true;
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
    private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;
    /**
     * When the {@link #lazyLoad} is true, is user should sync wait for {@link StructStore} init done.
     */
    private boolean syncWaitForInit = true;
    /**
     * Print {@link StructStoreService}'s banner.
     */
    private boolean banner = true;


    public String getWorkspace() {
        return workspace;
    }

    public StructStoreConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public StructStoreConfig setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }

    public boolean isMonitorFileChange() {
        return monitorFileChange;
    }

    public StructStoreConfig setMonitorFileChange(boolean monitorFileChange) {
        this.monitorFileChange = monitorFileChange;
        return this;
    }

    public long getScheduleInitialDelay() {
        return scheduleInitialDelay;
    }

    public StructStoreConfig setScheduleInitialDelay(long scheduleInitialDelay) {
        this.scheduleInitialDelay = scheduleInitialDelay;
        return this;
    }

    public long getScheduleDelay() {
        return scheduleDelay;
    }

    public StructStoreConfig setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
        return this;
    }

    public TimeUnit getScheduleTimeUnit() {
        return scheduleTimeUnit;
    }

    public StructStoreConfig setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
        this.scheduleTimeUnit = scheduleTimeUnit;
        return this;
    }

    public boolean isSyncWaitForInit() {
        return syncWaitForInit;
    }

    public void setSyncWaitForInit(boolean syncWaitForInit) {
        this.syncWaitForInit = syncWaitForInit;
    }

    public boolean isBanner() {
        return banner;
    }

    public void setBanner(boolean banner) {
        this.banner = banner;
    }
}
