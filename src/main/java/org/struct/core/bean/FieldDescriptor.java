package org.struct.core.bean;

import org.struct.core.Converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class FieldDescriptor implements Serializable {
    private static final long serialVersionUID = 8949543119635057452L;

    private String name;
    private Field field;
    private Class<?> reference;
    private String[] refGroupBy;
    private String[] refUniqueKey;
    private boolean required;
    private Converter converter;

    public FieldDescriptor() {
    }

    public FieldDescriptor(String name, Field field, Class<?> reference, String[] refGroupBy, String[] refUniqueKey, boolean required, Converter converter) {
        this.name = name;
        this.field = field;
        this.reference = reference;
        this.refGroupBy = refGroupBy;
        this.refUniqueKey = refUniqueKey;
        this.required = required;
        this.converter = converter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "FieldDescriptor{" +
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
        FieldDescriptor that = (FieldDescriptor) o;
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
