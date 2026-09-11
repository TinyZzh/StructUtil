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
import org.struct.core.converter.Converter;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;

/**
 * The descriptors' {@code equals} / {@code compareTo} are built out of long short circuiting
 * chains - every single term needs its own pair of objects, otherwise a missing field silently
 * goes unnoticed.
 *
 * @author TinyZ.
 */
class DescriptorEqualityTest {

    static class Bean {
        public String a;
        public String b;
    }

    static class TestFilter extends StructBeanFilter<Object> {
        public TestFilter(Consumer<Object> cellHandler) {
            super(cellHandler);
        }

        @Override
        public boolean test(Object bean) {
            return true;
        }
    }

    //  ------------------------------------------------------------------
    //  SingleFieldDescriptor#equals
    //  ------------------------------------------------------------------

    @Test
    void testSingleFieldDescriptorEquals() throws Exception {
        Field fa = Bean.class.getDeclaredField("a");
        Field fb = Bean.class.getDeclaredField("b");

        SingleFieldDescriptor a = new SingleFieldDescriptor(fa, null);
        //  the identity shortcut
        Assertions.assertTrue(a.equals(a));
        //  null and other types are never equal
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals("x"));

        //  the super part (the name) already differs
        Assertions.assertNotEquals(a, new SingleFieldDescriptor(fb, null));

        //  same field, same everything
        SingleFieldDescriptor b = new SingleFieldDescriptor(fa, null);
        Assertions.assertEquals(a, b);

        //  now flip one field at a time - each one has to break the equality.
        b = new SingleFieldDescriptor(fa, null);
        b.setRequired(!a.isRequired());
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setReference(String.class);
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setRefGroupBy(new String[]{"g"});
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setRefUniqueKey(new String[]{"u"});
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setAggregateBy("agg");
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setAggregateType(String.class);
        Assertions.assertNotEquals(a, b);

        b = new SingleFieldDescriptor(fa, null);
        b.setConverter(mock(Converter.class));
        Assertions.assertNotEquals(a, b);
    }

    //  ------------------------------------------------------------------
    //  StructDescriptor#equals
    //  ------------------------------------------------------------------

    @Test
    void testStructDescriptorEquals() {
        StructDescriptor a = new StructDescriptor("f", "s", 1, 2, FileExtensionMatcher.class, TestFilter.class);

        Assertions.assertTrue(a.equals(a));
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals("x"));
        Assertions.assertEquals(a, new StructDescriptor("f", "s", 1, 2, FileExtensionMatcher.class, TestFilter.class));

        //  flip one field at a time
        Assertions.assertNotEquals(a, new StructDescriptor("f", "s", 9, 2, FileExtensionMatcher.class, TestFilter.class));
        Assertions.assertNotEquals(a, new StructDescriptor("f", "s", 1, 9, FileExtensionMatcher.class, TestFilter.class));
        Assertions.assertNotEquals(a, new StructDescriptor("F", "s", 1, 2, FileExtensionMatcher.class, TestFilter.class));
        Assertions.assertNotEquals(a, new StructDescriptor("f", "S", 1, 2, FileExtensionMatcher.class, TestFilter.class));
        Assertions.assertNotEquals(a, new StructDescriptor("f", "s", 1, 2, null, TestFilter.class));
        Assertions.assertNotEquals(a, new StructDescriptor("f", "s", 1, 2, FileExtensionMatcher.class, null));

        Assertions.assertEquals(a.hashCode(),
                new StructDescriptor("f", "s", 1, 2, FileExtensionMatcher.class, TestFilter.class).hashCode());
    }

    //  ------------------------------------------------------------------
    //  FieldDescriptor#compareTo
    //  ------------------------------------------------------------------

    @Test
    void testCompareTo() throws Exception {
        Field fa = Bean.class.getDeclaredField("a");
        Field fb = Bean.class.getDeclaredField("b");

        //  neither is a reference field -> the name decides
        SingleFieldDescriptor plain0 = new SingleFieldDescriptor(fa, null);
        SingleFieldDescriptor plain1 = new SingleFieldDescriptor(fb, null);
        Assertions.assertEquals("a".compareTo("b"), plain0.compareTo(plain1));

        //  only one of them is a reference field
        SingleFieldDescriptor ref = new SingleFieldDescriptor(fa, null);
        ref.setReference(String.class);
        Assertions.assertTrue(ref.compareTo(plain1) > 0);
        Assertions.assertTrue(plain1.compareTo(ref) < 0);

        //  both are reference fields, neither has a converter -> the name decides
        SingleFieldDescriptor ref1 = new SingleFieldDescriptor(fb, null);
        ref1.setReference(String.class);
        Assertions.assertEquals("a".compareTo("b"), ref.compareTo(ref1));

        //  both are reference fields, only one has a converter
        Converter converter = mock(Converter.class);
        ref.setConverter(converter);
        Assertions.assertTrue(ref.compareTo(ref1) > 0);
        Assertions.assertTrue(ref1.compareTo(ref) < 0);

        //  both have a converter
        ref1.setConverter(converter);
        Assertions.assertTrue(ref.compareTo(ref1) != 0);
    }

    /**
     * {@code OptionalDescriptor} sorts after the plain fields.
     */
    @Test
    void testCompareToOptional() throws Exception {
        Field fa = Bean.class.getDeclaredField("a");
        SingleFieldDescriptor plain = new SingleFieldDescriptor(fa, null);

        OptionalDescriptor opt = new OptionalDescriptor();
        opt.setName("a");

        Assertions.assertTrue(opt.compareTo(plain) > 0);
        Assertions.assertTrue(plain.compareTo(opt) < 0);
        Assertions.assertEquals("a".compareTo("z"), opt.compareTo(withName("z")));
    }

    private static OptionalDescriptor withName(String name) {
        OptionalDescriptor od = new OptionalDescriptor();
        od.setName(name);
        return od;
    }

    @Test
    void testOptionalDescriptorEquals() {
        OptionalDescriptor a = new OptionalDescriptor();
        a.setName("a");
        Assertions.assertTrue(a.equals(a));
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals("x"));
        Assertions.assertEquals(a, withName("a"));
        Assertions.assertNotEquals(a, withName("b"));

        //  the super part already differs
        OptionalDescriptor other = new OptionalDescriptor();
        other.setName("b");
        Assertions.assertNotEquals(a, other);

        //  the nested descriptors take part too
        OptionalDescriptor c = withName("a");
        c.setDescriptors(new SingleFieldDescriptor[]{new SingleFieldDescriptor()});
        Assertions.assertNotEquals(a, c);
    }
}
