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

package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConfigurationProperties(prefix = StarterConstant.STRUCT_UTIL)
public class StructProperties {

    private boolean structRequiredDefault = false;
    private boolean ignoreEmptyRow = true;
    private ArrayConverterProperties arrayConverter;

    public ArrayConverterProperties getArrayConverter() {
        return arrayConverter;
    }

    public void setArrayConverter(ArrayConverterProperties arrayConverter) {
        this.arrayConverter = arrayConverter;
    }

    public boolean isStructRequiredDefault() {
        return structRequiredDefault;
    }

    public void setStructRequiredDefault(boolean structRequiredDefault) {
        this.structRequiredDefault = structRequiredDefault;
    }

    public boolean isIgnoreEmptyRow() {
        return ignoreEmptyRow;
    }

    public void setIgnoreEmptyRow(boolean ignoreEmptyRow) {
        this.ignoreEmptyRow = ignoreEmptyRow;
    }

    @Override
    public String toString() {
        return "StructProperties{" +
                "structRequiredDefault=" + structRequiredDefault +
                ", ignoreEmptyRow=" + ignoreEmptyRow +
                ", arrayConverter=" + arrayConverter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructProperties that = (StructProperties) o;
        return structRequiredDefault == that.structRequiredDefault && ignoreEmptyRow == that.ignoreEmptyRow && Objects.equals(arrayConverter, that.arrayConverter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structRequiredDefault, ignoreEmptyRow, arrayConverter);
    }

}
