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
