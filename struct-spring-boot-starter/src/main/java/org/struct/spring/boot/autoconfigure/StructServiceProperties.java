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

package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConfigurationProperties(prefix = StarterConstant.SERVICE)
public class StructServiceProperties {

    private boolean lazyLoad = true;
    private boolean monitorFileChange = true;
    private long scheduleInitialDelay = 10000L;
    private long scheduleDelay = 5000L;
    private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;
    private boolean banner = true;

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public boolean isMonitorFileChange() {
        return monitorFileChange;
    }

    public void setMonitorFileChange(boolean monitorFileChange) {
        this.monitorFileChange = monitorFileChange;
    }

    public long getScheduleInitialDelay() {
        return scheduleInitialDelay;
    }

    public void setScheduleInitialDelay(long scheduleInitialDelay) {
        this.scheduleInitialDelay = scheduleInitialDelay;
    }

    public long getScheduleDelay() {
        return scheduleDelay;
    }

    public void setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
    }

    public TimeUnit getScheduleTimeUnit() {
        return scheduleTimeUnit;
    }

    public void setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
        this.scheduleTimeUnit = scheduleTimeUnit;
    }

    public boolean isBanner() {
        return banner;
    }

    public void setBanner(boolean banner) {
        this.banner = banner;
    }
}
