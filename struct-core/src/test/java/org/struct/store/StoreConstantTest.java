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
class StoreConstantTest {

    /**
     * The bean definition property keys are part of the (de)serialization contract between the
     * bean definition generator and the store beans - changing a value silently breaks every
     * store registered by the generator.
     */
    @Test
    void testConstants() {
        Assertions.assertEquals("clzOfBean", StoreConstant.CLZ_OF_BEAN);
        Assertions.assertEquals("keyResolver", StoreConstant.KEY_RESOLVER);
        Assertions.assertEquals("keyResolverBeanName", StoreConstant.KEY_RESOLVER_BEAN_NAME);
        Assertions.assertEquals("keyResolverBeanClass", StoreConstant.KEY_RESOLVER_BEAN_CLASS);
        Assertions.assertEquals("options", StoreConstant.KEY_OPTIONS);
        Assertions.assertEquals("./data/", StoreConstant.STRUCT_WORKSPACE);
    }

    @Test
    void testInstantiable() {
        Assertions.assertNotNull(new StoreConstant());
    }
}
