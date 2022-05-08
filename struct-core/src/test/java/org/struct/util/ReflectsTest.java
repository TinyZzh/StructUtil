/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

import java.lang.reflect.Field;
import java.util.List;

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

}