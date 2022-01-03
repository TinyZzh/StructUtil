/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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
import java.util.List;

public class CfgItemTest {

    @Test
    public void test() {
        StructWorker<RandomPackArchiveBeanConfig> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", RandomPackArchiveBeanConfig.class);
        ArrayList<RandomPackArchiveBeanConfig> items = worker.load(ArrayList::new);

        System.out.println();
    }


    @StructSheet(fileName = "item.xlsx", sheetName = "Data_道具表", startOrder = 3)
    public static class CfgItem {
        private int id;
        private String name;
        private boolean auto;
        private boolean expendable;
        private String numType;
        private int type;
        private int icon;
        private String comment;
        private String description;
        private String script;
        private int num;
        private int stack;
        private int permanent;
        private int xlv;

        @StructField(name = "items", ref = CfgPackItem.class, refGroupBy = "packId")
        private List<CfgPackItem> items;


    }

    @StructSheet(fileName = "cfg_pack.xlsx", sheetName = "Data_道具表", startOrder = 3)
    public static class CfgPackItem {



    }

}
