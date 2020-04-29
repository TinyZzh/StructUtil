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

package org.struct.core.converter;

import org.struct.util.ConverterUtil;

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
        try {
            int i = (int) ConverterUtil.covert(originValue, Integer.class);
            if (i < enums.length) {
                return enums[i];
            }
        } catch (Exception e) {
            if (originValue instanceof String) {
                for (Object anEnum : enums) {
                    if (((String) originValue).equalsIgnoreCase(String.valueOf(anEnum))) {
                        return anEnum;
                    }
                }
            }
        }
        throw new IllegalStateException("clz:" + targetType.getName() + ", unknown enum:" + originValue);
    }
}
