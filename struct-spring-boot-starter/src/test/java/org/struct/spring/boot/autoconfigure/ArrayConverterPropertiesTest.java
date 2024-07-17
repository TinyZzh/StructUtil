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
 * @author TinyZ
 * @date 2023-05-22
 */
class ArrayConverterPropertiesTest {

    @Test
    public void test() {
        ArrayConverterProperties prop1 = new ArrayConverterProperties();
        prop1.setStringSeparator(":");
        prop1.setStringTrim(false);
        prop1.setIgnoreBlank(false);
        Assertions.assertEquals(prop1, prop1);
        Assertions.assertNotEquals(prop1, null);
        Assertions.assertNotEquals(prop1, new Object());

        ArrayConverterProperties prop2 = new ArrayConverterProperties();
        prop2.setStringSeparator(":");
        prop2.setStringTrim(false);
        prop2.setIgnoreBlank(false);

        Assertions.assertEquals(prop1, prop2);
        {
            prop2.setStringSeparator(",");
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setStringSeparator(":");
        }
        {
            prop2.setStringTrim(true);
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setStringTrim(false);
        }
        {
            prop2.setIgnoreBlank(true);
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setIgnoreBlank(false);
        }

        Assertions.assertEquals(prop1.toString(), prop2.toString());
        Assertions.assertEquals(prop1.hashCode(), prop2.hashCode());
    }

}