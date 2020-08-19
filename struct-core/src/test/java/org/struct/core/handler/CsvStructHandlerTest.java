package org.struct.core.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author TinyZ.
 * @version 2020.08.19
 */
class CsvStructHandlerTest {

    @Test
    public void test() {
        CsvFileBean var0 = new CsvFileBean(1, "n1");

        StructWorker<CsvFileBean> worker = new StructWorker<>("classpath:/org/struct/core/", CsvFileBean.class);
        ArrayList<CsvFileBean> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(5, list.size());
        Assertions.assertTrue(list.contains(var0));
    }

    @Test
    public void testStartOrder3() {
        CsvFileBeanStartOrder3 var0 = new CsvFileBeanStartOrder3(1, "n1");
        CsvFileBeanStartOrder3 var3 = new CsvFileBeanStartOrder3(3, "n3");

        StructWorker<CsvFileBeanStartOrder3> worker = new StructWorker<>("classpath:/org/struct/core/", CsvFileBeanStartOrder3.class);
        ArrayList<CsvFileBeanStartOrder3> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, list.size());
        Assertions.assertFalse(list.contains(var0));
        Assertions.assertTrue(list.contains(var3));
    }

    @Test
    public void testStartOrder3AndEndOrder4() {
        CsvFileBeanStartOrder3AndEnd4 var0 = new CsvFileBeanStartOrder3AndEnd4(1, "n1");
        CsvFileBeanStartOrder3AndEnd4 var3 = new CsvFileBeanStartOrder3AndEnd4(3, "n3");

        StructWorker<CsvFileBeanStartOrder3AndEnd4> worker = new StructWorker<>("classpath:/org/struct/core/", CsvFileBeanStartOrder3AndEnd4.class);
        ArrayList<CsvFileBeanStartOrder3AndEnd4> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, list.size());
        Assertions.assertFalse(list.contains(var0));
        Assertions.assertTrue(list.contains(var3));
    }

    @StructSheet(fileName = "examples.csv")
    static class CsvFileBean extends Bean {

        public CsvFileBean() {
        }

        public CsvFileBean(int id, String name) {
            super(id, name);
        }
    }

    @StructSheet(fileName = "examples.csv", startOrder = 3)
    static class CsvFileBeanStartOrder3 extends Bean {
        public CsvFileBeanStartOrder3() {
        }

        public CsvFileBeanStartOrder3(int id, String name) {
            super(id, name);
        }
    }

    @StructSheet(fileName = "examples.csv", startOrder = 3, endOrder = 4)
    static class CsvFileBeanStartOrder3AndEnd4 extends Bean {
        public CsvFileBeanStartOrder3AndEnd4() {
        }

        public CsvFileBeanStartOrder3AndEnd4(int id, String name) {
            super(id, name);
        }
    }

    static class Bean {

        private int id;
        private String name;

        public Bean() {
        }

        public Bean(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "CsvFileBean{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bean that = (Bean) o;
            return id == that.id &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

}

