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

package org.struct.core.converter;

/**
 * {@link Enum} converter.
 *
 * @author TinyZ.
 */
public class EnumConverter implements Converter {

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        if (!targetType.isEnum()) {
            return originValue;
        }
        Object[] enums = targetType.getEnumConstants();
        //  1. int -> enum
        try {
            int i = (int) ConverterRegistry.convert(originValue, Integer.class);
            if (0 <= i && i < enums.length) {
                return enums[i];
            }
        } catch (Exception e) {
            //  no-op
        }
        //  2. string -> enum
        if (originValue instanceof String) {
            if (Enum.class.isAssignableFrom(targetType)) {
                String trim = ((String) originValue).trim();
                try {
                    return Enum.valueOf((Class<? extends Enum>) targetType, trim);
                } catch (Exception e1) {
                    try {
                        //  Upper case
                        return Enum.valueOf((Class<? extends Enum>) targetType, trim.toUpperCase());
                    } catch (Exception e2) {
                        //  no-op
                    }
                }
            }
            //  compare string ignore case.
            for (Object anEnum : enums) {
                if (((String) originValue).equalsIgnoreCase(String.valueOf(anEnum))) {
                    return anEnum;
                }
            }
        }
        //  3. enum -> enum
        if (originValue.getClass().isEnum()) {
            int i = ((Enum) originValue).ordinal();
            if (i < enums.length) {
                return enums[i];
            }
        }
        throw new IllegalStateException("clz:" + targetType.getName() + ", unknown enum:" + originValue);
    }
}
