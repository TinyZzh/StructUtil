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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Every embedded converter dispatches on the runtime type of the source value - each dispatch arm
 * needs its own case, including the hexadecimal notation and the numeric fallbacks.
 *
 * @author TinyZ.
 */
class EmbeddedConverterBranchesTest {

    private static final ConvertContext CTX = null;

    @Test
    void testIntegerConverter() {
        Converter c = new EmbeddedConverters.IntegerConverter();
        Assertions.assertEquals(0, c.convert(CTX, null, int.class));
        Assertions.assertEquals(1, c.convert(CTX, "1", int.class));
        //  hexadecimal notation
        Assertions.assertEquals(0x1F, c.convert(CTX, "0x1F", int.class));
        //  a Number source
        Assertions.assertEquals(5, c.convert(CTX, 5L, int.class));
        //  an unsupported source
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), int.class));
    }

    @Test
    void testLongConverter() {
        Converter c = new EmbeddedConverters.LongConverter();
        Assertions.assertEquals(0L, c.convert(CTX, null, long.class));
        Assertions.assertEquals(1L, c.convert(CTX, "1", long.class));
        Assertions.assertEquals(0x1FL, c.convert(CTX, "0x1F", long.class));
        Assertions.assertEquals(5L, c.convert(CTX, 5, long.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), long.class));
    }

    @Test
    void testShortConverter() {
        Converter c = new EmbeddedConverters.ShortConverter();
        Assertions.assertEquals((short) 0, c.convert(CTX, null, short.class));
        Assertions.assertEquals((short) 1, c.convert(CTX, "1", short.class));
        Assertions.assertEquals((short) 0x1F, c.convert(CTX, "0x1F", short.class));
        Assertions.assertEquals((short) 5, c.convert(CTX, 5, short.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), short.class));
    }

    @Test
    void testByteConverter() {
        Converter c = new EmbeddedConverters.ByteConverter();
        Assertions.assertEquals((byte) 0, c.convert(CTX, null, byte.class));
        Assertions.assertEquals((byte) 1, c.convert(CTX, "1", byte.class));
        Assertions.assertEquals((byte) 0x1F, c.convert(CTX, "0x1F", byte.class));
        Assertions.assertEquals((byte) 5, c.convert(CTX, 5, byte.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), byte.class));
    }

    @Test
    void testBooleanConverter() {
        Converter c = new EmbeddedConverters.BooleanConverter();
        Assertions.assertEquals(false, c.convert(CTX, null, boolean.class));
        //  already a Boolean
        Assertions.assertEquals(Boolean.TRUE, c.convert(CTX, Boolean.TRUE, boolean.class));
        Assertions.assertEquals(Boolean.TRUE, c.convert(CTX, "true", boolean.class));
        //  a Number source: 1 is true, everything else is false
        Assertions.assertEquals(Boolean.TRUE, c.convert(CTX, 1, boolean.class));
        Assertions.assertEquals(Boolean.FALSE, c.convert(CTX, 0, boolean.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), boolean.class));
    }

    @Test
    void testFloatConverter() {
        Converter c = new EmbeddedConverters.FloatConverter();
        Assertions.assertEquals(0.0F, c.convert(CTX, null, float.class));
        Assertions.assertEquals(1.5F, c.convert(CTX, "1.5", float.class));
        //  a Number source
        Assertions.assertEquals(1.5F, c.convert(CTX, 1.5D, float.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), float.class));
    }

    @Test
    void testDoubleConverter() {
        Converter c = new EmbeddedConverters.DoubleConverter();
        Assertions.assertEquals(0.0D, c.convert(CTX, null, double.class));
        Assertions.assertEquals(1.5D, c.convert(CTX, "1.5", double.class));
        Assertions.assertEquals(1.5D, c.convert(CTX, 1.5F, double.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), double.class));
    }

    @Test
    void testBigIntegerConverter() {
        Converter c = new EmbeddedConverters.BigIntegerConverter();
        Assertions.assertEquals(BigInteger.ZERO, c.convert(CTX, null, BigInteger.class));
        Assertions.assertEquals(BigInteger.ONE, c.convert(CTX, "1", BigInteger.class));
        //  hexadecimal notation
        Assertions.assertEquals(BigInteger.valueOf(31L), c.convert(CTX, "0x1F", BigInteger.class));
        //  a BigDecimal source keeps the integral part
        Assertions.assertEquals(BigInteger.valueOf(3L), c.convert(CTX, new BigDecimal("3.7"), BigInteger.class));
        //  any other Number
        Assertions.assertEquals(BigInteger.valueOf(7L), c.convert(CTX, 7L, BigInteger.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), BigInteger.class));
    }

    @Test
    void testBigDecimalConverter() {
        Converter c = new EmbeddedConverters.BigDecimalConverter();
        Assertions.assertEquals(BigDecimal.ZERO, c.convert(CTX, null, BigDecimal.class));
        //  a String source
        Assertions.assertEquals(new BigDecimal("1.25"), c.convert(CTX, "1.25", BigDecimal.class));
        //  a Number source
        Assertions.assertEquals(new BigDecimal("5"), c.convert(CTX, 5, BigDecimal.class));
        Assertions.assertThrows(RuntimeException.class, () -> c.convert(CTX, new Object(), BigDecimal.class));
    }
}
