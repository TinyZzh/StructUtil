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

package org.struct.spring.boot.autoconfigure;

import java.util.Objects;

public class ArrayConverterProperties {

    private String stringSeparator = "\\|";
    private boolean stringTrim = true;
    private boolean ignoreBlank = false;

    public String getStringSeparator() {
        return stringSeparator;
    }

    public void setStringSeparator(String stringSeparator) {
        this.stringSeparator = stringSeparator;
    }

    public boolean isStringTrim() {
        return stringTrim;
    }

    public void setStringTrim(boolean stringTrim) {
        this.stringTrim = stringTrim;
    }

    public boolean isIgnoreBlank() {
        return ignoreBlank;
    }

    public void setIgnoreBlank(boolean ignoreBlank) {
        this.ignoreBlank = ignoreBlank;
    }

    @Override
    public String toString() {
        return "ArrayConverterProperties{" +
                "stringSeparator='" + stringSeparator + '\'' +
                ", stringTrim=" + stringTrim +
                ", ignoreBlank=" + ignoreBlank +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayConverterProperties that = (ArrayConverterProperties) o;
        return stringTrim == that.stringTrim && ignoreBlank == that.ignoreBlank && Objects.equals(stringSeparator, that.stringSeparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringSeparator, stringTrim, ignoreBlank);
    }
}
