package org.struct.core;

import java.util.LinkedHashMap;

/**
 * Temp struct instance implement.
 */
public class StructImpl {

    /**
     * Current row cell's value map.
     * [column index, field value]
     */
    private LinkedHashMap<String, Object> valuesMap = new LinkedHashMap<>();

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
     * @param force     covert the prevent value if the field has been set.
     */
    public void add(String fieldName, Object val, boolean force) {
        //  ignore NULL or empty value;
        boolean ignore = (val == null)
                || (val instanceof String && ((String) val).isEmpty());
        if (ignore) {
            return;
        }
        if (force) {
            valuesMap.put(fieldName, val);
        } else {
            valuesMap.putIfAbsent(fieldName, val);
        }
    }

    public Object get(FieldDescriptor descriptor) {
        return valuesMap.get(descriptor.getName());
    }

    public boolean isEmpty() {
        return valuesMap.isEmpty();
    }

}
