/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;

import java.util.ArrayList;

public class XlsxSaxWorkerTest {

    @Test
    public void test() throws Exception {
        StructWorker<Animal> worker = new StructWorker<>("classpath:/org/struct/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assertions.assertTrue(!list.isEmpty());
        Assertions.assertEquals(10, list.size());
    }

    @Test
    public void testWithEndOrder() throws Exception {
        StructWorker<AnimalWithEnd> worker2 = new StructWorker<>("classpath:/org/struct/core/", AnimalWithEnd.class);
        ArrayList<AnimalWithEnd> list2 = worker2.load(ArrayList::new);
        Assertions.assertTrue(!list2.isEmpty());
        Assertions.assertEquals(1, list2.size());
    }

    @Test
    public void testWrongExcelFile() {
        StructWorker<WrongExcelFile> worker = new StructWorker<>("classpath:/org/struct/core/", WrongExcelFile.class);
        try {
            worker.load(ArrayList::new);
        } catch (Exception e) {
            //  suc
            return;
        }
        Assertions.fail();
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
         * this field required's class is {@link ExcelWorkerTest.Classification}.
         * So the {@link ExcelWorkerTest.Classification}'s struct data will be convert to a temp Map collection.
         * this field's value will be injected from map.
         * the key is {@link ArrayKey} include total refUniqueKey's value .
         */
        @StructField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "Sheet1", endOrder = 2)
    public static class AnimalWithEnd {
        private int id;

        @StructField(name = "name")
        private String name;

        @StructField()
        private Double weight;

        @StructField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }

    @StructSheet(fileName = "WrongExcelFile.xlsx", sheetName = "Sheet1", endOrder = 2)
    public static class WrongExcelFile {

    }
}