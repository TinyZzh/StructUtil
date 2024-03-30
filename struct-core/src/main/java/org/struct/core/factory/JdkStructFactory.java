package org.struct.core.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.core.ArrayKey;
import org.struct.core.FieldDescriptor;
import org.struct.core.OptionalDescriptor;
import org.struct.core.SingleFieldDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.exception.StructTransformException;
import org.struct.exception.UnSupportConvertOperationException;
import org.struct.util.AnnotationUtils;
import org.struct.util.ConverterUtil;
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

/**
 * Struct Factory Impl.
 *
 * @author TinyZ
 * @since 2022-04-14
 */
public final class JdkStructFactory implements StructFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkStructFactory.class);

    private final Class<?> clzOfStruct;
    private final StructWorker<?> worker;
    /**
     * {@link #clzOfStruct}'s all field.
     */
    private final Map<String, FieldDescriptor> beanFieldMap = new HashMap<>();

    private List<FieldDescriptor> beanFieldsList;

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
            FieldDescriptor[] descriptors = new FieldDescriptor[components.length];
            for (int i = 0; i < components.length; i++) {
                FieldDescriptor descriptor = this.createFieldDescriptor(components[i]);
                descriptors[i] = descriptor;
                FieldDescriptor prevFd = map.putIfAbsent(descriptor.getName(), descriptor);
                if (prevFd != null) {
                    LOGGER.warn("field descriptor new:{} conflicted with prev:{}.", descriptor, prevFd);
                }
            }
            this.beanFieldsList = List.of(descriptors);
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
            this.beanFieldsList = map.values().stream().sorted().toList();
        }
        this.beanFieldMap.putAll(map);
    }

    FieldDescriptor createFieldDescriptor(AnnotatedElement fieldOrRc) {
        StructOptional anno;
        FieldDescriptor descriptor;
        if (null != (anno = AnnotationUtils.findAnnotation(StructOptional.class, fieldOrRc))) {
            descriptor = new OptionalDescriptor(fieldOrRc, anno, this::createSingleFieldDescriptor);
        } else {
            descriptor = this.createSingleFieldDescriptor(fieldOrRc, AnnotationUtils.findAnnotation(StructField.class, fieldOrRc));
        }
        return descriptor;
    }

    SingleFieldDescriptor createSingleFieldDescriptor(Object fieldOrRc, StructField annotation) {
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
            int size = this.beanFieldsList.size();
            Object[] args = new Object[size];
            Class<?>[] argTypes = new Class<?>[size];
            this.forEachBeanFields(structImpl, (i, sfd, v) -> {
                args[i] = v;
                argTypes[i] = sfd.getFieldType();
                if (structImpl instanceof StructImpl impl) {
                    impl.add(sfd.getName(), v, true);
                }
            });
            try {
                Constructor<?> constructor = this.clzOfStruct.getDeclaredConstructor(argTypes);
                return Optional.of(constructor.newInstance(args));
            } catch (Exception e) {
                throw new RuntimeException("No such constructor with [" + Arrays.toString(argTypes) + "]", e);
            }
        } else {
            Object instance = Reflects.newInstance(this.clzOfStruct);
            this.forEachBeanFields(structImpl, (i, sfd, v) -> {
                sfd.setFieldValue(instance, v);
                if (structImpl instanceof StructImpl impl) {
                    impl.add(sfd.getName(), v, true);
                }
            });
            return Optional.of(instance);
        }
    }

    void forEachBeanFields(Object structImpl, TriConsumer<Integer, SingleFieldDescriptor, Object> consumer) {
        List<FieldDescriptor> list = this.beanFieldsList;
        for (int i = 0; i < list.size(); i++) {
            FieldDescriptor fd = list.get(i);
            try {
                if (fd instanceof OptionalDescriptor ofd) {
                    for (SingleFieldDescriptor sfd : ofd.getDescriptors()) {
                        Object value = this.handleInstanceFieldValue(structImpl, sfd);
                        if (value != null) {
                            consumer.accept(i, sfd, value);
                            break;
                        }
                    }
                } else if (fd instanceof SingleFieldDescriptor sfd) {
                    consumer.accept(i, sfd, this.handleInstanceFieldValue(structImpl, sfd));
                }
            } catch (Exception e) {
                String msg = "set instance field's value failure. clz:" + this.clzOfStruct.getSimpleName() + "#field:" + fd.getName() + ", msg:" + e.getMessage();
                throw new StructTransformException(msg, e);
            }
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
        Object fv;
        if (null != converter) {
            fv = converter.convert(value, sfd.getFieldType());
        } else if (sfd.isReferenceField()) {
            if (sfd.isBasicTypeCollection()) {
                fv = ConverterRegistry.convertCollection(value, sfd.getFieldType(), sfd.getReference());
            } else {
                fv = this.handleReferenceFieldValue(structImpl, sfd);
            }
        } else {
            fv = ConverterRegistry.convert(value, sfd.getFieldType());
        }
        if (sfd.isCached()) {
            if (fv instanceof String str) {
                fv = str.intern();
            }
        }
        return fv;
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
        String[] refKeys;
        Object keys, val;
        if (fd.isAggregateField()) {
            refKeys = new String[]{fd.getAggregateBy()};
            keys = this.getFieldValuesArray(structImpl, refKeys);
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
            refKeys = fd.getRefGroupBy().length > 0
                    ? fd.getRefGroupBy()
                    : fd.getRefUniqueKey();
            keys = this.getFieldValuesArray(structImpl, refKeys);
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

    @FunctionalInterface
    public interface TriConsumer<K, V, S> {

        /**
         * Performs the operation given the specified arguments.
         *
         * @param k the first input argument
         * @param v the second input argument
         * @param s the third input argument
         */
        void accept(K k, V v, S s);
    }

}
