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

public class EnumConverterTest {

    @Test
    public void test() {
        EnumConverter converter = new EnumConverter();
        Assertions.assertEquals(MyEnum.One, converter.convert(null, 0, MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, converter.convert(null, 1.0D, MyEnum.class));
        Assertions.assertEquals(MyEnum.Three, converter.convert(null, 2L, MyEnum.class));

        Assertions.assertEquals(MyEnum.One, converter.convert(null, "0", MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, converter.convert(null, "1", MyEnum.class));
        Assertions.assertEquals(MyEnum.Three, converter.convert(null, "2", MyEnum.class));

        Assertions.assertEquals(MyEnum.One, converter.convert(null, "One", MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, converter.convert(null, "Two", MyEnum.class));
        Assertions.assertEquals(MyEnum.Three, converter.convert(null, "Three", MyEnum.class));

        Assertions.assertEquals(MyEnum.One, converter.convert(null, "one", MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, converter.convert(null, "two", MyEnum.class));
        Assertions.assertEquals(MyEnum.Three, converter.convert(null, "three", MyEnum.class));

        Assertions.assertEquals("three", converter.convert(null, "three", Integer.class));

        Assertions.assertEquals(MyEnum.One, converter.convert(null, MyEnum.One, MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, converter.convert(null, MyEnum.Two, MyEnum.class));
        Assertions.assertEquals(MyEnum.Three, converter.convert(null, MyEnum.Three, MyEnum.class));
        try {
            converter.convert(null, "four", MyEnum.class);
        } catch (IllegalStateException e) {
            //  no-op
        } catch (Exception e) {
            Assertions.fail("four");
        }
    }

    @Test
    public void testEnum() {
        Assertions.assertTrue(Enum.class.isAssignableFrom(MyEnum.class));
        Assertions.assertEquals(MyEnum.Two, ConverterRegistry.convert(null, "two", MyEnum.class));
    }

    public enum MyEnum {
        One,
        Two,
        Three

    }
}