/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
class StructServicePropertiesTest {

    @Test
    public void test() {
        StructServiceProperties properties = new StructServiceProperties();
        properties.setWorkspace("./");
        properties.setBanner(true);
        properties.setLazyLoad(true);
        properties.setWatchFile(true);
        properties.setScheduleDelay(1L);
        properties.setScheduleTimeUnit(TimeUnit.DAYS);
        properties.setScheduleInitialDelay(2L);
        Assertions.assertEquals("./", properties.getWorkspace());
        Assertions.assertTrue(properties.isBanner());
        Assertions.assertTrue(properties.isLazyLoad());
        Assertions.assertTrue(properties.isWatchFile());
        Assertions.assertEquals(1L, properties.getScheduleDelay());
        Assertions.assertEquals(2L, properties.getScheduleInitialDelay());
        Assertions.assertEquals(TimeUnit.DAYS, properties.getScheduleTimeUnit());
    }
}