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
import java.util.function.BiFunction;

/**
 * 原生类型的转换器.
 */
public class EmbeddedBasicTypeConverter implements Converter {

    private static final Set<BasicJavaType> basicTypeMap = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        basicTypeMap.add(new BasicJavaType((s, c) -> ConverterUtil.isHexNumber(s) ? Integer.decode(s) : Integer.valueOf(s), 0, int.class, Integer.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> ConverterUtil.isHexNumber(s) ? Long.decode(s) : Long.valueOf(s), 0L, long.class, Long.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> ConverterUtil.isBooleanTrue(s), false, boolean.class, Boolean.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> ConverterUtil.isHexNumber(s) ? Short.decode(s) : Short.valueOf(s), (short) 0, short.class, Short.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> ConverterUtil.isHexNumber(s) ? Byte.decode(s) : Byte.valueOf(s), (byte) 0, byte.class, Byte.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> Float.valueOf(s), 0.0F, float.class, Float.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> Double.valueOf(s), 0.0D, double.class, Double.class));
        basicTypeMap.add(new BasicJavaType((s, c) -> s.charAt(0), (char) 0, char.class, Character.class));
    }

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        for (BasicJavaType spt : basicTypeMap) {
            for (Class<?> clzOfType : spt.classes) {
                if (clzOfType == targetType) {
                    boolean isBlank = originValue == null
                            || (originValue instanceof String && ((String) originValue).isEmpty());
                    return isBlank
                            ? spt.defaultValue
                            : spt.func.apply(originValue, targetType);
                }
            }
        }
        //  notice: if the targetType is primitive type, impossible do this.
        return originValue;
    }

    public static class BasicJavaType {

        private final BiFunction<Object, Class<?>, Object> func;
        private final Object defaultValue;
        private final Class<?>[] classes;

        public BasicJavaType(BiFunction<String, Class<?>, Object> func, Object defaultValue, Class<?>... classes) {
            this.func = (obj, clz) -> {
                if (obj instanceof String) {
                    return func.apply((String) obj, clz);
                } else if (obj instanceof Number) {
                    return ConverterUtil.convertNumberToTargetClass((Number) obj, clz);
                } else {
                    return obj;
                }
            };
            this.defaultValue = defaultValue;
            if (classes == null || classes.length <= 0)
                throw new IllegalArgumentException("classes");
            this.classes = classes;
        }
    }
}
