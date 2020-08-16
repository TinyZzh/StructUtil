package org.struct.core.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author TinyZ.
 * @version 2020.08.17
 */
public class RefFieldBeanTest {

    @Test
    public void test() {
        RefFieldRef ref = new RefFieldRef(1, 998);
        RefFieldBean var0 = new RefFieldBean(1, ref);

        StructWorker<RefFieldBean> worker = new StructWorker<>("classpath:/org/struct/core/", RefFieldBean.class);
        ArrayList<RefFieldBean> list = worker.toList(ArrayList::new);
        RefFieldBean bean = list.get(0);
        Assertions.assertEquals(var0, bean);
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    static class RefFieldBean {

        @StructField(name = "id")
        private int id;

        @StructField(ref = RefFieldRef.class, refUniqueKey = "id")
        private RefFieldRef ref;

        public RefFieldBean() {
        }

        public RefFieldBean(int id, RefFieldRef ref) {
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
    static class RefFieldRef {

        private int id;

        private long value;

        public RefFieldRef() {
        }

        public RefFieldRef(int id, long value) {
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
            RefFieldRef that = (RefFieldRef) o;
            return id == that.id &&
                    value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }


}
