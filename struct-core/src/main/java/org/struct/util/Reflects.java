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

package org.struct.util;

import org.apache.commons.lang3.ClassUtils;

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
}
