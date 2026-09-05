/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.struct.annotation.StructField;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.core.factory.StructFactory;
import org.struct.exception.IllegalAccessPropertyException;
import org.struct.util.ConverterUtil;

import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SingleFieldDescriptor extends FieldDescriptor {
    @Serial
    private static final long serialVersionUID = 8949543119635057452L;

    /**
     * {@link Field} Or {@link java.lang.reflect.RecordComponent}
     */
    private Object fieldOrRc;
    private Class<?> reference;
    private String[] refGroupBy;
    private String[] refUniqueKey;
    private String aggregateBy;
    private Class<?> aggregateType;
    private boolean required;
    private boolean cached;
    private Converter converter;

    /**
     * Memoized protobuf field lookup. {@code transient}: it is a derived cache, not
     * state, and {@link Descriptors.FieldDescriptor} is not serializable.
     *
     * @see #fieldValueFromMessage(Message)
     */
    private transient volatile ProtobufField pbField;

    public SingleFieldDescriptor() {
    }

    @Deprecated(since = "2022.04.29", forRemoval = true)
    public SingleFieldDescriptor(String name, Field field, Class<?> reference, String[] refGroupBy, String[] refUniqueKey, boolean required, Converter converter) {
        super(name);
        this.fieldOrRc = field;
        this.reference = reference;
        this.refGroupBy = refGroupBy;
        this.refUniqueKey = refUniqueKey;
        this.required = required;
        this.converter = converter;
    }

    public SingleFieldDescriptor(Object fieldOrRc, StructField annotation) {
        this.fieldOrRc = Objects.requireNonNull(fieldOrRc, "field");
        assert fieldOrRc instanceof Field || fieldOrRc instanceof RecordComponent;
        if (annotation != null) {
            this.setRequired(annotation.required());
            this.setCached(annotation.cached());
            if (!annotation.name().isEmpty()) {
                this.setName(annotation.name());
            }
            if (Object.class != annotation.ref()) {
                this.setReference(annotation.ref());
                this.setRefGroupBy(annotation.refGroupBy());
                this.setRefUniqueKey(annotation.refUniqueKey());
            }
            if (annotation.aggregateBy().length() > 0) {
                this.setAggregateBy(annotation.aggregateBy());
                this.setAggregateType(annotation.aggregateType());
            }
            Class<? extends Converter> c = annotation.converter();
            if (Converter.class != c
                    && !Modifier.isInterface(c.getModifiers())
                    && !Modifier.isAbstract(c.getModifiers())
            ) {
                this.setConverter(ConverterRegistry.lookupOrDefault(c, c));
            }
        } else {
            this.setRequired(StructConfig.INSTANCE.isStructRequiredDefault());
        }
        String name = this.getName();
        //  handle default field name.
        if (null == name || name.isEmpty()) {
            if (fieldOrRc instanceof RecordComponent rc) {
                this.setName(rc.getName());
            } else {
                this.setName(((Field) fieldOrRc).getName());
            }
        }
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

    public String getAggregateBy() {
        return aggregateBy;
    }

    public void setAggregateBy(String aggregateBy) {
        this.aggregateBy = aggregateBy;
    }

    public Class<?> getAggregateType() {
        return this.aggregateType;
    }

    public void setAggregateType(Class<?> aggregateType) {
        this.aggregateType = aggregateType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
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
     * Is this field is basic type Collection field? e.g.
     *
     * <pre> {@code
     *    define struct field.
     *  class xxx {
     *      @StructField(ref = int.class)
     *      public List<Integer> var0;
     *      @StructField(ref = String.class)
     *      public List<String> var1;
     *      @StructField(ref = Double.class)
     *      public List<Double> var2;
     *  }}</pre>
     *
     * @return
     * @see ConverterRegistry#convertCollection(org.struct.core.converter.ConvertContext, Object, Class, Class)
     * @see ConverterUtil#isBasicType(Class)
     */
    public boolean isBasicTypeCollection() {
        boolean isRefConfigMissing = this.refGroupBy.length == 0 && refUniqueKey.length == 0 && !this.isAggregateField();
        return Collection.class.isAssignableFrom(this.getFieldType())
                && isRefConfigMissing
                && ConverterUtil.isBasicType(this.getReference());
    }

    /**
     * @return the reference field url.
     */
    public String getRefFieldUrl() {
        return getReference().getName() + ":" + getName();
    }

    public Class<?> getFieldType() {
        if (fieldOrRc instanceof RecordComponent rc) {
            return rc.getType();
        } else if (fieldOrRc instanceof Field f) {
            return f.getType();
        }
        return Object.class;
    }

    /**
     * Get field's value.
     * <p>
     * The value source is resolved by the runtime type of {@code instance}:
     * <ul>
     *   <li>{@link StructImpl} - a row of tabular data (csv / excel), keyed by column name.</li>
     *   <li>{@link Message} - a protobuf message; the value is read straight from the
     *       message's descriptor by field name, with no intermediate row object.</li>
     *   <li>anything else - a plain bean / record, read reflectively.</li>
     * </ul>
     *
     * @param instance the instance object
     * @return field's value.
     */
    public Object getFieldValueFrom(Object instance) {
        if (instance instanceof StructImpl si) {
            return si.get(this);
        }
        if (instance instanceof Message msg) {
            return this.fieldValueFromMessage(msg);
        }
        try {
            if (fieldOrRc instanceof RecordComponent rc) {
                Method accessor = rc.getAccessor();
                if (!accessor.canAccess(instance)) {
                    accessor.setAccessible(true);
                }
                return accessor.invoke(instance);
            } else if (fieldOrRc instanceof Field f) {
                return f.get(instance);
            }
        } catch (Exception e) {
            throw new IllegalAccessPropertyException("get field value failure. field:" + this.getName(), e);
        }
        return null;
    }

    /**
     * Read this field's value directly out of a protobuf message.
     * <p>
     * This is what lets a protobuf {@code DynamicMessage} drive the regular converter
     * pipeline without ever being flattened into a {@link StructImpl}: it saves the
     * whole intermediate row object (one HashMap plus one entry set per row).
     *
     * <p><b>Why {@code hasField} is checked.</b> proto3 omits fields holding the type's
     * default value from the wire, but {@code getField} still returns that default
     * ({@code 0} / {@code ""} / {@code false}). An unset field must behave exactly like
     * a key missing from a {@link StructImpl} - i.e. {@code null} - otherwise
     * {@code required} validation and the converters would observe {@code 0} instead of
     * "absent" and silently produce wrong data.
     *
     * <p>The resolved {@link Descriptors.FieldDescriptor} is memoized against the
     * message descriptor it came from. A single data file always yields the same
     * descriptor instance, so this is a monomorphic hit after the first row; the pair
     * is published as one immutable record, so a torn read can only cause a re-resolve,
     * never a mismatch between descriptor and field.
     */
    private Object fieldValueFromMessage(Message msg) {
        Descriptors.Descriptor msgDescriptor = msg.getDescriptorForType();
        ProtobufField cached = this.pbField;
        Descriptors.FieldDescriptor fd;
        if (cached != null && cached.descriptor() == msgDescriptor) {
            fd = cached.field();
        } else {
            fd = resolveProtobufField(msgDescriptor, this.getName());
            this.pbField = new ProtobufField(msgDescriptor, fd);
        }
        if (fd == null) {
            return null;
        }
        if (fd.isRepeated()) {
            List<?> values = (List<?>) msg.getField(fd);
            return values.isEmpty() ? null : values;
        }
        return msg.hasField(fd) ? msg.getField(fd) : null;
    }

    /**
     * Resolve a protobuf field by name. proto field names are {@code snake_case} by
     * convention while the corresponding java / JSON name is {@code camelCase}, so the
     * JSON name is tried as a fallback - that way both {@code stack_limit} and
     * {@code stackLimit} resolve to the same field.
     */
    private static Descriptors.FieldDescriptor resolveProtobufField(Descriptors.Descriptor descriptor, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Descriptors.FieldDescriptor fd = descriptor.findFieldByName(name);
        if (fd != null) {
            return fd;
        }
        //  Fall back to the protobuf JSON name (e.g. snake_case field vs camelCase bean).
        for (Descriptors.FieldDescriptor candidate : descriptor.getFields()) {
            if (name.equals(candidate.getJsonName())) {
                return candidate;
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
        if (fieldOrRc instanceof RecordComponent) {
            throw new UnsupportedOperationException("JDK record class not support setter. clz:" + instance.getClass());
        } else if (fieldOrRc instanceof Field f) {
            try {
                f.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessPropertyException("set field value failure. field:" + this.getName() + ", val:" + value, e);
            }
        }
    }

    public boolean isAggregateField() {
        String by = this.aggregateBy;
        return by != null && by.length() > 0;
    }

    /**
     * The aggregate struct field type.
     *
     * @return the aggregate struct field's type.
     * @see StructWorker#handleReferenceFieldValue(StructFactory, SingleFieldDescriptor)
     */
    public Class<?> resolveAggregateWorkerType() {
        return Object.class == this.aggregateType
                ? this.reference
                : this.aggregateType;
    }

    @Override
    public String toString() {
        return "SingleFieldDescriptor{" +
                "fieldOrRc=" + fieldOrRc +
                ", reference=" + reference +
                ", refGroupBy=" + Arrays.toString(refGroupBy) +
                ", refUniqueKey=" + Arrays.toString(refUniqueKey) +
                ", required=" + required +
                ", converter=" + converter +
                ", name='" + getName() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SingleFieldDescriptor that = (SingleFieldDescriptor) o;
        return required == that.required && Objects.equals(fieldOrRc, that.fieldOrRc) && Objects.equals(reference, that.reference) && Arrays.equals(refGroupBy, that.refGroupBy) && Arrays.equals(refUniqueKey, that.refUniqueKey) && Objects.equals(aggregateBy, that.aggregateBy) && Objects.equals(aggregateType, that.aggregateType) && Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), fieldOrRc, reference, aggregateBy, aggregateType, required, converter);
        result = 31 * result + Arrays.hashCode(refGroupBy);
        result = 31 * result + Arrays.hashCode(refUniqueKey);
        return result;
    }

    /**
     * A message descriptor paired with the field resolved from it. Both components are
     * published together through a single {@code volatile} write, so a reader can never
     * observe a field that belongs to a different descriptor.
     */
    private record ProtobufField(Descriptors.Descriptor descriptor, Descriptors.FieldDescriptor field) {
    }
}
