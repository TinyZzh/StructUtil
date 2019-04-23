package org.excel.core;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.excel.util.ExcelUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ExcelWorkerTest {

    @Test
    public void streamLoad() throws InvalidFormatException, InterruptedException {
        Thread.sleep(10000L);
        String filePath = ExcelUserModelWorker.class.getResource("./Bean.xlsx").getPath();
        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(pkg);


//            InputStream stream = reader.getSheet("Sheet2");

            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
            XMLReader parser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(styles, sharedStrings, new MySheetContentsHandler(), true);
            parser.setContentHandler(handler);

            Iterator<InputStream> it = reader.getSheetsData();
            if (it instanceof XSSFReader.SheetIterator) {
                while (it.hasNext()) {
                    InputStream inputStream = it.next();
                    if (((XSSFReader.SheetIterator) it).getSheetName().equalsIgnoreCase("Sheet3")) {
                        parser.parse(new InputSource(inputStream));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(60000L);
    }

    public static class MySheetContentsHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        @Override
        public void startRow(int rowNum) {
            System.out.println("row:" + rowNum);
        }

        @Override
        public void endRow(int rowNum) {
//            System.out.println();
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            System.out.println();
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
//            System.out.println();
        }
    }

    @Test
    public void test() throws Exception {
        //
        URL resource = ExcelWorkerTest.class.getResource("Bean.xlsx");
        final String rootPath = ExcelWorkerTest.class.getResource("/").getPath();
        ExcelWorker<Animal> worker = ExcelUtil.newWorker("classpath:/org/excel/core/", Animal.class);
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

        /**
         * if the field's name is equals column's name, the @ExcelField is not necessary.
         */
        @ExcelField(name = "name")
        private String name;

        @ExcelField()
        private Double weight;

        /**
         * this field required's class is {@link Classification}.
         * So the {@link Classification}'s excel data will be convert to a temp Map collection.
         * this field's value will be injected from map.
         * the key is {@link ArrayKey} include total refUniqueKey's value .
         */
        @ExcelField(ref = Classification.class, refUniqueKey = {"id"})
        private Classification bean;
    }
}