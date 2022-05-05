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
import org.struct.exception.UnSupportConvertOperationException;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        if (this.clzOfStruct.isRecord()) {
            RecordComponent[] components = this.clzOfStruct.getRecordComponents();
            for (RecordComponent rc : components) {
                FieldDescriptor descriptor = this.createFieldDescriptor(rc);
                FieldDescriptor prevFd = map.putIfAbsent(descriptor.getName(), descriptor);
                if (prevFd != null) {
                    LOGGER.warn("field descriptor new:{} conflicted with prev:{}.", descriptor, prevFd);
                }
            }
        } else {
            List<Field> fields = Reflects.resolveAllFields(this.clzOfStruct, true);
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                FieldDescriptor descriptor = this.createFieldDescriptor(field);
                FieldDescriptor prevFd = map.putIfAbsent(descriptor.getName(), descriptor);
                if (prevFd != null) {
                    LOGGER.warn("field descriptor new:{} conflicted with prev:{}.", descriptor, prevFd);
                }
            }
            this.beanFieldsList = map.values().stream().sorted().collect(Collectors.toList());
        }
        this.beanFieldMap.putAll(map);
    }

    protected FieldDescriptor createFieldDescriptor(AnnotatedElement fieldOrRc) {
        StructOptional anno;
        FieldDescriptor descriptor;
        if (null != (anno = AnnotationUtils.findAnnotation(StructOptional.class, fieldOrRc))) {
            descriptor = new OptionalDescriptor(fieldOrRc, anno, this::createSingleFieldDescriptor);
        } else {
            descriptor = this.createSingleFieldDescriptor(fieldOrRc, AnnotationUtils.findAnnotation(StructField.class, fieldOrRc));
        }
        return descriptor;
    }

    protected SingleFieldDescriptor createSingleFieldDescriptor(Object fieldOrRc, StructField annotation) {
        SingleFieldDescriptor descriptor = new SingleFieldDescriptor(fieldOrRc, annotation, worker.globalStructRequiredValue());
        //  try resolve field reference.
        worker.handleReferenceFieldValue(this, descriptor);
        return descriptor;
    }

    @Override
    public Object newStructInstance(Object structImpl) {
        if (null == structImpl)
            return Optional.empty();
        if (this.clzOfStruct.isRecord()) {
            RecordComponent[] components = this.clzOfStruct.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] argTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                argTypes[i] = components[i].getType();
                FieldDescriptor d = this.beanFieldMap.get(components[i].getName());
                if (d instanceof OptionalDescriptor od) {
                    for (SingleFieldDescriptor sfd : od.getDescriptors()) {
                        Object r = this.handleInstanceFieldValue(structImpl, sfd);
                        if (null != r) {
                            args[i] = r;
                            break;
                        }
                    }
                } else if (d instanceof SingleFieldDescriptor sfd) {
                    args[i] = this.handleInstanceFieldValue(structImpl, sfd);
                }
            }
            try {
                Constructor<?> constructor = this.clzOfStruct.getDeclaredConstructor(argTypes);
                return Optional.of(constructor.newInstance(args));
            } catch (Exception e) {
                throw new RuntimeException("No such constructor with [" + Arrays.toString(argTypes) + "]", e);
            }
        } else {
            Object instance = Reflects.newInstance(this.clzOfStruct);
            this.beanFieldsList.forEach(d -> {
                try {
                    if (d instanceof OptionalDescriptor od) {
                        for (SingleFieldDescriptor sfd : od.getDescriptors()) {
                            Object value = this.handleInstanceFieldValue(structImpl, sfd);
                            if (value != null) {
                                sfd.setFieldValue(instance, value);
                                break;
                            }
                        }
                    } else if (d instanceof SingleFieldDescriptor sfd) {
                        sfd.setFieldValue(instance, this.handleInstanceFieldValue(structImpl, sfd));
                    }
                } catch (Exception e) {
                    String msg = "set instance field's value failure. clz:" + this.clzOfStruct.getSimpleName() + "#field:" + d.getName() + ", msg:" + e.getMessage();
                    throw new StructTransformException(msg, e);
                }
            });
            return Optional.ofNullable(instance);
        }
    }

    @Override
    public Object getFieldValuesArray(Object src, String[] refKeys) throws RuntimeException {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            FieldDescriptor descriptor = beanFieldMap.get(refKeys[i]);
            if (descriptor instanceof SingleFieldDescriptor sfd) {
                ary[i] = sfd.getFieldValueFrom(src);
            } else {
                throw new NoSuchFieldReferenceException("No such field: [" + refKeys[i] + "] in source obj:" + src.getClass());
            }
        }
        return ary.length == 1 ? ary[0] : new ArrayKey(ary);
    }

    Object handleInstanceFieldValue(Object structImpl, SingleFieldDescriptor sfd) {
        Object value = sfd.getFieldValueFrom(structImpl);
        if (sfd.isRequired() && !sfd.isReferenceField()) {
            boolean invalid = value == null
                    || (value instanceof String && ((String) value).isEmpty());
            if (invalid) {
                throw new IllegalArgumentException("unresolved required clz:" + this.clzOfStruct.getSimpleName() + "#field:" + sfd.getName() + "'s value. val:" + value);
            }
        }
        Converter converter = sfd.getConverter();
        if (null != converter) {
            return converter.convert(value, sfd.getFieldType());
        } else if (sfd.isReferenceField()) {
            return this.handleReferenceFieldValue(structImpl, sfd);
        } else {
            return ConverterRegistry.convert(value, sfd.getFieldType());
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
        if (fd.isAggregateField()) {
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
        } else {
            val = map.get(keys);
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
