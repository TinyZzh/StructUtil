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

package org.struct.store;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ.
 */
class StoreOptionsTest {

    @Test
    void testDefaults() {
        StoreOptions options = new StoreOptions();
        Assertions.assertEquals(StoreConstant.STRUCT_WORKSPACE, options.getWorkspace());
        Assertions.assertFalse(options.isLazyLoad());
        Assertions.assertFalse(options.isWaitForInit());
    }

    @Test
    void testAccessors() {
        StoreOptions options = new StoreOptions();
        options.setWorkspace("xx");
        options.setLazyLoad(true);
        options.setWaitForInit(true);
        Assertions.assertEquals("xx", options.getWorkspace());
        Assertions.assertTrue(options.isLazyLoad());
        Assertions.assertTrue(options.isWaitForInit());
    }
}
