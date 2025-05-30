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

package org.struct.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class StringsTest {

    @Test
    public void test() {
        Assertions.assertNull(Strings.capitalize(null));
        Assertions.assertEquals("", Strings.capitalize(""));
        Assertions.assertEquals("Xyz", Strings.capitalize("xyz"));
        Assertions.assertEquals("X1", Strings.capitalize("x1"));
        Assertions.assertEquals("123", Strings.capitalize("123"));
    }
}