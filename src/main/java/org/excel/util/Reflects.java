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
        if (null == constructor)
            return null;
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
