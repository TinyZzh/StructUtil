package test;

import dev.tinyz.excel2json.annotation.ExcelField;
import dev.tinyz.excel2json.annotation.ExcelSheet;
import dev.tinyz.excel2json.core.ExcelWorker;
import org.junit.Test;

import java.util.ArrayList;

public class BeanUtilTest {

    @Test
    public void test() throws Exception {
        final String rootPath = "./out/test/resources/com/hob/gameserver/excel/";
        ExcelWorker<Animal> worker = new ExcelWorker<>(rootPath, Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
    }

    @ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
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

    @ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet2")
    public static class Animal {

        private int id;

        private String name;

        private Double weight;

        @ExcelField(ref = Classification.class, refUniqueKey = {"id"})
        private Classification bean;
    }

}