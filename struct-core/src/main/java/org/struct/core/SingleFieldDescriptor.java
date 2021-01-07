/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

import org.struct.core.converter.Converter;
import org.struct.exception.IllegalAccessPropertyException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class SingleFieldDescriptor extends FieldDescriptor {
    private static final long serialVersionUID = 8949543119635057452L;

    private Field field;
    private Class<?> fieldType;
    private Class<?> reference;
    private String[] refGroupBy;
    private String[] refUniqueKey;
    private boolean required;
    private Converter converter;

    public SingleFieldDescriptor() {
    }

    public SingleFieldDescriptor(String name, Field field, Class<?> reference, String[] refGroupBy, String[] refUniqueKey, boolean required, Converter converter) {
        this.name = name;
        this.field = field;
        this.reference = reference;
        this.refGroupBy = refGroupBy;
        this.refUniqueKey = refUniqueKey;
        this.required = required;
        this.converter = converter;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Class<?> getReference() {
        return reference;
    }

    public void setReference(Class<?> reference) {
        this.reference = reference;
    }

    public String[] getRefGroupBy() {
        return refGroupBy;
    }

    public void setRefGroupBy(String[] refGroupBy) {
        this.refGroupBy = refGroupBy;
    }

    public String[] getRefUniqueKey() {
        return refUniqueKey;
    }

    public void setRefUniqueKey(String[] refUniqueKey) {
        this.refUniqueKey = refUniqueKey;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    /**
     * Is this field is reference field?
     *
     * @return return true if the field is reference other clz.
     */
    public boolean isReferenceField() {
        return null != this.reference && Object.class != this.reference;
    }

    /**
     * @return the reference field url.
     */
    public String getRefFieldUrl() {
        return getReference().getName() + ":" + getName();
    }

    public Class<?> getFieldType() {
        Field field = getField();
        if (field != null) {
            return field.getType();
        } else if (this.fieldType != null) {
            return this.fieldType;
        }
        return Object.class;
    }

    /**
     * Get field's value.
     *
     * @param instance the instance object
     * @return field's value.
     */
    public Object getFieldValue(Object instance) {
        Field field = getField();
        if (field != null) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessPropertyException("get field value failure. field:" + field.getName(), e);
            }
        }
        return null;
    }

    /**
     * Set field's value.
     *
     * @param instance the instance object
     * @param value    the field's value.
     */
    public void setFieldValue(Object instance, Object value) {
        Field field = getField();
        if (field != null) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessPropertyException("set field value failure. field:" + field.getName() + ", val:" + value, e);
            }
        }
    }

    @Override
    public String toString() {
        return "SingleFieldDescriptor{" +
                "name='" + name + '\'' +
                ", field=" + field +
                ", ref=" + reference +
                ", refGroupBy=" + Arrays.toString(refGroupBy) +
                ", refUniqueKey=" + Arrays.toString(refUniqueKey) +
                ", required=" + required +
                ", converter=" + converter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleFieldDescriptor that = (SingleFieldDescriptor) o;
        return required == that.required &&
                Objects.equals(name, that.name) &&
                Objects.equals(field, that.field) &&
                Objects.equals(reference, that.reference) &&
                Arrays.equals(refGroupBy, that.refGroupBy) &&
                Arrays.equals(refUniqueKey, that.refUniqueKey) &&
                Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, field, reference, required, converter);
        result = 31 * result + Arrays.hashCode(refGroupBy);
        result = 31 * result + Arrays.hashCode(refUniqueKey);
        return result;
    }
}
