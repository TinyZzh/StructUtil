/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

public class ExcelUserModelWorkerTest {

    @Test
    public void test() throws Exception {
        StructWorker<Animal> worker = new StructWorker<>("classpath:/org/struct/core/", Animal.class);
        ArrayList<Animal> list = worker.load(ArrayList::new);
        Assertions.assertFalse(list.isEmpty());
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