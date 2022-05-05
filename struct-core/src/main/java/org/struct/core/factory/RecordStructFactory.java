package org.struct.core.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.core.ArrayKey;
import org.struct.core.FieldDescriptor;
import org.struct.core.OptionalDescriptor;
import org.struct.core.SingleFieldDescriptor;
import org.struct.core.SingleRecordFieldDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.exception.StructTransformException;
import org.struct.exception.UnSupportConvertOperationException;
import org.struct.util.AnnotationUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * JDK {@link Record}
 *
 * @author TinyZ
 * @date 2022-04-14
 */
public class RecordStructFactory implements StructFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkStructFactory.class);

    private final Class<?> clzOfStruct;
    private final StructWorker<?> worker;
    /**
     * {@link #clzOfStruct}'s all field.
     */
    protected Map<String, FieldDescriptor> beanFieldMap = new ConcurrentHashMap<>();

    public RecordStructFactory(Class<?> clzOfStruct, StructWorker<?> worker) {
        assert clzOfStruct != null && clzOfStruct.isRecord();
        this.clzOfStruct = clzOfStruct;
        this.worker = worker;
    }

    @Override
    public void parseStruct() {
        if (!this.beanFieldMap.isEmpty())
            return;
        Map<String, FieldDescriptor> map = new HashMap<>();
        RecordComponent[] components = this.clzOfStruct.getRecordComponents();
        for (RecordComponent rc : components) {
            FieldDescriptor descriptor = this.resolveFieldDescriptor(rc);
            FieldDescriptor prevFd = map.putIfAbsent(descriptor.getName(), descriptor);
            if (prevFd != null) {
                LOGGER.warn("field descriptor new:{} conflicted with prev:{}.", descriptor, prevFd);
            }
        }
        this.beanFieldMap.putAll(map);
    }

    @Override
    public Object newStructInstance(Object structImpl) {
        if (null == structImpl)
            return Optional.empty();
        RecordComponent[] components = this.clzOfStruct.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] argTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            argTypes[i] = components[i].getType();
            FieldDescriptor d = this.beanFieldMap.get(components[i].getName());
            if (d instanceof OptionalDescriptor od) {
                for (SingleFieldDescriptor sfd : od.getDescriptors()) {
                    Object r = convert(structImpl, sfd);
                    if (null != r) {
                        args[i] = r;
                        break;
                    }
                }
            } else if (d instanceof SingleFieldDescriptor sfd) {
                args[i] = convert(structImpl, sfd);
            }
        }
        try {
            Constructor<?> constructor = this.clzOfStruct.getDeclaredConstructor(argTypes);
            return Optional.of(constructor.newInstance(args));
        } catch (Exception e) {
            throw new RuntimeException("No such constructor with [" + Arrays.toString(argTypes) + "]", e);
        }
    }

    @Override
    public Object getFieldValuesArray(Object src, String[] refKeys) {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            FieldDescriptor descriptor = beanFieldMap.get(refKeys[i]);
            if (!(descriptor instanceof SingleFieldDescriptor sdf)) {
                throw new RuntimeException("No such field: [" + refKeys[i] + "] in source obj:" + src.getClass());
            }
            ary[i] = sdf.getFieldValueFrom(src);
        }
        return ary.length == 1 ? ary[0] : new ArrayKey(ary);
    }

    protected FieldDescriptor resolveFieldDescriptor(RecordComponent rc) {
        StructOptional anSmf;
        FieldDescriptor descriptor;
        if (null != (anSmf = AnnotationUtils.findAnnotation(StructOptional.class, rc))) {
            descriptor = new OptionalDescriptor();
            if (!anSmf.name().isEmpty()) {
                descriptor.setName(anSmf.name());
            }
            String n;
            if (null == (n = descriptor.getName()) || n.isEmpty()) {
                descriptor.setName(rc.getName());
            }
            ((OptionalDescriptor) descriptor).setDescriptors(Stream.of(anSmf.value()).map(sf -> resolveSingleFieldDescriptor(rc, sf)).toArray(SingleFieldDescriptor[]::new));
        } else {
            descriptor = this.resolveSingleFieldDescriptor(rc, AnnotationUtils.findAnnotation(StructField.class, rc));
        }
        return descriptor;
    }

    protected SingleFieldDescriptor resolveSingleFieldDescriptor(RecordComponent rc, StructField annotation) {
        SingleRecordFieldDescriptor descriptor = new SingleRecordFieldDescriptor(annotation, worker.globalStructRequiredValue());
        descriptor.setRc(rc);
        if (null == descriptor.getName() || descriptor.getName().isEmpty()) {
            descriptor.setName(rc.getName());
        }
        //  try resolve field reference.
        worker.handleReferenceFieldValue(this, descriptor);
        return descriptor;
    }

    protected Object convert(Object structImpl, SingleFieldDescriptor descriptor) {
        try {
            Object value = descriptor.getFieldValueFrom(structImpl);
            if (descriptor.isRequired() && !descriptor.isReferenceField()) {
                boolean invalid = value == null
                        || (value instanceof String && ((String) value).isEmpty());
                if (invalid) {
                    throw new IllegalArgumentException("unresolved required clz:" + this.clzOfStruct.getSimpleName()
                            + "#field:" + descriptor.getName() + "'s value. val:" + value);
                }
            }
            Converter converter = descriptor.getConverter();
            if (null != converter) {
                return converter.convert(value, descriptor.getFieldType());
            } else if (descriptor.isReferenceField()) {
                // String refFieldKey = descriptor.getRefFieldUrl();
                // Map<Object, Object> map = this.worker.getRefFieldValuesMap(refFieldKey);
                // if (descriptor.isRequired() && map == null || map.isEmpty()) {
                //     throw new IllegalArgumentException("unresolved reference dependency. key:" + refFieldKey);
                // }
                // String[] refKeys = descriptor.getRefGroupBy().length > 0
                //         ? descriptor.getRefGroupBy()
                //         : descriptor.getRefUniqueKey();
                // Object keys = getFieldValuesArray(structImpl, refKeys);
                // Object val = map.get(keys);
                // if (descriptor.isRequired() && val == null) {
                //     throw new NoSuchFieldReferenceException("unknown dependent field. make sure field's type and name is right. "
                //             + " ref clazz:" + descriptor.getReference().getName()
                //             + ". map key field's name:" + Arrays.toString(refKeys)
                //             + ", actual:" + keys);
                // }
                // if (val != null
                //         && val.getClass().isArray()) {
                //     val = Arrays.copyOf((Object[]) val, ((Object[]) val).length, (Class) descriptor.getFieldType());
                // }
                return handleReferenceFieldValue(structImpl, descriptor);
            } else {
                return ConverterRegistry.convert(value, descriptor.getFieldType());
            }
        } catch (Exception e) {
            String msg = "set instance field's value failure. clz:" + this.clzOfStruct.getSimpleName()
                    + "#field:" + descriptor.getName() + ", msg:" + e.getMessage();
            throw new StructTransformException(msg, e);
        }
    }

    Object handleReferenceFieldValue(Object structImpl, SingleFieldDescriptor fd) {
        String refFieldKey = fd.getRefFieldUrl();
        Map<Object, Object> map = this.worker.getRefFieldValuesMap(refFieldKey);
        if (map == null || map.isEmpty()) {
            if (fd.isRequired()) {
                throw new IllegalArgumentException("unresolved reference dependency. key:" + refFieldKey);
            } else {
                return null;
            }
        }
        String[] refKeys = fd.getRefGroupBy().length > 0
                ? fd.getRefGroupBy()
                : fd.getRefUniqueKey();
        Object keys = this.getFieldValuesArray(structImpl, refKeys);
        Object val;
        if (fd.getAggregateBy().isEmpty()) {
            val = map.get(keys);
        } else {
            //  key value's type
            Class<?> targetFieldType = fd.getFieldType();
            if (keys.getClass().isArray()) {
                int length = Array.getLength(keys);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(map.get(Array.get(keys, i)));
                }
                val = targetFieldType.isArray() ? list.toArray() : list;
            } else if (keys instanceof Collection ck) {
                List<Object> list = new ArrayList<>(ck.size());
                for (Object key : ck) {
                    list.add(map.get(key));
                }
                val = targetFieldType.isArray() ? list.toArray() : list;
            } else if (Map.class.isAssignableFrom(keys.getClass())) {
                throw new UnSupportConvertOperationException("Un support Map.class key yet.");
            } else {
                val = map.get(keys);
            }
        }
        if (fd.isRequired() && val == null) {
            throw new NoSuchFieldReferenceException("unknown dependent field. make sure field's type and name is right. "
                    + " ref clazz:" + fd.getReference().getName()
                    + ". map key field's name:" + Arrays.toString(refKeys)
                    + ", actual:" + keys);
        }
        if (val != null
                && val.getClass().isArray()) {
            val = Arrays.copyOf((Object[]) val, ((Object[]) val).length, (Class) fd.getFieldType());
        }
        return val;
    }

}
