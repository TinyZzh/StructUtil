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

    /**
     * A missing value must resolve to {@code null}, NOT to the first enum constant.
     * <p>
     * Before the fix, {@code null} was converted to Integer {@code 0} and then silently
     * resolved to {@code enums[0]} - an empty enum column silently became {@code One}.
     */
    @Test
    public void testNullValue() {
        Assertions.assertNull(converter().convert(null, null, MyEnum.class));
        Assertions.assertNull(ConverterRegistry.convert(null, null, MyEnum.class));
        //  must NOT be the first constant
        Assertions.assertNotEquals(MyEnum.One, ConverterRegistry.convert(null, null, MyEnum.class));
    }

    /**
     * A blank value behaves like a missing one - {@link org.struct.core.StructImpl}
     * already drops empty strings, so both must lead to the same result.
     */
    @Test
    public void testBlankValue() {
        Assertions.assertNull(converter().convert(null, "", MyEnum.class));
        Assertions.assertNull(converter().convert(null, "   ", MyEnum.class));
    }

    /**
     * {@code 0} is a real ordinal and must still resolve to the first constant.
     */
    @Test
    public void testZeroOrdinalIsStillResolved() {
        Assertions.assertEquals(MyEnum.One, converter().convert(null, 0, MyEnum.class));
        Assertions.assertEquals(MyEnum.One, converter().convert(null, "0", MyEnum.class));
    }

    /**
     * An out-of-range ordinal must fall through the {@code int -> enum} shortcut and
     * fail loudly at the end, instead of silently returning a wrong constant.
     * <p>
     * Covers both halves of {@code 0 <= i && i < enums.length}:
     * {@code -1} fails the lower bound, {@code 3} fails the upper bound (3 constants).
     */
    @Test
    public void testOrdinalOutOfRange() {
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, -1, MyEnum.class));
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, 3, MyEnum.class));
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, "-1", MyEnum.class));
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, "3", MyEnum.class));
    }

    /**
     * The {@code enum -> enum} shortcut rejects a source constant whose ordinal is
     * beyond the target enum's size.
     */
    @Test
    public void testEnumToEnumOrdinalOutOfRange() {
        Assertions.assertEquals(Upper.RED, converter().convert(null, Lower.RED, Upper.class));
        //  Lower.BLUE has ordinal 2, but Upper has only 2 constants (0, 1)
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, Lower.BLUE, Upper.class));
    }

    /**
     * {@link Enum#valueOf(Class, String)} is case sensitive, so an upper-case constant
     * is only reachable through the upper-case fallback.
     */
    @Test
    public void testUpperCaseFallback() {
        Assertions.assertEquals(Upper.RED, converter().convert(null, "RED", Upper.class));
        Assertions.assertEquals(Upper.RED, converter().convert(null, "red", Upper.class));
        Assertions.assertEquals(Upper.GREEN, converter().convert(null, " green ", Upper.class));
        //  a value that matches neither the exact nor the upper-case name - and not even
        //  case-insensitively - is rejected.
        Assertions.assertThrows(IllegalStateException.class, () -> converter().convert(null, "blue", Upper.class));
    }

    private static EnumConverter converter() {
        return new EnumConverter();
    }

    public enum MyEnum {
        One,
        Two,
        Three

    }

    /**
     * Constants named in upper case - only reachable via the {@code toUpperCase()} fallback.
     */
    public enum Upper {
        RED, GREEN
    }

    /**
     * One constant more than {@link Upper}.
     */
    public enum Lower {
        RED, GREEN, BLUE
    }
}