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

package org.struct.util;

import org.apache.commons.lang3.ClassUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Reflects {

    private static final Map<Class<?>, Map<String, Optional<MethodHandle>>> METHOD_HANDLE_MAP = new ConcurrentHashMap<>();

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
            if (null == params || params.length <= 0) {
                return constructor.newInstance();
            } else {
                return constructor.newInstance(params);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Instance class:" + clazz + " failure. args:" + Arrays.toString(params), e);
        }
    }

    public static <T> Constructor<T> lookupConstructor(Class<T> clazz, Object... params) {
        Constructor<T> constructor = null;
        if (null == params || params.length <= 0) {
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
            final Class<?>[] paramTypes = ClassUtils.toClass(params);
            try {
                constructor = clazz.getConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                try {
                    constructor = clazz.getDeclaredConstructor(paramTypes);
                } catch (NoSuchMethodException e1) {
                    //  no-op
                }
            }
        }
        if (constructor == null) {
            //  lookup the constructor
            for (Constructor<?> ctor : clazz.getConstructors()) {
                Class<?>[] ctorParameterTypes = ctor.getParameterTypes();
                if (params.length == ctorParameterTypes.length) {
                    boolean matched = true;
                    for (int i = 0; i < params.length; i++) {
                        if (!ClassUtils.isAssignable(params[i].getClass(), ctorParameterTypes[i], true)) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        constructor = (Constructor<T>) ctor;
                        break;
                    }
                }
            }
        }
        if (constructor != null) {
            constructor.setAccessible(true);
        }
        return constructor;
    }

    public static List<Field> resolveAllFields(Class<?> clz) {
        return resolveAllFields(clz, true);
    }

    public static List<Field> resolveAllFields(Class<?> originClz, boolean declared) {
        List<Field> result = new ArrayList<>();
        Class<?> thatClz = originClz;
        while (thatClz != Object.class) {
            Collections.addAll(result, declared ? thatClz.getDeclaredFields() : thatClz.getFields());
            thatClz = thatClz.getSuperclass();
        }
        return result;
    }

    public static Optional<MethodHandle> lookupFieldGetter(Class<?> clzOfObj, String fieldName) {
        Map<String, Optional<MethodHandle>> methodHandleMap = METHOD_HANDLE_MAP.computeIfAbsent(clzOfObj, c -> new ConcurrentHashMap<>());
        return methodHandleMap.computeIfAbsent(fieldName, fn -> Optional.ofNullable(lookupAccessor(clzOfObj, fieldName)));
    }

    public static MethodHandle lookupAccessor(Class<?> clz, String fieldName) {
        Class<?> searchType = Objects.requireNonNull(clz, "clz");
        assert fieldName != null && fieldName.length() > 0;
        try {
            while (Object.class != searchType && searchType != null) {
                if (searchType.isRecord()) {
                    for (RecordComponent rc : searchType.getRecordComponents()) {
                        if (Objects.equals(fieldName, rc.getName())
                                && rc.getAccessor().trySetAccessible()) {
                            return MethodHandles.lookup().unreflect(rc.getAccessor());
                        }
                    }
                } else {
                    Field[] fields = searchType.getDeclaredFields();
                    for (Field field : fields) {
                        if (Objects.equals(fieldName, field.getName())
                                && field.trySetAccessible()) {
                            return MethodHandles.lookup().unreflectGetter(field);
                        }
                    }
                }
                searchType = searchType.getSuperclass();
            }
        } catch (Exception e) {
            //  no-op
        }
        return null;
    }

    public static Optional<MethodHandle> lookupFieldSetter(Class<?> clzOfObj, String fieldName) {
        Objects.requireNonNull(clzOfObj, "clzOfObj");
        assert fieldName != null && fieldName.length() > 0;
        if (clzOfObj.isRecord())
            throw new IllegalStateException("Record can not access the field's write method");
        Map<String, Optional<MethodHandle>> methodHandleMap = METHOD_HANDLE_MAP.computeIfAbsent(clzOfObj, c -> new ConcurrentHashMap<>());
        return methodHandleMap.computeIfAbsent(fieldName, fn -> {
            Class<?> searchType = clzOfObj;
            try {
                while (Object.class != searchType && searchType != null) {
                    Field[] fields = searchType.getDeclaredFields();
                    for (Field field : fields) {
                        if (Objects.equals(fieldName, field.getName())
                                && field.trySetAccessible()) {
                            return Optional.ofNullable(MethodHandles.lookup().unreflectSetter(field));
                        }
                    }
                    searchType = searchType.getSuperclass();
                }
            } catch (Exception e) {
                //  no-op
            }
            return Optional.empty();
        });
    }
}
