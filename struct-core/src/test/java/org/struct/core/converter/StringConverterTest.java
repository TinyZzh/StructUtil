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

package org.struct.core.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
class StringConverterTest {

    @Test
    public void test() {
        StringConverter converter = new StringConverter();
        Assertions.assertEquals("xx", converter.convert("xx", String.class));
        Assertions.assertEquals(1, converter.convert(1, int.class));
        Assertions.assertNull(converter.convert(null, String.class));
    }
}