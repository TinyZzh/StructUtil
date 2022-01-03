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

package org.struct.core.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author TinyZ.
 * @version 2020.08.17
 */
public class OptionalFieldBeanTest {

    @Test
    public void test() {
        RefFieldBean var0 = new RefFieldBean(1, new RefFieldRef1(1, 998));
        RefFieldBean var1 = new RefFieldBean(2, new RefFieldRef2(2, 997));

        StructWorker<RefFieldBean> worker = new StructWorker<>("classpath:/org/struct/core/", RefFieldBean.class);
        ArrayList<RefFieldBean> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(var0, list.get(0));
        Assertions.assertEquals(var1, list.get(1));
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    static class RefFieldBean {

        @StructField(name = "id")
        private int id;

        @StructOptional(value = {
                @StructField(ref = RefFieldRef1.class, refUniqueKey = "id"),
                @StructField(ref = RefFieldRef2.class, refUniqueKey = "id")
        })
        private Object ref;

        public RefFieldBean() {
        }

        public RefFieldBean(int id, Object ref) {
            this.id = id;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return "RefFieldBean{" +
                    "id=" + id +
                    ", ref=" + ref +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RefFieldBean that = (RefFieldBean) o;
            return id == that.id &&
                    Objects.equals(ref, that.ref);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, ref);
        }
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_2")
    static class RefFieldRef1 {

        private int id;

        private long value;

        public RefFieldRef1() {
        }

        public RefFieldRef1(int id, long value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String toString() {
            return "RefFieldRef{" +
                    "id=" + id +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RefFieldRef1 that = (RefFieldRef1) o;
            return id == that.id &&
                    value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_3")
    static class RefFieldRef2 {

        private int id;

        private long value;

        public RefFieldRef2() {
        }

        public RefFieldRef2(int id, long value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String toString() {
            return "RefFieldRef2{" +
                    "id=" + id +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RefFieldRef2 that = (RefFieldRef2) o;
            return id == that.id &&
                    value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }


}
