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

package org.struct.core.converter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import static org.struct.core.StructInternal.ARRAY_CONVERTER_IGNORE_BLANK;
import static org.struct.core.StructInternal.ARRAY_CONVERTER_STRING_SEPARATOR;
import static org.struct.core.StructInternal.ARRAY_CONVERTER_STRING_TRIM;

/**
 * @author TinyZ.
 * @version 2020.03.26
 */
public class ArrayConverter implements Converter {

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
        this(ARRAY_CONVERTER_STRING_SEPARATOR, ARRAY_CONVERTER_STRING_TRIM, ARRAY_CONVERTER_IGNORE_BLANK);
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
    public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
        if (!targetType.isArray()) {
            //  this converter handles array target types only.
            //  NOTE: previously this fell through to Array.newInstance(null, 0) -> NPE.
            return null;
        }
        Class<?> componentType = targetType.getComponentType();
        if (originValue == null) {
            //  keep the original behaviour: an absent value becomes an EMPTY array.
            //  this matches the framework convention of returning the type's zero
            //  value for a null input (numbers return 0 / 0.0, BigDecimal ZERO...).
            return Array.newInstance(componentType, 0);
        }
        //  the origin value already is structured (e.g. a json array), use it as is.
        if (originValue instanceof Collection<?>) {
            return this.toArray(ctx, ((Collection<?>) originValue).toArray(), componentType);
        }
        if (originValue.getClass().isArray()) {
            int length = Array.getLength(originValue);
            Object[] data = new Object[length];
            for (int i = 0; i < length; i++) {
                data[i] = Array.get(originValue, i);
            }
            return this.toArray(ctx, data, componentType);
        }
        String content = String.valueOf(originValue);
        String[] data = content.split(separator);
        if (this.isIgnoreBlank()) {
            data = Arrays.stream(data).filter(s -> !s.isEmpty()).toArray(String[]::new);
        }
        if (this.isStrTrim()) {
            data = Arrays.stream(data).map(String::trim).toArray(String[]::new);
        }
        return this.toArray(ctx, data, componentType);
    }

    private Object toArray(ConvertContext ctx, Object[] data, Class<?> componentType) {
        Object array = Array.newInstance(componentType, data.length);
        for (int i = 0; i < data.length; i++) {
            Array.set(array, i, ConverterRegistry.convert(ctx, data[i], componentType));
        }
        return array;
    }
}
