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

import org.struct.spi.ServiceLoader;
import org.struct.util.ConverterUtil;
import org.struct.util.Reflects;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converter Registry.
 * <p>
 * manager user's custom converter.
 */
public final class ConverterRegistry {

    private static final Map<Class<?>, Converter> REGISTERED_CONVERTER_CACHE_MAP = new ConcurrentHashMap<>();

    static {
        List<Converters> convertersList = ServiceLoader.loadAll(Converters.class);
        convertersList.forEach(o -> {
            for (Map.Entry<Class<?>, Converter> entry : o.getConverters().entrySet()) {
                register(entry.getKey(), entry.getValue());
            }
        });
    }

    private ConverterRegistry() {
        //  no-op
    }

    /**
     * register custom converter implement.
     */
    public static void register(Class<?> actualType, Converter converter) {
        REGISTERED_CONVERTER_CACHE_MAP.put(actualType, converter);
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
        REGISTERED_CONVERTER_CACHE_MAP.remove(actualType);
    }

    public static Converter lookup(Class<?> actualType) {
        return REGISTERED_CONVERTER_CACHE_MAP.get(actualType);
    }

    /**
     * look up converter by class.
     */
    public static Converter lookupOrDefault(Class<?> actualType, Class<? extends Converter> clzOfConverter, Object... params) {
        Converter converter = REGISTERED_CONVERTER_CACHE_MAP.get(actualType);
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

    /**
     * Convert originValue to requiredType.
     *
     * @param originValue  the origin value.
     * @param requiredType the required type.
     * @return the converted object, which must be an instance of {@code requiredType} (or null)
     */
    public static Object convert(Object originValue, Class<?> requiredType) {
        if (Object.class == requiredType
                || requiredType.isInstance(originValue)) {
            return originValue;
        }
        //  try lookup user's converter first.
        Converter converter = lookup(requiredType);
        if (null == converter) {
            if (requiredType.isEnum()) {
                converter = lookup(Enum.class);
            } else if (requiredType.isArray()) {
                converter = lookup(Array.class);
            }
        }
        if (null != converter) {
            return converter.convert(originValue, requiredType);
        }
        return originValue;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convertCollection(Object originValue, Class<?> collectType, Class<?> requiredType) {
        if (!Collection.class.isAssignableFrom(collectType)
                || !ConverterUtil.isBasicType(requiredType)) {
            return originValue;
        }
        Collection collection = null;
        if (collectType.isInterface()) {
            if (SortedSet.class.isAssignableFrom(collectType)) {
                collection = new TreeSet();
            } else if (Set.class.isAssignableFrom(collectType)) {
                collection = new HashSet();
            } else {
                collection = new ArrayList();
            }
        } else {
            try {
                collection = (Collection<Object>) collectType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                //  no-op
                collection = new ArrayList();
            }
        }
        //  try lookup user's converter first.
        Converter converter = lookup(Array.class);
        Object ary = converter.convert(originValue, Array.newInstance(requiredType, 0).getClass());
        if (ary != null && ary.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(ary); i++) {
                collection.add(Array.get(ary, i));
            }
        }
        return collection;
    }
}
