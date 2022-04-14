package org.struct.core.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.core.ArrayKey;
import org.struct.core.FieldDescriptor;
import org.struct.core.OptionalDescriptor;
import org.struct.core.SingleFieldDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.exception.StructTransformException;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 使用反射赋值.
 *
 * @author TinyZ
 * @date 2022-04-14
 */
public class JdkStructFactory implements StructFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkStructFactory.class);

    private final Class<?> clzOfStruct;
    private final StructWorker<?> worker;
    /**
     * {@link #clzOfStruct}'s all field.
     */
    protected Map<String, FieldDescriptor> beanFieldMap = new ConcurrentHashMap<>();

    private List<FieldDescriptor> beanFieldsList = new ArrayList<>();

    public JdkStructFactory(Class<?> clzOfStruct, StructWorker<?> worker) {
        this.clzOfStruct = clzOfStruct;
        this.worker = worker;
    }

    @Override
    public void parseStruct() throws RuntimeException {
        if (!this.beanFieldMap.isEmpty())
            return;
        final Map<String, FieldDescriptor> map = new HashMap<>();
        List<Field> fields = Reflects.resolveAllFields(this.clzOfStruct, true);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            FieldDescriptor descriptor = this.resolveFieldDescriptor(field);
            FieldDescriptor prevFd = map.putIfAbsent(descriptor.getName(), descriptor);
            if (prevFd != null) {
                LOGGER.warn("field descriptor new:{} conflicted with prev:{}.", descriptor, prevFd);
            }
        }
        this.beanFieldMap.putAll(map);
        this.beanFieldsList = map.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Object newStructInstance(Object structImpl) {
        if (null == structImpl)
            return Optional.empty();
        Object instance = Reflects.newInstance(this.clzOfStruct);
        this.beanFieldsList.forEach(d -> {
            if (d instanceof OptionalDescriptor) {
                for (SingleFieldDescriptor fd : ((OptionalDescriptor) d).getDescriptors()) {
                    if (this.setObjFieldValue(instance, fd, fd.getFieldValue(structImpl))) {
                        break;
                    }
                }
            } else if (d instanceof SingleFieldDescriptor) {
                this.setObjFieldValue(instance, (SingleFieldDescriptor) d, ((SingleFieldDescriptor) d).getFieldValue(structImpl));
            }
        });
        return Optional.ofNullable(instance);
    }

    @Override
    public Object getFieldValuesArray(Object src, String[] refKeys) throws RuntimeException {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            FieldDescriptor descriptor = beanFieldMap.get(refKeys[i]);
            if (!(descriptor instanceof SingleFieldDescriptor)) {
                throw new RuntimeException("No such field: [" + refKeys[i] + "] in source obj:"
                        + src.getClass());
            }
            ary[i] = ((SingleFieldDescriptor) descriptor).getFieldValue(src);
        }
        return ary.length == 1 ? ary[0] : new ArrayKey(ary);
    }

    protected FieldDescriptor resolveFieldDescriptor(Field field) {
        StructOptional anSmf;
        FieldDescriptor descriptor;
        if (null != (anSmf = AnnotationUtils.findAnnotation(StructOptional.class, field))) {
            descriptor = new OptionalDescriptor();
            if (!anSmf.name().isEmpty()) {
                descriptor.setName(anSmf.name());
            }
            String n;
            if (null == (n = descriptor.getName()) || n.isEmpty()) {
                descriptor.setName(field.getName());
            }
            ((OptionalDescriptor) descriptor).setDescriptors(Stream.of(anSmf.value()).map(sf -> resolveSingleFieldDescriptor(field, sf)).toArray(SingleFieldDescriptor[]::new));
        } else {
            descriptor = this.resolveSingleFieldDescriptor(field, AnnotationUtils.findAnnotation(StructField.class, field));
        }
        return descriptor;
    }

    protected SingleFieldDescriptor resolveSingleFieldDescriptor(Field field, StructField annotation) {
        SingleFieldDescriptor descriptor = new SingleFieldDescriptor(annotation, worker.globalStructRequiredValue());
        descriptor.setField(field);
        if (null == descriptor.getName() || descriptor.getName().isEmpty()) {
            descriptor.setName(field.getName());
        }
        //  try resolve field reference.
        worker.handleReferenceFieldValue(this, descriptor);
        return descriptor;
    }

    protected boolean setObjFieldValue(Object instance, SingleFieldDescriptor descriptor, Object value) {
        try {
            if (descriptor.isRequired() && !descriptor.isReferenceField()) {
                boolean invalid = value == null
                        || (value instanceof String && ((String) value).isEmpty());
                if (invalid) {
                    throw new IllegalArgumentException("unresolved required clz:" + instance.getClass()
                            + "#field:" + descriptor.getName() + "'s value. val:" + value);
                }
            }
            Converter converter = descriptor.getConverter();
            if (null != converter) {
                descriptor.setFieldValue(instance, converter.convert(value, descriptor.getFieldType()));
            } else if (descriptor.isReferenceField()) {
                this.setObjReferenceFieldValue(instance, descriptor);
            } else {
                descriptor.setFieldValue(instance, ConverterRegistry.convert(value, descriptor.getFieldType()));
            }
            return descriptor.getFieldValue(instance) != null;
        } catch (Exception e) {
            String msg = "set instance field's value failure. clz:" + instance.getClass()
                    + "#field:" + descriptor.getName() + ", msg:" + e.getMessage();
            throw new StructTransformException(msg, e);
        }
    }

    protected void setObjReferenceFieldValue(Object obj, SingleFieldDescriptor descriptor) {
        try {
            String refFieldKey = descriptor.getRefFieldUrl();
            Map<Object, Object> map = this.worker.getRefFieldValuesMap(refFieldKey);
            if (descriptor.isRequired() && map == null || map.isEmpty()) {
                throw new IllegalArgumentException("unresolved reference dependency. key:" + refFieldKey);
            }
            String[] refKeys = descriptor.getRefGroupBy().length > 0
                    ? descriptor.getRefGroupBy()
                    : descriptor.getRefUniqueKey();
            Object keys = getFieldValuesArray(obj, refKeys);
            Object val = map.get(keys);
            if (descriptor.isRequired() && val == null) {
                throw new NoSuchFieldReferenceException("unknown dependent field. make sure field's type and name is right. "
                        + " ref clazz:" + descriptor.getReference().getName()
                        + ". map key field's name:" + Arrays.toString(refKeys)
                        + ", actual:" + keys);
            }
            if (val != null
                    && val.getClass().isArray()) {
                val = Arrays.copyOf((Object[]) val, ((Object[]) val).length, (Class) descriptor.getFieldType());
            }
            descriptor.setFieldValue(obj, val);
        } catch (Exception e) {
            throw new StructTransformException(e.getMessage(), e);
        }
    }

}
