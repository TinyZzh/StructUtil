package org.struct.core;

import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ExcelUserModelWorkerTest {

    @Test
    public void test() throws Exception {
        StructWorker<Animal> worker = new StructWorker<>("classpath:/org/struct/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
    }

    @StructSheet(fileName = "bean.xls", sheetName = "Sheet2")
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

    @StructSheet(fileName = "bean.xls", sheetName = "Sheet1")
    public static class Animal {

        private int id;

        @StructField(name = "name")
        private String name;

        @StructField()
        private Double weight;

        @StructField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }
}