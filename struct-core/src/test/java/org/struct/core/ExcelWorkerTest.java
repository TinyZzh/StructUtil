package org.struct.core;

import org.junit.Assert;
import org.junit.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.util.WorkerUtil;

import java.net.URL;
import java.util.ArrayList;

public class ExcelWorkerTest {

    @Test
    public void test() throws Exception {
        //
        URL resource = ExcelWorkerTest.class.getResource("Bean.xlsx");
        final String rootPath = ExcelWorkerTest.class.getResource("/").getPath();
        StructWorker<Animal> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", Animal.class);
//        StructWorker<Animal> worker = StructWorker.of("./out/test/resources/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testAnimalWithArray() throws Exception {
        StructWorker<AnimalWithArray> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", AnimalWithArray.class);
        ArrayList<AnimalWithArray> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
    }

    @Test
    public void testAnimalWithUnknownField() throws Exception {
        StructWorker<AnimalWithUnknownField> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", AnimalWithUnknownField.class);
        try {
            ArrayList<AnimalWithUnknownField> list = worker.load(ArrayList::new);
        } catch (Exception e) {
            //  no-op
            e.printStackTrace();
            return;
        }
        Assert.fail();
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "Sheet2")
    public static class Classification {
        private int id;
        private String domain;
        private String phylum;
        private String clazz;
        private String order;
        private String family;
        private String genus;
        private String species;
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
    public static class Animal {

        private int id;

        /**
         * if the field's name is equals column's name, the @StructField is not necessary.
         */
        @StructField(name = "name")
        private String name;

        @StructField()
        private Double weight;

        /**
         * this field required's class is {@link Classification}.
         * So the {@link Classification}'s struct data will be convert to a temp Map collection.
         * this field's value will be injected from map.
         * the key is {@link ArrayKey} include total refUniqueKey's value .
         */
        @StructField(ref = Classification.class, refUniqueKey = {"id"})
        private Classification bean;
    }


    @StructSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
    public static class AnimalWithArray {
        private int id;
        @StructField(name = "name")
        private String name;
        @StructField()
        private Double weight;
        @StructField(ref = Classification.class, refGroupBy = {"id"}, refUniqueKey = {"id"})
        private Classification[] beans;
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
    public static class AnimalWithUnknownField {
        private int id;
        @StructField(name = "name")
        private String name;
        @StructField()
        private Double weight;
        @StructField(ref = Classification.class, refUniqueKey = {"unknownRefField"})
        private Classification[] beans;
    }
}