package org.struct.core.converter;

import org.struct.core.Converter;
import org.struct.util.ConverterUtil;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author TinyZ.
 * @version 2020.03.26
 */
public class StringToArrayConverter implements Converter {

    private static final String DEFAULT_SEPARATOR = "\\|";

    /**
     * The string separator.
     */
    private final String separator;

    private final boolean exceptBlank;

    public StringToArrayConverter() {
        this(DEFAULT_SEPARATOR, true);
    }

    public StringToArrayConverter(String separator, boolean exceptBlank) {
        this.separator = separator;
        this.exceptBlank = exceptBlank;
    }

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        if (!targetType.isArray() || String.class != originValue.getClass()) {
            return null;
        }
        String content = (String) originValue;
        Class<?> componentType = targetType.getComponentType();
        String[] data = content.split(separator);
        if (exceptBlank) {
            data = Arrays.stream(data)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
        Object array = Array.newInstance(componentType, data.length);
        for (int i = 0; i < data.length; i++) {
            Array.set(array, i, ConverterUtil.covert(data[i], componentType));
        }
        return array;
    }
}
