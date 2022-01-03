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

package org.struct.examples;

/**
 * @author TinyZ.
 * @date 2020-07-22.
 */
public class DataInfo {

    private int id;
    private String name;
    private Double weight;

    public int getId() {
        return id;
    }

    public DataInfo setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DataInfo setName(String name) {
        this.name = name;
        return this;
    }

    public Double getWeight() {
        return weight;
    }

    public DataInfo setWeight(Double weight) {
        this.weight = weight;
        return this;
    }
}
