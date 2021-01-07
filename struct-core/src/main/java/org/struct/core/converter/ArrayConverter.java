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

package org.struct.core.converter;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author TinyZ.
 * @version 2020.03.26
 */
public class ArrayConverter implements Converter {

    private static final String DEFAULT_SEPARATOR = "\\|";

    /**
     * The string separator.
     */
    private String separator;
    /**
     * Trim string originValue.
     */
    private boolean strTrim;
    /**
     * Ignore empty string.
     */
    private boolean ignoreBlank;

    public ArrayConverter() {
        this(DEFAULT_SEPARATOR, true);
    }

    public ArrayConverter(String separator, boolean strTrim) {
        this(separator, strTrim, false);
    }

    public ArrayConverter(String separator, boolean strTrim, boolean ignoreBlank) {
        this.separator = separator;
        this.strTrim = strTrim;
        this.ignoreBlank = ignoreBlank;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public boolean isStrTrim() {
        return strTrim;
    }

    public void setStrTrim(boolean strTrim) {
        this.strTrim = strTrim;
    }

    public boolean isIgnoreBlank() {
        return ignoreBlank;
    }

    public void setIgnoreBlank(boolean ignoreBlank) {
        this.ignoreBlank = ignoreBlank;
    }

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        if (!targetType.isArray() || String.class != originValue.getClass()) {
            return null;
        }
        String content = (String) originValue;
        Class<?> componentType = targetType.getComponentType();
        String[] data = content.split(separator);
        if (this.isIgnoreBlank()) {
            data = Arrays.stream(data).filter(s -> !s.isEmpty()).toArray(String[]::new);
        }
        Object array = Array.newInstance(componentType, data.length);
        for (int i = 0; i < data.length; i++) {
            String str = strTrim ? data[i].trim() : data[i];
            Array.set(array, i, ConverterRegistry.convert(str, componentType));
        }
        return array;
    }
}
