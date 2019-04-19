package org.excel.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public final class Reflects {

    private Reflects() {
        //  no-op
    }

    /**
     * Create new instance with clazz.
     */
    public static <T> T newInstance(Class<T> clazz, Object... params) {
        Constructor<T> constructor = lookupConstructor(clazz, params);
        //  Notice: the class is nested class or parameter not match
        if (null == constructor)
            throw new IllegalArgumentException("Couldn't match any constructor. clz:" + clazz + ", params:" + Arrays.toString(params)
                    + ". make sure not nested class and all params type not primitive type");
        try {
            if (params.length <= 0) {
                return constructor.newInstance();
            } else {
                return constructor.newInstance(params);
            }
        } catch (Exception e) {
            //  no-op
        }
        return null;
    }

    public static <T> Constructor<T> lookupConstructor(Class<T> clazz, Object... params) {
        Constructor<T> constructor = null;
        if (params.length <= 0) {
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                try {
                    constructor = clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException e1) {
                    //  no-op
                }
            }
        } else {
            Class[] classes = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
            try {
                constructor = clazz.getConstructor(classes);
            } catch (NoSuchMethodException e) {
                try {
                    constructor = clazz.getDeclaredConstructor(classes);
                } catch (NoSuchMethodException e1) {
                    //  no-op
                }
            }
        }
        if (constructor != null) {
            constructor.setAccessible(true);
        }
        return constructor;
    }
}
