package org.excel.core;

import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ExcelUserModelWorkerTest {

    @Test
    public void test() throws Exception {
        ExcelUserModelWorker<Animal> worker = new ExcelUserModelWorker<>("classpath:/org/excel/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
    }

    @ExcelSheet(fileName = "bean.xls", sheetName = "Sheet2")
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

    @ExcelSheet(fileName = "bean.xls", sheetName = "Sheet1")
    public static class Animal {

        private int id;

        @ExcelField(name = "name")
        private String name;

        @ExcelField()
        private Double weight;

        @ExcelField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }
}