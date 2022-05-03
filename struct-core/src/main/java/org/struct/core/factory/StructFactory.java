package org.struct.core.factory;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
public interface StructFactory {

    /**
     * Parse struct class.
     */
    void parseStruct();

    /**
     * Create struct class instance.
     *
     * @param structImpl the struct impl data.
     * @return optional of struct class instance.
     */
    Object newStructInstance(Object structImpl);

    /**
     * Get struct field's value.
     *
     * @param src     the struct instance.
     * @param refKeys ref keys array.
     * @return {@link Object[]} or field value.
     */
    Object getFieldValuesArray(Object src, String[] refKeys);
}
