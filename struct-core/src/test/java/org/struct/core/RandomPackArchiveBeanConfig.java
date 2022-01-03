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

import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;

import java.util.List;

@StructSheet(fileName = "tpl_shop_randomPack.xlsx", sheetName = "sheet1", startOrder = 2)
public class RandomPackArchiveBeanConfig {
    private int id;
    private String packName; // 随机礼包名称
    private String description; // 随机礼包描述
    private String icon; // 大厅资源（商城、仓库）
    private String res; // 物品通用资源图标（128*128）
    private boolean auto; // 自动打开

    @StructField(ref = RandomPackItemArchiveBeanConfig.class, refGroupBy = {"id"})
    private List<RandomPackItemArchiveBeanConfig> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public List<RandomPackItemArchiveBeanConfig> getItems() {
        return items;
    }

    public void setItems(List<RandomPackItemArchiveBeanConfig> items) {
        this.items = items;
    }
}
