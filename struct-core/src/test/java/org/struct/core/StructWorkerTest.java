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

import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author TinyZ.
 * @date 2020-10-09.
 */
class StructWorkerTest {


    @Test
    public void testMapWithGroup() {
        StructWorker<MapWithGroup0> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MapWithGroup0.class);
        ArrayList<MapWithGroup0> list = worker.load(ArrayList::new);
        System.out.println();
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "MapWithGroup0")
    public static class MapWithGroup0 {
        public int id;
        public String str;
        public int group;
        @StructField(ref = MapWithGroup1.class, refGroupBy = "group", refUniqueKey = "vg")
        public Map<Integer, MapWithGroup1> data;
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "MapWithGroup1")
    public static class MapWithGroup1 {
        public String vg;
        public int group;
        public int v;
    }

}