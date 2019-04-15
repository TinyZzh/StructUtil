/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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

package org.excel.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converter Registry.
 * <p>
 * manager user's custom converter.
 */
public final class ConverterRegistry {

    private static final Map<Class, Converter> registeredConverterMap = new ConcurrentHashMap<>();

    private ConverterRegistry() {
        //  no-op
    }

    /**
     * register custom converter implement.
     */
    public static void register(Converter<?> converter) {
        registeredConverterMap.putIfAbsent(converter.getClass(), converter);
    }

    public static void register(Class<? extends Converter> clzOfConverter) throws Exception {
        registeredConverterMap.putIfAbsent(clzOfConverter, clzOfConverter.getConstructor().newInstance());
    }

    /**
     * look up converter by class.
     */
    public static Converter<?> lookup(Class<? extends Converter> clz) {
        Converter converter = registeredConverterMap.get(clz);
        if (converter != null)
            return converter;
        try {
            Converter impl = clz.getConstructor().newInstance();
            register(impl);
            return impl;
        } catch (Exception e) {
            throw new IllegalArgumentException("clz:" + clz.getName());
        }
    }


}
