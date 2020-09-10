/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

/**
 * Struct global configuration.
 */
public class StructConfig {

    /**
     * {@link org.struct.core.converter.ArrayConverter}
     */
    private String arrayConverterStringSeparator = "\\|";
    /**
     *
     */
    private boolean arrayConverterStringTrim = true;
    /**
     * ignore blank string.
     */
    private boolean arrayConverterIgnoreBlank = false;

    /**
     * The default {@link StructField#required()}'s value.
     */
    private boolean structRequiredDefault = false;

    /**
     * Ignore row if this row's all cell is blank.
     */
    private boolean ignoreEmptyRow = true;


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

    public String getArrayConverterStringSeparator() {
        return arrayConverterStringSeparator;
    }

    public void setArrayConverterStringSeparator(String arrayConverterStringSeparator) {
        this.arrayConverterStringSeparator = arrayConverterStringSeparator;
    }

    public boolean isArrayConverterStringTrim() {
        return arrayConverterStringTrim;
    }

    public void setArrayConverterStringTrim(boolean arrayConverterStringTrim) {
        this.arrayConverterStringTrim = arrayConverterStringTrim;
    }

    public boolean isArrayConverterIgnoreBlank() {
        return arrayConverterIgnoreBlank;
    }

    public void setArrayConverterIgnoreBlank(boolean arrayConverterIgnoreBlank) {
        this.arrayConverterIgnoreBlank = arrayConverterIgnoreBlank;
    }
}
