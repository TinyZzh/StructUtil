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
import org.struct.core.converter.EmbeddedConverters.BigDecimalConverter;
import org.struct.core.converter.EmbeddedConverters.BigIntegerConverter;
import org.struct.core.converter.EmbeddedConverters.BooleanConverter;
import org.struct.core.converter.EmbeddedConverters.ByteConverter;
import org.struct.core.converter.EmbeddedConverters.DoubleConverter;
import org.struct.core.converter.EmbeddedConverters.FloatConverter;
import org.struct.core.converter.EmbeddedConverters.LongConverter;
import org.struct.core.converter.EmbeddedConverters.ShortConverter;
import org.struct.exception.UnSupportConvertOperationException;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.struct.core.converter.EmbeddedConverters.IntegerConverter;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
public class EmbeddedConvertersTest {

    @Test
    public void test() {
        EmbeddedConverters converters = new EmbeddedConverters();
        Assertions.assertNotNull(converters.getConverters());
    }

    @Test
    public void testIntegerConverter() {
        IntegerConverter converter = new IntegerConverter();
        //  int.class
        Assertions.assertEquals(0, converter.convert(null, int.class));
        Assertions.assertEquals(1, converter.convert("1", int.class));
        Assertions.assertEquals(1, converter.convert("0x1", int.class));
        Assertions.assertEquals(1, converter.convert(1, int.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE), int.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, int.class));
        //  Integer.class
        Assertions.assertEquals(0, converter.convert(null, Integer.class));
        Assertions.assertEquals(1, converter.convert("1", Integer.class));
        Assertions.assertEquals(1, converter.convert("0x1", Integer.class));
        Assertions.assertEquals(1, converter.convert(1, Integer.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE), Integer.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Integer.class));
    }

    @Test
    public void testLongConverter() {
        LongConverter converter = new LongConverter();
        //  int.class
        Assertions.assertEquals(0L, converter.convert(null, long.class));
        Assertions.assertEquals(1L, converter.convert("1", long.class));
        Assertions.assertEquals(1L, converter.convert("0x1", long.class));
        Assertions.assertEquals(1L, converter.convert(1, long.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, long.class));
        //  Integer.class
        Assertions.assertEquals(0L, converter.convert(null, Long.class));
        Assertions.assertEquals(1L, converter.convert("1", Long.class));
        Assertions.assertEquals(1L, converter.convert("0x1", Long.class));
        Assertions.assertEquals(1L, converter.convert(1, Long.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Long.class));
    }

    @Test
    public void testBooleanConverter() {
        BooleanConverter converter = new BooleanConverter();
        //  int.class
        Assertions.assertEquals(false, converter.convert(null, boolean.class));
        Assertions.assertEquals(true, converter.convert(Boolean.TRUE, boolean.class));
        Assertions.assertEquals(true, converter.convert((short) 1, boolean.class));
        Assertions.assertEquals(true, converter.convert("y", boolean.class));
        Assertions.assertEquals(true, converter.convert(1, boolean.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, boolean.class));
        //  Integer.class
        Assertions.assertEquals(false, converter.convert(null, Boolean.class));
        Assertions.assertEquals(true, converter.convert(Boolean.TRUE, Boolean.class));
        Assertions.assertEquals(true, converter.convert(1, Boolean.class));
        Assertions.assertEquals(true, converter.convert("y", Boolean.class));
        Assertions.assertEquals(true, converter.convert(1, Boolean.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Boolean.class));
    }

    @Test
    public void testShortConverter() {
        ShortConverter converter = new ShortConverter();
        //  int.class
        Assertions.assertEquals((short) 0, converter.convert(null, short.class));
        Assertions.assertEquals((short) 1, converter.convert("1", short.class));
        Assertions.assertEquals((short) 1, converter.convert("0x1", short.class));
        Assertions.assertEquals((short) 1, converter.convert(1, short.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE), short.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, short.class));
        //  Integer.class
        Assertions.assertEquals((short) 0, converter.convert(null, Short.class));
        Assertions.assertEquals((short) 1, converter.convert("1", Short.class));
        Assertions.assertEquals((short) 1, converter.convert("0x1", Short.class));
        Assertions.assertEquals((short) 1, converter.convert(1, Short.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE), Short.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Short.class));
    }

    @Test
    public void testByteConverter() {
        ByteConverter converter = new ByteConverter();
        //  int.class
        Assertions.assertEquals((byte) 0x00, converter.convert(null, byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert("1", byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert("0x1", byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert(1, byte.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE), byte.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, byte.class));
        //  Integer.class
        Assertions.assertEquals((byte) 0x00, converter.convert(null, Byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert("1", Byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert("0x1", Byte.class));
        Assertions.assertEquals((byte) 0x01, converter.convert(1, Byte.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert(BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE), Byte.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Byte.class));
    }

    @Test
    public void testFloatConverter() {
        FloatConverter converter = new FloatConverter();
        //  int.class
        Assertions.assertEquals(0.0F, converter.convert(null, float.class));
        Assertions.assertEquals(1.0F, converter.convert("1", float.class));
        Assertions.assertEquals(1.0F, converter.convert(1, float.class));
        Assertions.assertThrows(NumberFormatException.class, () -> converter.convert("0x1", float.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, float.class));
        //  Integer.class
        Assertions.assertEquals(0.0F, converter.convert(null, Float.class));
        Assertions.assertEquals(1.0F, converter.convert("1", Float.class));
        Assertions.assertEquals(1.0F, converter.convert(1, Float.class));
        Assertions.assertThrows(NumberFormatException.class, () -> converter.convert("0x1", Float.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Float.class));
    }

    @Test
    public void testDoubleConverter() {
        DoubleConverter converter = new DoubleConverter();
        //  int.class
        Assertions.assertEquals(0.0D, converter.convert(null, double.class));
        Assertions.assertEquals(1.0D, converter.convert("1", double.class));
        Assertions.assertEquals(1.0D, converter.convert(1, double.class));
        Assertions.assertThrows(NumberFormatException.class, () -> converter.convert("0x1", double.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, double.class));
        //  Integer.class
        Assertions.assertEquals(0.0D, converter.convert(null, Double.class));
        Assertions.assertEquals(1.0D, converter.convert("1", Double.class));
        Assertions.assertEquals(1.0D, converter.convert(1, Double.class));
        Assertions.assertThrows(NumberFormatException.class, () -> converter.convert("0x1", Double.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, Double.class));
    }

    @Test
    public void testBigIntegerConverter() {
        BigIntegerConverter converter = new BigIntegerConverter();
        //  int.class
        Assertions.assertEquals(BigInteger.ZERO, converter.convert(null, BigInteger.class));
        Assertions.assertEquals(BigInteger.ONE, converter.convert("1", BigInteger.class));
        Assertions.assertEquals(BigInteger.ONE, converter.convert("0x1", BigInteger.class));
        Assertions.assertEquals(BigInteger.ONE, converter.convert(1, BigInteger.class));
        Assertions.assertEquals(BigInteger.ONE, converter.convert(BigDecimal.valueOf(1), BigInteger.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, BigInteger.class));
    }

    @Test
    public void testBigDecimalConverter() {
        BigDecimalConverter converter = new BigDecimalConverter();
        //  BigDecimal.class
        Assertions.assertEquals(BigDecimal.ZERO, converter.convert(null, BigDecimal.class));
        Assertions.assertEquals(BigDecimal.ONE, converter.convert("1", BigDecimal.class));
        Assertions.assertEquals(BigDecimal.ONE, converter.convert(1, BigDecimal.class));
        Assertions.assertThrows(NumberFormatException.class, () -> converter.convert("0x1", BigDecimal.class));
        Assertions.assertThrows(UnSupportConvertOperationException.class, () -> converter.convert(converter, BigDecimal.class));
    }
}