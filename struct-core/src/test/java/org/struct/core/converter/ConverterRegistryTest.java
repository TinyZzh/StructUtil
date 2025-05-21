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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ConverterRegistryTest {

    @Test
    public void lookupOrDefault() {
        final int num = 9999;
        Object obj = ConverterRegistry.lookupOrDefault(A.class, MyConverter.class, num)
                .convert(null, "x", int.class);
        Assertions.assertEquals(num, obj);
    }

    @Test
    public void registerConverterClz() {
        final int num = 9999;
        ConverterRegistry.register(A.class, MyConverter.class, num);
        Object obj = ConverterRegistry.lookup(A.class)
                .convert(null, "x", int.class);
        Assertions.assertEquals(num, obj);
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
        public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
            //  do nothing and return val.
            return this.val;
        }
    }

    @Test
    public void registerConverter() {
        String content = "x";
        ConverterRegistry.register(B.class, new Converter() {
            @Override
            public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
                return new B(String.valueOf(originValue));
            }
        });
        B obj = (B) ConverterRegistry.lookup(B.class).convert(null, content, String.class);
        Assertions.assertEquals(content, obj.content);
    }

    public static class B {

        public String content;

        public B(String content) {
            this.content = content;
        }
    }

    @Test
    public void covert() {
        Assertions.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(null, "1", byte.class));
        Assertions.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(null, "1", Byte.class));
        Assertions.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(null, "1", short.class));
        Assertions.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(null, "1", Short.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, "1", int.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, "1", Integer.class));
        Assertions.assertEquals(1L, ConverterRegistry.convert(null, "1", long.class));
        Assertions.assertEquals(1L, ConverterRegistry.convert(null, "1", Long.class));
        Assertions.assertEquals(true, ConverterRegistry.convert(null, "true", boolean.class));
        Assertions.assertEquals(false, ConverterRegistry.convert(null, "false", Boolean.class));
        Assertions.assertEquals(BigInteger.valueOf(1L), ConverterRegistry.convert(null, "1", BigInteger.class));
        Assertions.assertEquals("1", ConverterRegistry.convert(null, "1", String.class));
        Assertions.assertEquals("1", ConverterRegistry.convert(null, "1", Object.class));
        Assertions.assertEquals(1.0F, ConverterRegistry.convert(null, "1", float.class));
        Assertions.assertEquals(1.0F, ConverterRegistry.convert(null, "1", Float.class));
        Assertions.assertEquals(1.0D, ConverterRegistry.convert(null, "1", double.class));
        Assertions.assertEquals(1.0D, ConverterRegistry.convert(null, "1", Double.class));
        Object obj = new Object();
        Assertions.assertEquals(obj, ConverterRegistry.convert(null, obj, ConverterRegistry.class));
    }

    @Test
    public void testConvertNumberToTargetClass() {
        Assertions.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(null, 1, byte.class));
        Assertions.assertEquals(Byte.valueOf("1"), ConverterRegistry.convert(null, 1, Byte.class));
        Assertions.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(null, 1, short.class));
        Assertions.assertEquals(Short.valueOf("1"), ConverterRegistry.convert(null, 1, Short.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, 1, int.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, 1, Integer.class));
        Assertions.assertEquals(1L, ConverterRegistry.convert(null, 1, long.class));
        Assertions.assertEquals(1L, ConverterRegistry.convert(null, 1, Long.class));
        Assertions.assertEquals(true, ConverterRegistry.convert(null, 1, boolean.class));
        Assertions.assertEquals(false, ConverterRegistry.convert(null, 0, Boolean.class));
        Assertions.assertEquals(BigInteger.valueOf(1L), ConverterRegistry.convert(null, 1, BigInteger.class));
        Assertions.assertEquals("1", ConverterRegistry.convert(null, 1, String.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, 1, Object.class));
        Assertions.assertEquals(1.0F, ConverterRegistry.convert(null, 1, float.class));
        Assertions.assertEquals(1.0F, ConverterRegistry.convert(null, 1, Float.class));
        Assertions.assertEquals(1.0D, ConverterRegistry.convert(null, 1, double.class));
        Assertions.assertEquals(1.0D, ConverterRegistry.convert(null, 1, Double.class));
        Assertions.assertEquals(BigInteger.valueOf(1), ConverterRegistry.convert(null, BigDecimal.valueOf(1), BigInteger.class));
        Assertions.assertEquals(1, ConverterRegistry.convert(null, BigInteger.valueOf(1), Integer.class));
        Assertions.assertEquals('a', ConverterRegistry.convert(null, 'a', char.class));
        Assertions.assertEquals('a', ConverterRegistry.convert(null, 'a', Character.class));
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
                Assertions.assertEquals(1.0D, ConverterRegistry.convert(null, objs[0], (Class) objs[1]));
            } catch (Exception e) {
                continue;
            }
            Assertions.fail();
        }
    }

    @Test
    public void covertCollection() {
        List<Integer> list = Arrays.asList(1, 2);
        Assertions.assertEquals(new TreeSet<>(list), ConverterRegistry.convertCollection(null, "1|2", SortedSet.class, int.class));
        Assertions.assertEquals(new HashSet<>(list), ConverterRegistry.convertCollection(null, "1|2", Set.class, int.class));
        Assertions.assertEquals(new HashSet<>(list), ConverterRegistry.convertCollection(null, "1|2", HashSet.class, int.class));
        Assertions.assertEquals(new ArrayList<>(list), ConverterRegistry.convertCollection(null, "1|2", List.class, int.class));
        Assertions.assertEquals(new LinkedList<>(list), ConverterRegistry.convertCollection(null, "1|2", LinkedList.class, int.class));
//        Assertions.assertEquals(new ArrayDeque<>(list), ConverterRegistry.convertCollection("1", ArrayDeque.class, int.class));

        //  constructor params is empty.
        Assertions.assertEquals(new ArrayList<>(list), ConverterRegistry.convertCollection(null, "1|2", MyList.class, int.class));
        Assertions.assertEquals(ArrayList.class, ConverterRegistry.convertCollection(null, "1|2", MyList.class, int.class).getClass());
        //  illegal convert params
        Assertions.assertEquals("1|2", ConverterRegistry.convertCollection(null, "1|2", HashMap.class, int.class));
        Assertions.assertEquals("1|2", ConverterRegistry.convertCollection(null, "1|2", Set.class, ConverterRegistry.class));
    }

    class MyList<E> extends ArrayList<E> {
        public MyList(int initialCapacity) {
            super(initialCapacity);
        }
    }
}