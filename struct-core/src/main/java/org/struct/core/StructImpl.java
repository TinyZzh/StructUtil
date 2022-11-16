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

package org.struct.core;

import java.util.HashMap;

/**
 * Temp struct instance implement.
 */
public class StructImpl {

    /**
     * Current row cell's value map.
     * [column index, field value]
     */
    private final HashMap<String, Object> valuesMap = new HashMap<>();

    /**
     * Add field's value.
     *
     * @param fieldName the field's name
     * @param val       the field's value
     */
    public void add(String fieldName, Object val) {
        this.add(fieldName, val, false);
    }

    /**
     * Add field's value.
     *
     * @param fieldName the field's name
     * @param val       the field's value
     * @param force     covert the previous value if the field has been set.
     */
    public void add(String fieldName, Object val, boolean force) {
        if (fieldName == null || fieldName.isEmpty()) {
            return;
        }
        //  ignore NULL or empty value;
        boolean ignore = (val == null)
                || (val instanceof String && ((String) val).isEmpty());
        if (ignore) {
            return;
        }
        fieldName = fieldName.intern();
        if (force) {
            valuesMap.put(fieldName, val);
        } else {
            valuesMap.putIfAbsent(fieldName, val);
        }
    }

    public Object get(SingleFieldDescriptor descriptor) {
        return valuesMap.get(descriptor.getName());
    }

    public boolean isEmpty() {
        return valuesMap.isEmpty();
    }

}
