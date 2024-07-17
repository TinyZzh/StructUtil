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

package org.struct.core;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayKeyTest {

    @Test
    public void testEquals() {
        ArrayKey ak1 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak2 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak3 = new ArrayKey(new Object[]{1, 2});
        Assertions.assertEquals(ak1, ak2);
        Assertions.assertNotEquals(ak2, ak3);
        Assertions.assertNotEquals(1L, ak2);
    }

    @Test
    public void testToString() {
        String string = new ArrayKey(new Object[]{1, 2, 3}).toString();
        Assertions.assertEquals("[1, 2, 3]", string);
    }
}