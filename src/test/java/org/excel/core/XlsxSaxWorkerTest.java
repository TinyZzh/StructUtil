package org.excel.core;

import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class XlsxSaxWorkerTest {

    @Test
    public void test() throws Exception {
        ExcelWorker<Animal> worker = new XlsxSaxWorker<>("classpath:/org/excel/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
    }

    @Test
    public void testWithEndOrder() throws Exception {
        ExcelWorker<AnimalWithEnd> worker2 = new XlsxSaxWorker<>("classpath:/org/excel/core/", AnimalWithEnd.class);
        ArrayList<AnimalWithEnd> list2 = worker2.load(ArrayList::new);
        Assert.assertTrue(!list2.isEmpty());
        Assert.assertEquals(1, list2.size());
    }

    @Test
    public void testWrongExcelFile() {
        ExcelWorker<WrongExcelFile> worker = new XlsxSaxWorker<>("classpath:/org/excel/core/", WrongExcelFile.class);
        try {
            worker.load(ArrayList::new);
        } catch (Exception e) {
            //  suc
            return;
        }
        Assert.fail();
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

        /**
         * if the field's name is equals column's name, the @ExcelField is not necessary.
         */
        @ExcelField(name = "name")
        private String name;

        @ExcelField()
        private Double weight;

        /**
         * this field required's class is {@link ExcelWorkerTest.Classification}.
         * So the {@link ExcelWorkerTest.Classification}'s excel data will be convert to a temp Map collection.
         * this field's value will be injected from map.
         * the key is {@link ArrayKey} include total refUniqueKey's value .
         */
        @ExcelField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }

    @ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet1", endOrder = 2)
    public static class AnimalWithEnd {
        private int id;

        @ExcelField(name = "name")
        private String name;

        @ExcelField()
        private Double weight;

        @ExcelField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }

    @ExcelSheet(fileName = "WrongExcelFile.xlsx", sheetName = "Sheet1", endOrder = 2)
    public static class WrongExcelFile {

    }
}