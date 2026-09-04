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

class StarterConstantTest {

    /**
     * These prefixes are public configuration keys - changing one silently disables
     * the corresponding auto configuration for every user.
     */
    @Test
    void testConstants() {
        Assertions.assertEquals("struct", StarterConstant.STRUCT_UTIL);
        Assertions.assertEquals("struct.store", StarterConstant.STRUCT_STORE);
        Assertions.assertEquals("enable", StarterConstant.ENABLE);
        Assertions.assertEquals("struct.store.service", StarterConstant.SERVICE);
        Assertions.assertEquals("struct.store.service.watch-file", StarterConstant.WATCH_FILE);
    }

    @Test
    void testInstantiable() {
        Assertions.assertNotNull(new StarterConstant());
    }

    /**
     * The health contributor auto configuration only wires beans together; simply
     * instantiating it verifies the constructor still resolves.
     */
    @Test
    void testHealthContributorAutoConfiguration() {
        Assertions.assertNotNull(new StructStoreServiceHealthContributorAutoConfiguration());
    }
}
