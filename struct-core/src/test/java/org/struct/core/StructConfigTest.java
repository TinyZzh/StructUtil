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

package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class StructConfigTest {

    @Test
    public void test() {
        StructConfig config = new StructConfig();
        config.setArrayConverterIgnoreBlank(false);
        config.setArrayConverterStringSeparator("xx");
        config.setIgnoreEmptyRow(false);
        config.setArrayConverterIgnoreBlank(false);
        config.setArrayConverterStringTrim(false);
        config.setStructRequiredDefault(false);
        Assertions.assertFalse(config.isArrayConverterIgnoreBlank());
        Assertions.assertFalse(config.isArrayConverterStringTrim());
        Assertions.assertFalse(config.isIgnoreEmptyRow());
        Assertions.assertFalse(config.isStructRequiredDefault());
        Assertions.assertEquals("xx", config.getArrayConverterStringSeparator());
    }
}