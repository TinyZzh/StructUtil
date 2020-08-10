/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructField;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.handler.StructHandler;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.exception.StructTransformException;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <T> the target java bean class.
 */
public class StructWorker<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructWorker.class);
    /**
     * the working space path.
     */
    protected String workspace;
    /**
     *
     */
    protected final Class<T> clzOfStruct;
    /**
     * Struct configuration.
     */
    protected StructConfig config;

    protected StructDescriptor descriptor;
    /**
     * {@link #clzOfStruct}'s all field.
     */
    protected Map<String, FieldDescriptor> beanFieldMap = new ConcurrentHashMap<>();
    /**
     * field namespace url - unique key - ref field's value
     */
    protected final Map<String, Map<Object, Object>> tempRefFieldValueMap;

    public StructWorker(String workspace, Class<T> clzOfStruct) {
        this(workspace, clzOfStruct, new StructDescriptor(clzOfStruct), new ConcurrentHashMap<>());
    }

    public StructWorker(String workspace, Class<T> clzOfStruct, StructDescriptor descriptor, Map<String, Map<Object, Object>> tempRefFieldValueMap) {
        this.workspace = workspace;
        this.clzOfStruct = clzOfStruct;
        this.descriptor = descriptor;
        this.tempRefFieldValueMap = tempRefFieldValueMap;
    }

    /// <editor-fold desc="   Protected Methods    "  defaultstate="collapsed">

    protected Map<String, FieldDescriptor> resolveBeanFields(Class<?> clzBean) throws RuntimeException {
        if (!this.beanFieldMap.isEmpty())
            return this.beanFieldMap;
        final Map<String, FieldDescriptor> map = new HashMap<>();
        List<Field> fields = Reflects.resolveAllFields(clzBean, true);
        for (Field field : fields) {
            field.setAccessible(true);

            StructField annotation = AnnotationUtils.findAnnotation(StructField.class, field);
            FieldDescriptor descriptor = new FieldDescriptor();
            descriptor.setField(field);
            if (annotation != null) {
                descriptor.setRequired(annotation.required());
                if (!annotation.name().isEmpty()) {
                    descriptor.setName(annotation.name());
                }
                if (Object.class != annotation.ref()) {
                    descriptor.setReference(annotation.ref());
                    descriptor.setRefGroupBy(annotation.refGroupBy());
                    descriptor.setRefUniqueKey(annotation.refUniqueKey());
                }
                if (Converter.class != annotation.converter()
                        && !Modifier.isInterface(annotation.converter().getModifiers())
                        && !Modifier.isAbstract(annotation.converter().getModifiers())
                ) {
                    descriptor.setConverter(ConverterRegistry.lookupOrDefault(annotation.converter(), annotation.converter()));
                }
            } else {
                descriptor.setRequired(this.config != null && this.config.isStructRequiredDefault());
            }
            if (null == descriptor.getName() || descriptor.getName().isEmpty()) {
                descriptor.setName(field.getName());
            }
            map.put(descriptor.getName(), descriptor);
            //  try resolve field reference.
            this.tryLoadReferenceFieldValue(descriptor);
        }
        this.beanFieldMap.putAll(map);
        return map;
    }

    protected void tryLoadReferenceFieldValue(FieldDescriptor descriptor) throws RuntimeException {
        if (descriptor == null || !descriptor.isReferenceField()) {
            return;
        }
        String clzFieldUrl = descriptor.getReference().getName() + ":" + descriptor.getName();
        if (tempRefFieldValueMap.containsKey(clzFieldUrl)) {
            throw new RuntimeException("loop dependent with key:" + clzFieldUrl + ", prev:" + descriptor.getName());
        }
        Class<?> targetType = descriptor.getFieldType();
        StructWorker<?> subWorker = WorkerUtil.newWorker(this.workspace, descriptor.getReference(), this.tempRefFieldValueMap);
        if (targetType.isArray()) {
            Map<Object, ?> map = subWorker.toListWithGroup(ArrayList::new, descriptor.getRefGroupBy());
            Map<Object, Object> collect = map.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof Collection)
                    .map(entry -> new Object[]{entry.getKey(), ((Collection) entry.getValue()).toArray()})
                    .collect(Collectors.toMap(o -> o[0], o -> o[1]));
            tempRefFieldValueMap.put(clzFieldUrl, collect);
        } else if (Collection.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toListWithGroup(targetType, descriptor.getRefGroupBy());
            tempRefFieldValueMap.put(clzFieldUrl, (Map<Object, Object>) map);
        } else if (Map.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toMapWithGroup(targetType, descriptor.getRefUniqueKey(), descriptor.getRefGroupBy());
            tempRefFieldValueMap.put(clzFieldUrl, (Map<Object, Object>) map);
        } else {
            Map<Object, ?> map = subWorker.toMap(HashMap::new, descriptor.getRefUniqueKey());
            tempRefFieldValueMap.put(clzFieldUrl, (Map<Object, Object>) map);
        }
    }

    /**
     * Create a new {@link #clzOfStruct} instance.
     *
     * @param struct the struct data.
     * @return new instance.
     */
    public Optional<T> createInstance(StructImpl struct) {
        if (struct.isEmpty())
            return Optional.empty();
        T instance = Reflects.newInstance(clzOfStruct);
        beanFieldMap.forEach((k, descriptor) -> setObjFieldValue(instance, descriptor, struct.get(descriptor)));
        return Optional.ofNullable(instance);
    }

    protected void setObjFieldValue(Object instance, FieldDescriptor descriptor, Object value) {
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
        } catch (Exception e) {
            String msg = "set instance field's value failure. clz:" + instance.getClass()
                    + "#field:" + descriptor.getName() + ", msg:" + e.getMessage();
            throw new StructTransformException(msg, e);
        }
    }

    protected void setObjReferenceFieldValue(Object obj, FieldDescriptor descriptor) {
        try {
            String refFieldKey = descriptor.getRefFieldUrl();
            Map<Object, Object> map = tempRefFieldValueMap.get(refFieldKey);
            if (descriptor.isRequired() && map == null || map.isEmpty()) {
                throw new IllegalArgumentException("unresolved reference dependency. key:" + refFieldKey);
            }
            String[] refKeys = descriptor.getRefGroupBy().length > 0
                    ? descriptor.getRefGroupBy()
                    : descriptor.getRefUniqueKey();
            ArrayKey keys = getFieldValueArray(obj, refKeys);
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

    public void setObjReferenceFieldValues(Object instance) {
        // resolve reference field.
        beanFieldMap.values().stream()
                .filter(FieldDescriptor::isReferenceField)
                .forEach(descriptor -> {
                    try {
                        setObjReferenceFieldValue(instance, descriptor);
                    } catch (Exception e) {
                        throw new StructTransformException(e.getMessage(), e);
                    }
                });
    }

    protected ArrayKey getFieldValueArray(Object src, String[] refKeys) throws RuntimeException {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            FieldDescriptor descriptor = beanFieldMap.get(refKeys[i]);
            if (descriptor == null) {
                throw new RuntimeException("No such field: [" + refKeys[i] + "] in source obj:"
                        + src.getClass());
            }
            ary[i] = descriptor.getFieldValue(src);
        }
        return new ArrayKey(ary);
    }

    public StructDescriptor getDescriptor() {
        return descriptor;
    }

    /// </editor-fold>

    /// <editor-fold desc=" Excel Convert Collection "  defaultstate="collapsed">

    public <C extends Collection<T>> C load(TypeRefFactory<C> factory) throws RuntimeException {
        return this.toList(factory);
    }

    public <C extends Collection<T>> C toList(TypeRefFactory<C> factory) throws RuntimeException {
        resolveBeanFields(this.clzOfStruct);
        C list = factory.newInstance();
        handleDataFile(list::add);
        return list;
    }

    /**
     * @param groupFunc the function to generate map key.
     * @param <G>       the group by function.
     * @param <C>       the collection class
     * @return return a map. the map's key generate by #groupFunc
     */
    public <G, C extends Collection<T>> Map<G, C> toListWithGroup(TypeRefFactory<C> factory, Function<T, G> groupFunc) throws RuntimeException {
        resolveBeanFields(this.clzOfStruct);
        Map<G, C> map = new HashMap<>();
        handleDataFile(obj -> {
            Collection<T> list = map.computeIfAbsent(groupFunc.apply(obj), objects -> factory.newInstance());
            list.add(obj);
        });
        return map;
    }

    public <C extends Collection<T>> Map<Object, C> toListWithGroup(TypeRefFactory<C> factory, String[] groupByKey) throws RuntimeException {
        return this.toListWithGroup(factory, obj -> getFieldValueArray(obj, groupByKey));
    }

    public Map<Object, Collection<T>> toListWithGroup(Class<?> clzOfCollection, String[] groupByKey) throws RuntimeException {
        return toListWithGroup(() -> {
            try {
                return WorkerUtil.newListOnly(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, groupByKey);
    }

    public <K, M extends Map<K, T>> M toMap(TypeRefFactory<M> factory, Function<T, K> func) throws RuntimeException {
        resolveBeanFields(this.clzOfStruct);
        M map = factory.newInstance();
        handleDataFile(obj -> map.put(func.apply(obj), obj));
        return map;
    }

    public <M extends Map<Object, T>> M toMap(TypeRefFactory<M> factory, String[] uniqueKey) throws RuntimeException {
        return this.toMap(factory, obj -> getFieldValueArray(obj, uniqueKey));
    }

    public <K, G, M extends Map<K, T>> Map<G, M> toMapWithGroup(TypeRefFactory<M> factory, Function<T, K> keyFunc, Function<T, G> groupFunc) throws RuntimeException {
        resolveBeanFields(this.clzOfStruct);
        Map<G, M> result = new HashMap<>();
        handleDataFile(obj -> {
            G groupBy = groupFunc.apply(obj);
            M map = result.computeIfAbsent(groupBy, objects -> factory.newInstance());
            map.put(keyFunc.apply(obj), obj);
        });
        return result;
    }

    public <M extends Map<Object, T>> Map<Object, M> toMapWithGroup(final TypeRefFactory<M> factory, final String[] uniqueKey, final String[] groupByKey) throws RuntimeException {
        return this.toMapWithGroup(factory, obj -> getFieldValueArray(obj, groupByKey), obj -> getFieldValueArray(obj, uniqueKey));
    }

    public Map<Object, Map<Object, T>> toMapWithGroup(Class<?> clzOfCollection, String[] uniqueKey, String[] groupByKey) throws RuntimeException {
        return toMapWithGroup(() -> {
            try {
                return WorkerUtil.newMap(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, uniqueKey, groupByKey);
    }

    /// </editor-fold>

    public void handleDataFile(Consumer<T> cellHandler) {
        String filePath = WorkerUtil.resolveFilePath(this.workspace, this.descriptor.getFileName());
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }
        List<StructHandler> collected = WorkerUtil.lookupStructHandler(this.descriptor, file);
        for (StructHandler handler : collected) {
            try {
                handler.handle(this, this.clzOfStruct, wrapCellHandler(this.descriptor, cellHandler), file);
                return;
            } catch (Exception e) {
                //  try next handler.
                LOGGER.info("{} handle data file failure. please check the class of struct file and the matcher rules. file:{}",
                        handler.getClass().getName(), file.getName(), e);
            }
        }
        throw new IllegalArgumentException("unknown data file extension. file name:" + file.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Consumer<T> wrapCellHandler(StructDescriptor descriptor, Consumer<T> cellHandler) {
        Consumer<T> handler = cellHandler;
        Class<? extends StructBeanFilter> clzOfFilter = descriptor.getFilter();
        if (StructBeanFilter.class != clzOfFilter
                && !Modifier.isInterface(clzOfFilter.getModifiers())
                && !Modifier.isAbstract(clzOfFilter.getModifiers())
        ) {
            try {
                Constructor<? extends StructBeanFilter> constructor = clzOfFilter.getConstructor(Consumer.class);
                handler = constructor.newInstance(cellHandler);
            } catch (Exception e) {
                String msg = "wrap cell handler failure. struct:" + descriptor.getFileName() + ", sheet:" + descriptor.getSheetName();
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return handler;
    }
}
