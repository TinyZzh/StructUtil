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

import org.struct.core.converter.Converter;
import org.struct.core.converter.StringToArrayConverter;
import org.struct.util.Reflects;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converter Registry.
 * <p>
 * manager user's custom converter.
 */
public final class ConverterRegistry {

    private static final Map<Class, Converter> registeredConverterCacheMap = new ConcurrentHashMap<>();

    static {
        Class<?>[] str2ArrayConverters = new Class[]{
                String[].class,
                byte[].class, Byte[].class,
                boolean[].class, Boolean[].class,
                short[].class, Short[].class,
                int[].class, Integer[].class,
                long[].class, Long[].class,
                float[].class, Float[].class,
                double[].class, Double[].class
        };
        StringToArrayConverter converter = new StringToArrayConverter();
        for (Class<?> targetType : str2ArrayConverters) {
            register(targetType, converter);
        }
    }

    private ConverterRegistry() {
        //  no-op
    }

    /**
     * register custom converter implement.
     */
    public static void register(Class<?> actualType, Converter converter) {
        registeredConverterCacheMap.put(actualType, converter);
    }

    public static void register(Class<?> actualType, Class<? extends Converter> clzOfConverter, Object... params) {
        if (Modifier.isAbstract(clzOfConverter.getModifiers())
                || Modifier.isInterface(clzOfConverter.getModifiers())
                || clzOfConverter.isAnonymousClass()) {
            throw new IllegalArgumentException("clazz :" + clzOfConverter.getName() + " must be real class.");
        }
        Converter converter = Reflects.newInstance(clzOfConverter, params);
        if (null == converter) {
            throw new IllegalArgumentException("clazz :" + clzOfConverter.getName() + " could't new instance.");
        }
        register(actualType, converter);
    }

    public static void unregister(Class<?> actualType) {
        registeredConverterCacheMap.remove(actualType);
    }

    public static Converter lookup(Class<?> actualType) {
        return registeredConverterCacheMap.get(actualType);
    }

    /**
     * look up converter by class.
     */
    public static Converter lookupOrDefault(Class<?> actualType, Class<? extends Converter> clzOfConverter, Object... params) {
        Converter converter = registeredConverterCacheMap.get(actualType);
        if (converter != null)
            return converter;
        try {
            Converter impl = Reflects.newInstance(clzOfConverter, params);
            register(actualType, impl);
            return impl;
        } catch (Exception e) {
            throw new IllegalArgumentException("clz:" + actualType.getName() + " no such found no args constructor.");
        }
    }
}
