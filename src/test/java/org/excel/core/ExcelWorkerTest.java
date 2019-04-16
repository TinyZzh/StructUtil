package org.excel.core;

import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;

public class ExcelWorkerTest {
    @Test
    public void test() throws Exception {
        //
        URL resource = ExcelWorkerTest.class.getResource("Bean.xlsx");
        final String rootPath = ExcelWorkerTest.class.getResource("/").getPath();
        ExcelWorker<Animal> worker = ExcelWorker.of("classpath:/org/excel/core/", Animal.class);
//        ExcelWorker<Animal> worker = ExcelWorker.of("./out/test/resources/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
        System.out.println();
    }

    @ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet2")
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

    @ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
    public static class Animal {

        private int id;

        private String name;

        private Double weight;

        @ExcelField(ref = Classification.class, refUniqueKey = {"id"})
        private Classification bean;
    }
}