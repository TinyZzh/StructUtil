/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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
import org.struct.annotation.StructSheet;
import org.struct.core.bean.FieldDescriptor;
import org.struct.core.handler.StructHandler;
import org.struct.exception.ExcelTransformException;
import org.struct.exception.IllegalAccessPropertyException;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.util.AnnotationUtils;
import org.struct.util.ConverterUtil;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * {@link #clzOfStruct}'s all field.
     */
    protected Map<String, FieldDescriptor> beanFieldMap = new ConcurrentHashMap<>();
    /**
     * field namespace url - unique key - ref field's value
     */
    protected final Map<String, Map<Object, Object>> tempRefFieldValueMap;

    public StructWorker(String workspace, Class<T> clzOfStruct) {
        this(workspace, clzOfStruct, new ConcurrentHashMap<>());
    }

    public StructWorker(String workspace, Class<T> clzOfStruct, Map<String, Map<Object, Object>> tempRefFieldValueMap) {
        this.workspace = workspace;
        this.clzOfStruct = clzOfStruct;
        this.tempRefFieldValueMap = tempRefFieldValueMap;
    }

    /// <editor-fold desc="   Protected Methods    "  defaultstate="collapsed">

    protected Map<String, FieldDescriptor> resolveBeanFields(Class<?> clzBean) throws RuntimeException {
        final Map<String, FieldDescriptor> map = new HashMap<>();
        Field[] fields = clzBean.getDeclaredFields();
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
            }
            if (null == descriptor.getName() || descriptor.getName().isEmpty()) {
                descriptor.setName(field.getName());
            }
            map.put(descriptor.getName(), descriptor);
            //  try resolve field reference.
            this.resolveReferenceFieldValue(descriptor);
        }
        this.beanFieldMap.putAll(map);
        return map;
    }

    protected void resolveReferenceFieldValue(FieldDescriptor descriptor) throws RuntimeException {
        if (descriptor == null || !descriptor.isReferenceField()) {
            return;
        }
        String clzFieldUrl = descriptor.getReference().getName() + ":" + descriptor.getName();
        if (tempRefFieldValueMap.containsKey(clzFieldUrl)) {
            throw new RuntimeException("loop dependent with key:" + clzFieldUrl + ", prev:" + descriptor.getName());
        }
        Class<?> targetType = descriptor.getField().getType();
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

    public void setObjectFieldValue(Object instance, String fileName, int columnIndex, Object formattedValue) {
        try {
            FieldDescriptor descriptor = beanFieldMap.get(fileName);
            if (descriptor != null) {
                Converter converter = descriptor.getConverter();
                if (null != converter) {
                    descriptor.getField().set(instance, converter.convert(formattedValue, descriptor.getField().getType()));
                } else if (descriptor.isReferenceField()) {
                    this.setRefFieldValue(instance, descriptor);
                } else {
                    descriptor.getField().set(instance, ConverterUtil.covert(formattedValue, descriptor.getField().getType()));
                }
            }
        } catch (Exception e) {
            String msg = "cell column index:" + columnIndex + ", msg:" + e.getMessage();
            throw new ExcelTransformException(msg, e);
        }
    }

    public void afterObjectSetCompleted(Object instance) {
        // resolve reference field.
        beanFieldMap.values().stream()
                .filter(FieldDescriptor::isReferenceField)
                .forEach(descriptor -> {
                    try {
                        setRefFieldValue(instance, descriptor);
                    } catch (Exception e) {
                        throw new ExcelTransformException(e.getMessage(), e);
                    }
                });
    }

    protected void setRefFieldValue(Object obj, FieldDescriptor descriptor) throws Exception {
        String refFieldKey = descriptor.getRefFieldUrl();
        Map<Object, Object> map = tempRefFieldValueMap.get(refFieldKey);
        if (descriptor.isRequired() && map == null || map.isEmpty()) {
            throw new IllegalArgumentException("unresolved loop dependence. key:" + refFieldKey);
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
            val = Arrays.copyOf((Object[]) val, ((Object[]) val).length, (Class) descriptor.getField().getType());
        }
        descriptor.getField().set(obj, val);
    }

    protected ArrayKey getFieldValueArray(Object src, String[] refKeys) throws RuntimeException {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            FieldDescriptor descriptor = beanFieldMap.get(refKeys[i]);
            if (descriptor == null) {
                throw new RuntimeException("No such field: [" + refKeys[i] + "] in source obj:"
                        + src.getClass());
            }
            try {
                ary[i] = descriptor.getField().get(src);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessPropertyException(e.getMessage(), e);
            }
        }
        return new ArrayKey(ary);
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
        StructSheet annotation = AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct);
        WorkerUtil.checkMissingExcelSheetAnnotation(annotation, clzOfStruct);
        String filePath = WorkerUtil.resolveFilePath(this.workspace, annotation);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }
        List<StructHandler> collected = WorkerUtil.lookupStructHandler(annotation, file);
        for (StructHandler handler : collected) {
            try {
                handler.handle(this, this.clzOfStruct, cellHandler, file);
                return;
            } catch (Exception e) {
                //  try next handler.
                LOGGER.info("{} handle data file failure. please check the class of struct file and the matcher rules. file:{}",
                        handler.getClass().getName(), file.getName(), e);
            }
        }
        throw new IllegalArgumentException("unknown data file extension. file name:" + file.getName());
    }
}
