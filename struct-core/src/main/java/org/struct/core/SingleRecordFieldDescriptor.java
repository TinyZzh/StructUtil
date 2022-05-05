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

import org.struct.annotation.StructField;
import org.struct.core.converter.Converter;
import org.struct.exception.IllegalAccessPropertyException;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Objects;

public class SingleRecordFieldDescriptor extends SingleFieldDescriptor {
    private static final long serialVersionUID = 8949543119635057452L;

    private RecordComponent rc;
    private Class<?> reference;
    private String[] refGroupBy;
    private String[] refUniqueKey;
    private boolean required;
    private Converter converter;

    public SingleRecordFieldDescriptor() {
    }

    public SingleRecordFieldDescriptor(StructField annotation, boolean globalStructRequiredValue) {
        super(annotation, globalStructRequiredValue);
    }

    public RecordComponent getRc() {
        return rc;
    }

    public void setRc(RecordComponent rc) {
        this.rc = rc;
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
        if (rc != null) {
            return rc.getType();
        }
        return Object.class;
    }

    /**
     * Get field's value.
     *
     * @param instance the instance object
     * @return field's value.
     */
    public Object getFieldValueFrom(Object instance) {
        if (instance instanceof StructImpl si) {
            return si.get(this);
        } else if (rc != null) {
            try {
                Method accessor = rc.getAccessor();
                if (!accessor.canAccess(instance)) {
                    accessor.setAccessible(true);
                }
                return accessor.invoke(instance);
            } catch (Exception e) {
                throw new IllegalAccessPropertyException("get field value failure. field:" + rc.getName(), e);
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
        throw new UnsupportedOperationException("JDK record class not support setter. clz:" + instance.getClass());
    }

    @Override
    public String toString() {
        return "SingleRecordFieldDescriptor{" +
                "rc=" + rc +
                ", reference=" + reference +
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
        if (!super.equals(o)) return false;
        SingleRecordFieldDescriptor that = (SingleRecordFieldDescriptor) o;
        return required == that.required && Objects.equals(rc, that.rc) && Objects.equals(reference, that.reference) && Arrays.equals(refGroupBy, that.refGroupBy) && Arrays.equals(refUniqueKey, that.refUniqueKey) && Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), rc, reference, required, converter);
        result = 31 * result + Arrays.hashCode(refGroupBy);
        result = 31 * result + Arrays.hashCode(refUniqueKey);
        return result;
    }

}
