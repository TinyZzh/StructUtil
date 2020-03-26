package org.struct.core.converter;

import org.struct.core.Converter;

import java.lang.reflect.Field;

/**
 * @author TinyZ.
 * @version 2020.03.26
 */
public class StringToArrayConverter implements Converter {

    private static final String ss = "|";

    @Override
    public Object convert(Object originValue, Field targetField) {
        Class<?> type = targetField.getType();
        if (!type.isArray() || String.class != originValue.getClass()) {
            return null;
        }
        String content = (String) originValue;
        Class<?> componentType = type.getComponentType();
        if (componentType.isPrimitive()) {
            String[] array = content.split(ss);
            if (boolean.class == componentType) {

            }
            else if (byte.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (char.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (double.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (float.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (int.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (long.class.isAssignableFrom(componentType)) {
                /* ... */
            }

            else if (short.class.isAssignableFrom(componentType)) {
                /* ... */
            }
        } else {
            //  object[]


        }
        return null;
    }
}
