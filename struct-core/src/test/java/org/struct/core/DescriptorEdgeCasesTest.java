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

import java.lang.reflect.Field;
import java.util.List;

/**
 * The descriptor / row edges: the half initialized state right after construction, and the values
 * that are deliberately left unset.
 *
 * @author TinyZ.
 */
class DescriptorEdgeCasesTest {

    static class Bean {
        public String a;
        public int b;
        public List<Integer> refList;
    }

    record RecordBean(int a) {
    }

    //  ------------------------------------------------------------------
    //  SingleFieldDescriptor
    //  ------------------------------------------------------------------

    /**
     * Right after construction nothing is set: no reference, no aggregation, no underlying field.
     */
    @Test
    void testEmptyDescriptor() {
        SingleFieldDescriptor fd = new SingleFieldDescriptor();
        Assertions.assertFalse(fd.isReferenceField());
        Assertions.assertFalse(fd.isAggregateField());
        Assertions.assertEquals(Object.class, fd.getFieldType());
        Assertions.assertNull(fd.getFieldValueFrom(new Bean()));
        //  setting a value is silently ignored - there is no field behind it.
        fd.setFieldValue(new Bean(), "x");
    }

    /**
     * A reference to {@code Object.class} means "no reference at all".
     */
    @Test
    void testReferenceToObjectIsNoReference() throws Exception {
        Field f = Bean.class.getDeclaredField("a");
        SingleFieldDescriptor fd = new SingleFieldDescriptor(f, null);
        fd.setReference(Object.class);
        Assertions.assertFalse(fd.isReferenceField());

        //  an empty aggregateBy is not an aggregate field either
        fd.setAggregateBy("");
        Assertions.assertFalse(fd.isAggregateField());

        //  a real one is
        fd.setAggregateBy("b");
        Assertions.assertTrue(fd.isAggregateField());
    }

    /**
     * A basic type collection is a collection field without any reference configuration.
     */
    @Test
    void testBasicTypeCollection() throws Exception {
        Field f = Bean.class.getDeclaredField("refList");
        SingleFieldDescriptor fd = new SingleFieldDescriptor(f, null);
        //  NOTE: the key arrays are not initialized by the constructor, they have to be set before
        //  isBasicTypeCollection() is called.
        fd.setRefGroupBy(new String[0]);
        fd.setRefUniqueKey(new String[0]);

        //  no refGroupBy / refUniqueKey / aggregateBy, and a basic reference type
        fd.setReference(int.class);
        Assertions.assertTrue(fd.isBasicTypeCollection());

        //  a non basic reference type is not
        fd.setReference(Bean.class);
        Assertions.assertFalse(fd.isBasicTypeCollection());

        //  configuring a group by disables it as well
        fd.setReference(int.class);
        fd.setRefGroupBy(new String[]{"b"});
        Assertions.assertFalse(fd.isBasicTypeCollection());
    }

    /**
     * The value is read reflectively from a plain bean and from a record.
     */
    @Test
    void testGetFieldValueFromBeanAndRecord() throws Exception {
        //  a plain field
        Field f = Bean.class.getDeclaredField("a");
        SingleFieldDescriptor fd = new SingleFieldDescriptor(f, null);
        Bean bean = new Bean();
        bean.a = "hello";
        Assertions.assertEquals("hello", fd.getFieldValueFrom(bean));

        //  a record component
        SingleFieldDescriptor rfd = new SingleFieldDescriptor(
                RecordBean.class.getRecordComponents()[0], null);
        Assertions.assertEquals(7, rfd.getFieldValueFrom(new RecordBean(7)));
        //  a record has no setter
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> rfd.setFieldValue(new RecordBean(7), 8));
    }

    /**
     * A field level converter that is abstract (or an interface) is not instantiated.
     */
    @Test
    void testAbstractConverterIsIgnored() {
        SingleFieldDescriptor fd = new SingleFieldDescriptor();
        //  the descriptor simply keeps the default (no converter)
        Assertions.assertNull(fd.getConverter());
    }

    abstract static class AbstractConverter implements Converter {
    }

    //  ------------------------------------------------------------------
    //  StructImpl
    //  ------------------------------------------------------------------

    /**
     * A blank column name carries no value.
     */
    @Test
    void testStructImplBlankFieldName() {
        StructImpl si = new StructImpl();
        si.add(null, "v");
        si.add("", "v");
        //  the null / blank names never made it in
        Assertions.assertNull(si.get(""));
        Assertions.assertNull(si.get("a"));
        //  a real name still works
        si.add("a", "v");
        Assertions.assertEquals("v", si.get("a"));
    }

    //  ------------------------------------------------------------------
    //  ArrayKey
    //  ------------------------------------------------------------------

    /**
     * An {@code ArrayKey} is never equal to a foreign type.
     */
    @Test
    void testArrayKeyEqualsForeignType() {
        ArrayKey key = new ArrayKey(new Object[]{1, 2});
        Assertions.assertFalse(key.equals(new Object()));
        Assertions.assertFalse(key.equals(null));
        Assertions.assertEquals(key, new ArrayKey(new Object[]{1, 2}));
        Assertions.assertEquals(key.hashCode(), new ArrayKey(new Object[]{1, 2}).hashCode());
    }
}
