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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.util.ConverterUtil;

import java.util.function.Consumer;

/**
 * @author TinyZ.
 * @version 2022.10.16
 */
public final class StructInternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructInternal.class);

    /**
     * Use {@link String#intern()} to cache field name.
     * equals G1GC -XX:+UseStringDeduplication.
     */
    public static boolean INTERN_FIELD_NAME = true;
    /**
     * Disable excel usermode if the file length greater than 1.5MB, otherwise false.
     * default : 1.5mb
     */
    public static long HANDLER_XLSX_UM_LENGTH_THRESHOLD = 1_572_864L;
    /**
     * {@link org.struct.core.converter.ArrayConverter}
     */
    public static String ARRAY_CONVERTER_STRING_SEPARATOR = "\\|";
    /**
     *
     */
    public static boolean ARRAY_CONVERTER_STRING_TRIM = true;
    /**
     * ignore blank string.
     */
    public static boolean ARRAY_CONVERTER_IGNORE_BLANK = false;

    static {
        handleProperty("struct.core.internFieldName", str -> INTERN_FIELD_NAME = ConverterUtil.isBooleanTrue(str));
        handleProperty("struct.handler.xlsx.UMThreshold", str -> HANDLER_XLSX_UM_LENGTH_THRESHOLD = Long.parseLong(str));
        handleProperty("struct.array-converter.stringSeparator", str -> ARRAY_CONVERTER_STRING_SEPARATOR = str);
        handleProperty("struct.array-converter.stringTrim", str -> ARRAY_CONVERTER_STRING_TRIM = ConverterUtil.isBooleanTrue(str));
        handleProperty("struct.array-converter.ignoreBlank", str -> ARRAY_CONVERTER_IGNORE_BLANK = ConverterUtil.isBooleanTrue(str));
    }

    static void handleProperty(String propertyName, Consumer<String> consumer) {
        try {
            String property = System.getProperty(propertyName);
            if (property != null) {
                consumer.accept(property);
            }
        } catch (Exception e) {
            LOGGER.warn("handle property {} failure. ", propertyName);
        }
    }

}
