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

package org.struct.spring.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StructConstantTest {

    /**
     * The bean definition property keys are part of the (de)serialization contract
     * between {@link ClassPathStructScanner} and the store beans - changing a value
     * silently breaks every store registered by the scanner.
     */
    @Test
    void testConstants() {
        Assertions.assertEquals("clzOfBean", StructConstant.CLZ_OF_BEAN);
        Assertions.assertEquals("keyResolver", StructConstant.KEY_RESOLVER);
        Assertions.assertEquals("keyResolverBeanName", StructConstant.KEY_RESOLVER_BEAN_NAME);
        Assertions.assertEquals("keyResolverBeanClass", StructConstant.KEY_RESOLVER_BEAN_CLASS);
        Assertions.assertEquals("options", StructConstant.KEY_OPTIONS);
        Assertions.assertEquals("./data/", StructConstant.STRUCT_WORKSPACE);
    }

    @Test
    void testInstantiable() {
        Assertions.assertNotNull(new StructConstant());
    }
}
