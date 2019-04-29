/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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

package org.excel.core;

import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author TinyZ.
 * @version 2019.04.29
 */
public class XlsEventWorkerTest {

    @Test
    public void test() throws Exception {
        XlsEventWorker<Animal> worker = new XlsEventWorker<>("classpath:/org/excel/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testWithEndOrder() throws Exception {
        XlsEventWorker<AnimalWithEnd> worker = new XlsEventWorker<>("classpath:/org/excel/core/", AnimalWithEnd.class);
        ArrayList<AnimalWithEnd> list = worker.load(ArrayList::new);
        Assert.assertTrue(!list.isEmpty());
        Assert.assertEquals(1, list.size());
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

    @ExcelSheet(fileName = "bean.xls", sheetName = "Sheet1", endOrder = 2)
    public static class AnimalWithEnd {
        private int id;

        @ExcelField(name = "name")
        private String name;

        @ExcelField()
        private Double weight;

        @ExcelField(ref = ExcelWorkerTest.Classification.class, refUniqueKey = {"id"})
        private ExcelWorkerTest.Classification bean;
    }
}