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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayConverterTest {

    private final ArrayConverter converter = new ArrayConverter();

    @Test
    public void testDefaultSeparator() {
        Assertions.assertArrayEquals(new int[]{1, 2, 3}, (int[]) converter.convert(null, "1|2|3", int[].class));
        Assertions.assertArrayEquals(new String[]{"a", "b"}, (String[]) converter.convert(null, "a|b", String[].class));
    }

    @Test
    public void testSingleElement() {
        Assertions.assertArrayEquals(new int[]{7}, (int[]) converter.convert(null, "7", int[].class));
    }

    @Test
    public void testTrim() {
        Assertions.assertArrayEquals(new int[]{1, 2}, (int[]) converter.convert(null, " 1 | 2 ", int[].class));
    }

    @Test
    public void testNullValue() {
        //  an absent value becomes an EMPTY array (the framework's zero value for
        //  an array type), never null and never an exception.
        Assertions.assertArrayEquals(new int[0], (int[]) converter.convert(null, null, int[].class));
        Assertions.assertArrayEquals(new String[0], (String[]) converter.convert(null, null, String[].class));
        Assertions.assertArrayEquals(new String[0], (String[]) ConverterRegistry.convert(null, null, String[].class));
    }

    /**
     * A non array target type used to raise a {@link NullPointerException}, because
     * {@code Class#getComponentType()} returns {@code null} for e.g. {@code List.class}.
     */
    @Test
    public void testTargetNotArray() {
        Assertions.assertNull(converter.convert(null, "1|2", List.class));
        Assertions.assertNull(converter.convert(null, "1|2", String.class));
        Assertions.assertNull(converter.convert(null, "1|2", int.class));
    }

    /**
     * Structured origin values (e.g. a json array) are consumed element-wise instead
     * of being split by the separator.
     */
    @Test
    public void testCollectionOriginValue() {
        Assertions.assertArrayEquals(new int[]{1, 2, 3},
                (int[]) converter.convert(null, Arrays.asList(1, 2, 3), int[].class));
        Assertions.assertArrayEquals(new String[]{"a", "b"},
                (String[]) converter.convert(null, Arrays.asList("a", "b"), String[].class));
        //  elements are converted, not just copied
        Assertions.assertArrayEquals(new int[]{1, 2},
                (int[]) converter.convert(null, Arrays.asList("1", "2"), int[].class));
    }

    @Test
    public void testArrayOriginValue() {
        Assertions.assertArrayEquals(new String[]{"a", "b"},
                (String[]) converter.convert(null, new String[]{"a", "b"}, String[].class));
        Assertions.assertArrayEquals(new int[]{1, 2},
                (int[]) converter.convert(null, new String[]{"1", "2"}, int[].class));
    }

    @Test
    public void testEmptyCollection() {
        Assertions.assertArrayEquals(new int[0],
                (int[]) converter.convert(null, Collections.emptyList(), int[].class));
    }

    @Test
    public void testCustomSeparatorAndIgnoreBlank() {
        ArrayConverter c = new ArrayConverter(",", true, true);
        Assertions.assertArrayEquals(new String[]{"a", "b"}, (String[]) c.convert(null, "a,,b", String[].class));
        Assertions.assertArrayEquals(new int[]{1, 2}, (int[]) c.convert(null, "1,,2", int[].class));
    }

    //  ------------------------------------------------------------------
    //  the following cover the type conversion risks introduced by accepting
    //  structured (Collection / array) origin values, whose elements are NOT
    //  guaranteed to be Strings.
    //  ------------------------------------------------------------------

    /**
     * A {@code null} element must not break a primitive array.
     * <p>
     * {@code Array.set(int[], i, null)} raises {@link IllegalArgumentException},
     * so the element has to go through the component converter - which turns
     * {@code null} into {@code 0} for primitive wrappers.
     */
    @Test
    public void testNullElementIntoPrimitiveArray() {
        Object r = converter.convert(null, Arrays.asList(1, null, 3), int[].class);
        Assertions.assertArrayEquals(new int[]{1, 0, 3}, (int[]) r);

        Object l = converter.convert(null, Arrays.asList(1L, null), long[].class);
        Assertions.assertArrayEquals(new long[]{1L, 0L}, (long[]) l);

        Object b = converter.convert(null, Arrays.asList(true, null), boolean[].class);
        Assertions.assertArrayEquals(new boolean[]{true, false}, (boolean[]) b);
    }

    /**
     * A {@code null} element in an object array follows the <em>component</em>
     * converter's own null semantics - exactly like a scalar field would.
     * <p>
     * {@code StringConverter} returns {@code null} for null, whereas the number
     * converters return {@code 0}/{@code 0.0} (see {@code EmbeddedConverters}).
     * Keeping the array element behaviour identical to the scalar behaviour is
     * deliberate: the array must not be stricter or looser than a single field.
     */
    @Test
    public void testNullElementIntoObjectArray() {
        //  String: null stays null
        Assertions.assertArrayEquals(new String[]{"a", null, "c"},
                (String[]) converter.convert(null, Arrays.asList("a", null, "c"), String[].class));
        //  Integer: null becomes 0 - the very same as a scalar `Integer` field
        //  with a missing value.
        Assertions.assertArrayEquals(new Integer[]{1, 0},
                (Integer[]) converter.convert(null, Arrays.asList(1, null), Integer[].class));
        //  consistency check: the scalar path behaves the same way
        Assertions.assertEquals(0, ConverterRegistry.convert(null, null, Integer.class));
        Assertions.assertNull(ConverterRegistry.convert(null, null, String.class));
    }

    /**
     * A {@code null} element in an enum array stays {@code null}.
     * (before the enum fix it would have silently become the first constant)
     */
    @Test
    public void testNullElementIntoEnumArray() {
        Object r = converter.convert(null, Arrays.asList("RED", null), Color[].class);
        Assertions.assertArrayEquals(new Color[]{Color.RED, null}, (Color[]) r);
    }

    /**
     * Elements of a structured value are converted, not blindly copied -
     * including widening / narrowing between number types.
     */
    @Test
    public void testElementConversionBetweenTypes() {
        //  Integer -> String
        Assertions.assertArrayEquals(new String[]{"1", "2"},
                (String[]) converter.convert(null, Arrays.asList(1, 2), String[].class));
        //  Long -> int (with range check inside IntegerConverter)
        Assertions.assertArrayEquals(new int[]{1, 2},
                (int[]) converter.convert(null, Arrays.asList(1L, 2L), int[].class));
        //  numeric String -> int
        Assertions.assertArrayEquals(new int[]{1, 2},
                (int[]) converter.convert(null, Arrays.asList("1", "2"), int[].class));
        //  Boolean -> String
        Assertions.assertArrayEquals(new String[]{"true"},
                (String[]) converter.convert(null, Collections.singletonList(true), String[].class));
    }

    /**
     * An out-of-range number must fail loudly instead of silently truncating.
     */
    @Test
    public void testOutOfRangeElementThrows() {
        Assertions.assertThrows(RuntimeException.class,
                () -> converter.convert(null, Collections.singletonList(Long.MAX_VALUE), int[].class));
    }

    /**
     * An element that cannot be converted at all fails loudly.
     */
    @Test
    public void testUnconvertibleElementThrows() {
        Assertions.assertThrows(RuntimeException.class,
                () -> converter.convert(null, Collections.singletonList("not-a-number"), int[].class));
    }

    /**
     * An array origin value is converted element-wise instead of being
     * stringified into {@code "[Ljava.lang.String;@..."}.
     */
    @Test
    public void testArrayOriginValueIsNotStringified() {
        //  before the fix, String.valueOf(arr) produced "[Ljava.lang.String;@1b6d3586"
        Assertions.assertArrayEquals(new int[]{1, 2},
                (int[]) converter.convert(null, new String[]{"1", "2"}, int[].class));
        Assertions.assertArrayEquals(new int[]{5, 6},
                (int[]) converter.convert(null, new Integer[]{5, 6}, int[].class));
        //  a primitive array is converted too
        Assertions.assertArrayEquals(new String[]{"7", "8"},
                (String[]) converter.convert(null, new int[]{7, 8}, String[].class));
    }

    /**
     * The separator based path must keep its original semantics:
     * {@code ignoreBlank} filters <em>before</em> trimming, so a blank-but-not-empty
     * token (" ") survives the filter and is only trimmed afterwards.
     */
    @Test
    public void testIgnoreBlankFiltersBeforeTrim() {
        //  " " is not empty -> kept by ignoreBlank, then trimmed to ""
        ArrayConverter c = new ArrayConverter("\\|", true, true);
        Assertions.assertArrayEquals(new String[]{"a", "", "b"},
                (String[]) c.convert(null, "a| |b", String[].class));
        //  "" is empty -> dropped by ignoreBlank
        Assertions.assertArrayEquals(new String[]{"a", "b"},
                (String[]) c.convert(null, "a||b", String[].class));
        //  with ignoreBlank disabled the blank token is preserved
        ArrayConverter c2 = new ArrayConverter("\\|", true, false);
        Assertions.assertArrayEquals(new String[]{"a", "", "b"},
                (String[]) c2.convert(null, "a||b", String[].class));
    }

    /**
     * Trimming must not be applied when it is switched off.
     */
    @Test
    public void testTrimDisabled() {
        ArrayConverter c = new ArrayConverter("\\|", false, false);
        Assertions.assertArrayEquals(new String[]{" a ", "b"},
                (String[]) c.convert(null, " a |b", String[].class));
    }

    /**
     * A non array target type returns {@code null} instead of raising a
     * {@link NullPointerException} ({@code Class#getComponentType()} is null).
     */
    @Test
    public void testNonArrayTargetReturnsNull() {
        Assertions.assertNull(converter.convert(null, "1|2", List.class));
        Assertions.assertNull(converter.convert(null, Arrays.asList(1, 2), List.class));
        Assertions.assertNull(converter.convert(null, "1|2", String.class));
        Assertions.assertNull(converter.convert(null, "1|2", int.class));
    }

    /**
     * A {@code null} origin value must keep the ORIGINAL behaviour: an empty array.
     * <p>
     * This deliberately does NOT return {@code null}, because the framework
     * consistently maps a null input to the target type's zero value
     * ({@code 0} / {@code 0.0} / {@code BigInteger.ZERO} / empty array).
     */
    @Test
    public void testNullOriginKeepsEmptyArray() {
        Assertions.assertArrayEquals(new int[0], (int[]) converter.convert(null, null, int[].class));
        Assertions.assertArrayEquals(new String[0], (String[]) converter.convert(null, null, String[].class));
        //  collection fields yield an empty collection, not null
        Object col = ConverterRegistry.convertCollection(null, null, List.class, String.class);
        Assertions.assertNotNull(col);
        Assertions.assertTrue(((List<?>) col).isEmpty());
    }

    public enum Color {RED, GREEN, BLUE}
}
