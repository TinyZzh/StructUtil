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

package org.struct.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.annotation.StructSheet;
import org.struct.core.converter.ConvertContext;
import org.struct.core.converter.Converter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectsTest {

    @Test
    public void testNewInstance() {
        //  [fail] nested class
        try {
            Reflects.newInstance(Apple.class);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [suc] static class
        StaticApple apple = Reflects.newInstance(StaticApple.class);
        Assertions.assertNotNull(apple);
        //  [suc] parameter class
        Pyrus pyrus = Reflects.newInstance(Pyrus.class, 10);
        Assertions.assertNotNull(pyrus);
        //  [fail] wrong parameter count
        try {
            Reflects.newInstance(Pyrus.class, 10, 1000);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [fail] wrong parameter type
        try {
            Reflects.newInstance(Pyrus.class, "11");
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [suc]
        Assertions.assertNotNull(Reflects.newInstance(Orange.class));
        //  [fail]  constructor throw exception
        Assertions.assertThrows(IllegalStateException.class, () -> Reflects.newInstance(OnlyException.class));
        //  [fail]  private constructor throw exception
        Assertions.assertThrows(IllegalStateException.class, () -> Reflects.newInstance(OnlyPriParamException.class, "xx"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Reflects.newInstance(OnlyPriParamException.class, new double[]{1.0D}));
    }

    @Test
    public void testToClass() {
        Assertions.assertNull(Reflects.toClass(null));
        Assertions.assertEquals(Reflects.EMPTY_CLASS_ARRAY, Reflects.toClass(new Class[0]));
        Assertions.assertArrayEquals(new Class[]{Integer.class, Boolean.class}, Reflects.toClass(1, false));
        Assertions.assertArrayEquals(new Class[]{Integer.class, null}, Reflects.toClass(2, null));
    }

    public class Apple {
    }

    public static class StaticApple {
    }

    public static class Pyrus {
        private Integer weight;

        public Pyrus(Integer weight) {
            this.weight = weight;
        }
    }

    public static class Orange {
        private Orange() {
        }
    }

    public static class OnlyException {
        public OnlyException() throws Exception {
            throw new Exception();
        }
    }

    public static class OnlyPriException {
        private OnlyPriException() throws Exception {
            throw new Exception();
        }
    }

    public static class OnlyPriParamException {
        private String str;

        private OnlyPriParamException(String str) throws Exception {
            this.str = str;
            throw new Exception();
        }
    }


    public static class Fruit2 {

        private String name;

    }

    public static class Apple2 extends Fruit2 {

        private int weight;
        public int quality;
        protected double price;
    }

    @Test
    public void resolveAllFields() {
        Field[] fields = Apple2.class.getFields();
        Assertions.assertEquals(1, fields.length);
        Field[] fields2 = Apple2.class.getDeclaredFields();
        Assertions.assertEquals(3, fields2.length);
        List<Field> fields3 = Reflects.resolveAllFields(Apple2.class, true);
        Assertions.assertEquals(4, fields3.size());
    }

    @Test
    public void testLookupAccessor() throws Throwable {
        Object[] values = new Object[]{1, "2"};
        BeanClz bean = new BeanClz(1, "2");
        Assertions.assertEquals(values[0], Reflects.lookupAccessor(BeanClz.class, "id").invoke(bean));
        Assertions.assertEquals(values[1], Reflects.lookupAccessor(BeanClz.class, "name").invoke(bean));
        Assertions.assertNull(Reflects.lookupAccessor(BeanClz.class, "nameX"));

        RecordClz record = new RecordClz(1, "2");
        Assertions.assertEquals(values[0], Reflects.lookupAccessor(RecordClz.class, "id").invoke(record));
        Assertions.assertEquals(values[1], Reflects.lookupAccessor(RecordClz.class, "name").invoke(record));
        Assertions.assertNull(Reflects.lookupAccessor(RecordClz.class, "nameX"));

        Assertions.assertThrows(AssertionError.class, () -> Reflects.lookupAccessor(RecordClz.class, null));
        Assertions.assertThrows(AssertionError.class, () -> Reflects.lookupAccessor(RecordClz.class, ""));
    }

    @Test
    public void testResolveAllFields() {
        //  Class
        {
            List<Field> fields = Reflects.resolveAllFields(BeanClz.class);
            Assertions.assertEquals(2, fields.size());
            Assertions.assertEquals("id", fields.get(0).getName());
            Assertions.assertEquals("name", fields.get(1).getName());
        }
        //  Parent Class
        {
            List<Field> fields = Reflects.resolveAllFields(SubBeanClz.class);
            Assertions.assertEquals(5, fields.size());
            Assertions.assertEquals("age", fields.get(0).getName());
            Assertions.assertEquals("name", fields.get(1).getName());
            Assertions.assertEquals("publicField", fields.get(2).getName());
            //  parent fields
            Assertions.assertEquals("id", fields.get(3).getName());
            Assertions.assertEquals("name", fields.get(4).getName());
        }
        //  Record
        {
            List<Field> fields = Reflects.resolveAllFields(RecordClz.class);
            Assertions.assertEquals(2, fields.size());
            Assertions.assertEquals("id", fields.get(0).getName());
            Assertions.assertEquals("name", fields.get(1).getName());
        }
    }

    @Test
    public void testResolveAllFieldsUnDeclared() {
        Assertions.assertTrue(Reflects.resolveAllFields(BeanClz.class, false).isEmpty());
        Assertions.assertEquals(1, Reflects.resolveAllFields(SubBeanClz.class, false).size());
        Assertions.assertTrue(Reflects.resolveAllFields(RecordClz.class, false).isEmpty());
    }

    @Test
    public void testIsAssignable() {
        Assertions.assertTrue(Reflects.isAssignable(Object.class, BeanClz.class));
        Assertions.assertFalse(Reflects.isAssignable(BeanClz.class, Object.class));
        Assertions.assertTrue(Reflects.isAssignable(int.class, Integer.class));
        Assertions.assertFalse(Reflects.isAssignable(int.class, Long.class));
        Assertions.assertTrue(Reflects.isAssignable(Integer.class, int.class));
        Assertions.assertFalse(Reflects.isAssignable(Integer.class, String.class));
    }

    static class BeanClz {
        private int id;
        private String name;

        public BeanClz(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    record RecordClz(int id, String name) {
    }

    static class SubBeanClz extends BeanClz {
        private int age;
        private String name;

        public float publicField;

        public SubBeanClz(int id, String name, int age, String name1) {
            super(id, name);
            this.age = age;
            this.name = name1;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    public void testLookupFieldSetter() {
        Assertions.assertThrows(IllegalStateException.class, () -> Reflects.lookupFieldSetter(RecordClz.class, "id"));
        Assertions.assertThrows(AssertionError.class, () -> Reflects.lookupFieldSetter(RecordClz.class, ""));
        Optional<MethodHandle> optional = Reflects.lookupFieldSetter(SubBeanClz.class, "id");
        Assertions.assertTrue(optional.isPresent());
        Optional<MethodHandle> optional2 = Reflects.lookupFieldSetter(SubBeanClz.class, "idxxx");
        Assertions.assertFalse(optional2.isPresent());
    }

    @Test
    public void testRecordFileName() {
        List<String> list = Reflects.resolveStructRelatedFileName(RecordAggregateByListKey.class);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(list.contains("file1.xlsx"));
        Assertions.assertTrue(list.contains("file2.csv"));
        list = Reflects.resolveStructRelatedFileName(ClassAggregateByListKey.class);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(list.contains("file1.xlsx"));
        Assertions.assertTrue(list.contains("file2.csv"));
    }

    @StructSheet(fileName = "file1.xlsx", sheetName = "Sheet1")
    class ClassAggregateByListKey {
        static final int id = 1;
        @StructField(name = "ids", converter = ListIntConverter.class)
        private List<Integer> idList;
        @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
        private RecordAggregateTargetBean[] beansAry;
        @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
        private List<RecordAggregateTargetBean> beansList;
        @StructOptional(value = {
                @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids"),
                @StructField(ref = ClassAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
        })
        private RecordAggregateTargetBean var0;
        @StructField(ref = ClassEmptyFile.class, refUniqueKey = "id")
        private ClassEmptyFile var1;
    }

    @StructSheet(fileName = "file2.csv", sheetName = "Sheet1")
    record RecordAggregateByListKey(int id,
                                    @StructField(name = "ids", converter = ListIntConverter.class)
                                    List<Integer> idList,
                                    @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
                                    RecordAggregateTargetBean[] beansAry,
                                    @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
                                    List<RecordAggregateTargetBean> beansList,
                                    @StructOptional(value = {
                                            @StructField(ref = RecordAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids"),
                                            @StructField(ref = ClassAggregateTargetBean.class, refUniqueKey = "id", aggregateBy = "ids")
                                    })
                                    RecordAggregateTargetBean var0,
                                    @StructField(ref = ClassEmptyFile.class, refUniqueKey = "id")
                                    ClassEmptyFile var1
    ) {
    }

    @StructSheet(fileName = "file1.xlsx", sheetName = "Sheet2")
    record RecordAggregateTargetBean(int id,
                                     String domain,
                                     String phylum,
                                     String clazz,
                                     String order,
                                     String family,
                                     String genus,
                                     String species) {
    }

    @StructSheet(fileName = "file2.csv", sheetName = "Sheet1")
    class ClassAggregateTargetBean {
    }

    @StructSheet(fileName = "")
    class ClassEmptyFile {
    }

    static class ListIntConverter implements Converter {

        @Override
        public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
            if (originValue instanceof String s) {
                return Arrays.stream(s.split("\\|")).map(Integer::parseInt).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
    }
}