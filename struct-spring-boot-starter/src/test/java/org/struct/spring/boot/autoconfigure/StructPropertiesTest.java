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

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
class StructPropertiesTest {

    @Test
    public void test() {
        StructProperties properties = new StructProperties();
        Assertions.assertEquals(properties, properties);
        Assertions.assertNotEquals(properties, null);
        Assertions.assertNotEquals(properties, new Object());
        Assertions.assertEquals(properties, new StructProperties());
        Assertions.assertEquals(properties.toString(), new StructProperties().toString());
        Assertions.assertEquals(properties.hashCode(), new StructProperties().hashCode());

        ArrayConverterProperties acp = new ArrayConverterProperties();
        acp.setStringSeparator("a");
        acp.setStringTrim(true);
        acp.setIgnoreBlank(true);
        properties.setArrayConverter(acp);
        properties.setStructRequiredDefault(true);
        properties.setIgnoreEmptyRow(true);

        Assertions.assertEquals("a", properties.getArrayConverter().getStringSeparator());
        Assertions.assertTrue(properties.getArrayConverter().isIgnoreBlank());
        Assertions.assertTrue(properties.getArrayConverter().isIgnoreBlank());
        Assertions.assertTrue(properties.isStructRequiredDefault());
        Assertions.assertTrue(properties.isIgnoreEmptyRow());
    }
}