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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class StructStoreConfigTest {

    @Test
    public void test() {
        StructStoreConfig config = new StructStoreConfig();
        config.setWorkspace("xx");
        config.setLazyLoad(false);
        config.setSyncWaitForInit(false);
        config.setBanner(false);
        config.setMonitorFileChange(false);
        config.setScheduleDelay(1L);
        config.setScheduleInitialDelay(2L);
        config.setScheduleTimeUnit(TimeUnit.SECONDS);
        Assertions.assertEquals("xx", config.getWorkspace());
        Assertions.assertEquals(1L, config.getScheduleDelay());
        Assertions.assertEquals(2L, config.getScheduleInitialDelay());
        Assertions.assertEquals(TimeUnit.SECONDS, config.getScheduleTimeUnit());
        Assertions.assertFalse(config.isLazyLoad());
        Assertions.assertFalse(config.isSyncWaitForInit());
        Assertions.assertFalse(config.isMonitorFileChange());
        Assertions.assertFalse(config.isBanner());
    }
}