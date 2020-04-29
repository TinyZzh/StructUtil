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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 原生类型的转换器.
 */
public class EmbeddedPrimitiveTypeConverter implements Converter {

    private static Set<StructPrimitiveType> primitiveTypeMap = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        primitiveTypeMap.add(new StructPrimitiveType(s -> ConverterUtil.isHexNumber(s) ? Integer.decode(s) : Integer.valueOf(s), 0, int.class, Integer.class));
        primitiveTypeMap.add(new StructPrimitiveType(s -> ConverterUtil.isHexNumber(s) ? Long.decode(s) : Long.valueOf(s), 0L, long.class, Long.class));
        primitiveTypeMap.add(new StructPrimitiveType(ConverterUtil::isBooleanTrue, false, boolean.class, Boolean.class));
        primitiveTypeMap.add(new StructPrimitiveType(s -> ConverterUtil.isHexNumber(s) ? Short.decode(s) : Short.valueOf(s), (short) 0, short.class, Short.class));
        primitiveTypeMap.add(new StructPrimitiveType(s -> ConverterUtil.isHexNumber(s) ? Byte.decode(s) : Byte.valueOf(s), (byte) 0, byte.class, Byte.class));
        primitiveTypeMap.add(new StructPrimitiveType(Float::valueOf, 0.0F, float.class, Float.class));
        primitiveTypeMap.add(new StructPrimitiveType(Double::valueOf, 0.0D, double.class, Double.class));
        primitiveTypeMap.add(new StructPrimitiveType(s -> s.charAt(0), (char) 0, char.class, Character.class));
    }

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        for (StructPrimitiveType spt : primitiveTypeMap) {
            for (Class<?> clzOfType : spt.classes) {
                if (clzOfType == targetType) {
                    return originValue instanceof String
                            ? spt.func.apply(originValue)
                            : spt.defaultValue;
                }
            }
        }
        //  notice: if the targetType is primitive type, impossible do this.
        return originValue;
    }

    public static class StructPrimitiveType {

        private Function<Object, Object> func;
        private Object defaultValue;
        private Class<?>[] classes;

        public StructPrimitiveType(Function<String, Object> func, Object defaultValue, Class<?>... classes) {
            this.func = (obj) -> {
                if (obj instanceof String) {
                    return func.apply((String) obj);
                } else {
                    return null;
                }
            };
            this.defaultValue = defaultValue;
            if (classes == null || classes.length <= 0)
                throw new IllegalArgumentException("classes");
            this.classes = classes;
        }
    }
}
