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

package org.struct.core.converter;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ConverterRegistryTest {

    @Test
    public void lookupOrDefault() {
        final int num = 9999;
        Object obj = ConverterRegistry.lookupOrDefault(A.class, MyConverter.class, num)
                .convert("x", int.class);
        Assert.assertEquals(num, obj);
    }

    @Test
    public void registerConverterClz() {
        final int num = 9999;
        ConverterRegistry.register(A.class, MyConverter.class, num);
        Object obj = ConverterRegistry.lookup(A.class)
                .convert("x", int.class);
        Assert.assertEquals(num, obj);
    }

    public static class A {
        public String content;

        public A(String content) {
            this.content = content;
        }
    }

    public static class MyConverter implements Converter {

        private final int val;

        public MyConverter(int val) {
            this.val = val;
        }

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            //  do nothing and return val.
            return this.val;
        }
    }

    @Test
    public void registerConverter() {
        String content = "x";
        ConverterRegistry.register(B.class, new Converter() {
            @Override
            public Object convert(Object originValue, Class<?> targetType) {
                return new B(String.valueOf(originValue));
            }
        });
        B obj = (B) ConverterRegistry.lookup(B.class).convert(content, String.class);
        Assert.assertEquals(content, obj.content);
    }

    public static class B {

        public String content;

        public B(String content) {
            this.content = content;
        }
    }

    @Test
    public void covert() {
        Assert.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert("1", byte.class));
        Assert.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert("1", Byte.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterRegistry.convert("1", short.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterRegistry.convert("1", Short.class));
        Assert.assertEquals(1, ConverterRegistry.convert("1", int.class));
        Assert.assertEquals(1, ConverterRegistry.convert("1", Integer.class));
        Assert.assertEquals(1L, ConverterRegistry.convert("1", long.class));
        Assert.assertEquals(1L, ConverterRegistry.convert("1", Long.class));
        Assert.assertEquals(true, ConverterRegistry.convert("true", boolean.class));
        Assert.assertEquals(false, ConverterRegistry.convert("false", Boolean.class));
        Assert.assertEquals(BigInteger.valueOf(1L), ConverterRegistry.convert("1", BigInteger.class));
        Assert.assertEquals("1", ConverterRegistry.convert("1", String.class));
        Assert.assertEquals("1", ConverterRegistry.convert("1", Object.class));
        Assert.assertEquals(1.0F, ConverterRegistry.convert("1", float.class));
        Assert.assertEquals(1.0F, ConverterRegistry.convert("1", Float.class));
        Assert.assertEquals(1.0D, ConverterRegistry.convert("1", double.class));
        Assert.assertEquals(1.0D, ConverterRegistry.convert("1", Double.class));
        Object obj = new Object();
        Assert.assertEquals(obj, ConverterRegistry.convert(obj, ConverterRegistry.class));
    }

    @Test
    public void testConvertNumberToTargetClass() {
        Assert.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(1, byte.class));
        Assert.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(1, Byte.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(1, short.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(1, Short.class));
        Assert.assertEquals(1, ConverterRegistry.convert(1, int.class));
        Assert.assertEquals(1, ConverterRegistry.convert(1, Integer.class));
        Assert.assertEquals(1L, ConverterRegistry.convert(1, long.class));
        Assert.assertEquals(1L, ConverterRegistry.convert(1, Long.class));
        Assert.assertEquals(true, ConverterRegistry.convert(1, boolean.class));
        Assert.assertEquals(false, ConverterRegistry.convert(0, Boolean.class));
        Assert.assertEquals(BigInteger.valueOf(1L), ConverterRegistry.convert(1, BigInteger.class));
        Assert.assertEquals("1", ConverterRegistry.convert(1, String.class));
        Assert.assertEquals(1, ConverterRegistry.convert(1, Object.class));
        Assert.assertEquals(1.0F, ConverterRegistry.convert(1, float.class));
        Assert.assertEquals(1.0F, ConverterRegistry.convert(1, Float.class));
        Assert.assertEquals(1.0D, ConverterRegistry.convert(1, double.class));
        Assert.assertEquals(1.0D, ConverterRegistry.convert(1, Double.class));
        Assert.assertEquals(BigInteger.valueOf(1), ConverterRegistry.convert(BigDecimal.valueOf(1), BigInteger.class));
        Assert.assertEquals(1, ConverterRegistry.convert(BigInteger.valueOf(1), Integer.class));
        Assert.assertEquals('a', ConverterRegistry.convert('a', char.class));
        Assert.assertEquals('a', ConverterRegistry.convert('a', Character.class));
    }

    @Test
    public void testOutOfRangeConvertNumberToTargetClass() {
        for (Object[] objs : Arrays.asList(
                new Object[]{Byte.MAX_VALUE + 1, Byte.class},
                new Object[]{Byte.MIN_VALUE - 1, Byte.class},
                new Object[]{Short.MAX_VALUE + 1, Short.class},
                new Object[]{Short.MIN_VALUE - 1, Short.class},
                new Object[]{Integer.MAX_VALUE + 1L, Integer.class},
                new Object[]{Integer.MIN_VALUE - 1L, Integer.class},
                new Object[]{BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), Long.class},
                new Object[]{BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), Long.class}
        )) {
            try {
                Assert.assertEquals(1.0D, ConverterRegistry.convert(objs[0], (Class) objs[1]));
            } catch (Exception e) {
                continue;
            }
            Assert.fail();
        }
    }
}