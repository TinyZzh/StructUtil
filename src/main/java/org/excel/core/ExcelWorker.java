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

package org.excel.core;

import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.excel.exception.ExcelTransformException;
import org.excel.exception.IllegalAccessPropertyException;
import org.excel.util.AnnotationUtils;
import org.excel.util.ConverterUtil;
import org.excel.util.ExcelUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <T> the target java bean class.
 */
public abstract class ExcelWorker<T> {

    /**
     * the working space path.
     */
    protected String rootPath;
    /**
     *
     */
    protected final Class<T> clzOfBean;
    /**
     * {@link #clzOfBean}'s all field.
     */
    protected Map<String, Field> beanFieldMap = new HashMap<>();
    /**
     *
     */
    protected Map<String, Map<Object, Object>> refFieldValueMap;

    public ExcelWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        this.rootPath = rootPath;
        this.clzOfBean = clzOfBean;
        this.refFieldValueMap = refFieldValueMap;
    }

    /// <editor-fold desc="   Protected Methods    "  defaultstate="collapsed">

    protected void tryResolveFieldRef(Field field) throws RuntimeException {
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        if (annotation == null || Object.class == annotation.ref()) {
            return;
        }
        String key = annotation.ref().getName() + ":" + resolveColumnName(field);
        if (refFieldValueMap.containsKey(key)) {
            throw new RuntimeException("loop dependent with key:" + key);
        }
        Class<?> targetType = field.getType();
        ExcelWorker<?> subWorker = ExcelUtil.newWorker(this.rootPath, annotation.ref(), this.refFieldValueMap);
        if (targetType.isArray()) {
            Map<Object, ?> map = subWorker.toListWithGroup(ArrayList::new, annotation.refGroupBy());
            Map<Object, Object> collect = map.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof Collection)
                    .map(entry -> new Object[]{entry.getKey(), ((Collection) entry.getValue()).toArray()})
                    .collect(Collectors.toMap(o -> o[0], o -> o[1]));
            refFieldValueMap.put(key, collect);
        } else if (Collection.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toListWithGroup(targetType, annotation.refGroupBy());
            refFieldValueMap.put(key, (Map<Object, Object>) map);
        } else if (Map.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toMapWithGroup(targetType, annotation.refUniqueKey(), annotation.refGroupBy());
            refFieldValueMap.put(key, (Map<Object, Object>) map);
        } else {
            Map<Object, ?> map = subWorker.toMap(HashMap::new, annotation.refUniqueKey());
            refFieldValueMap.put(key, (Map<Object, Object>) map);
        }
    }

    protected void setRefFieldValue(Object obj, Field field) throws Exception {
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        String refFieldKey = getRefFieldKey(field, annotation);
        Map<Object, Object> map = refFieldValueMap.get(refFieldKey);
        if (annotation.required() && map == null || map.isEmpty()) {
            throw new IllegalArgumentException("unresolved loop dependence. key:" + refFieldKey);
        }
        String[] refKeys = annotation.refGroupBy().length > 0
                ? annotation.refGroupBy()
                : annotation.refUniqueKey();
        ArrayKey keys = getFieldValueArray(obj, refKeys);
        Object val = map.get(keys);
        if (annotation.required() && val == null) {
            throw new NoSuchFieldException("unknown dependent field. make sure field's type and name is right. "
                    + " ref clazz:" + annotation.ref().getName()
                    + ". map key field's name:" + Arrays.toString(refKeys)
                    + ", actual:" + keys);
        }
        if (val != null
                && val.getClass().isArray()) {
            val = Arrays.copyOf((Object[]) val, ((Object[]) val).length, (Class) field.getType());
        }
        field.set(obj, val);
    }

    protected String getRefFieldKey(Field field, ExcelField annotation) {
        return annotation.ref().getName() + ":" + resolveColumnName(field);
    }

    protected ArrayKey getFieldValueArray(Object src, String[] refKeys) throws RuntimeException {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            Field keyField = beanFieldMap.get(refKeys[i].toLowerCase());
            if (keyField == null) {
                throw new RuntimeException("No such field: [" + refKeys[i] + "] in source obj:"
                        + src.getClass());
            }
            try {
                ary[i] = keyField.get(src);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessPropertyException(e.getMessage(), e);
            }
        }
        return new ArrayKey(ary);
    }

    protected Map<String, Field> resolveBeanFields(Class<?> clzBean) throws RuntimeException {
        final Map<String, Field> map = new HashMap<>();
        Field[] fields = clzBean.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = resolveColumnName(field);
            map.put(columnName.toLowerCase(), field);

            //  try resolve field reference.
            tryResolveFieldRef(field);
        }
        this.beanFieldMap.putAll(map);
        return map;
    }

    protected String resolveColumnName(Field field) {
        ExcelField property = field.getAnnotation(ExcelField.class);
        if (property != null) {
            if (!property.name().isEmpty()) {
                return property.name();
            }
        }
        return field.getName();
    }

    protected boolean isReferenceField(Field field) {
        Objects.requireNonNull(field);
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        return annotation != null && Object.class != annotation.ref();
    }

    protected Optional<Converter<?>> hasCustomConverter(Field field) {
        Objects.requireNonNull(field);
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        if (annotation == null
                || Converter.class == annotation.converter()
                || Modifier.isInterface(annotation.converter().getModifiers())
                || Modifier.isAbstract(annotation.converter().getModifiers())
        )
            return Optional.empty();
        return Optional.of(ConverterRegistry.lookupOrDefault(field.getType(), annotation.converter()));
    }

    protected void setObjectFieldValue(Object instance, String fileName, int columnIndex, Object formattedValue) {
        try {
            Field field = beanFieldMap.get(fileName);
            if (field != null) {
                Optional<Converter<?>> optional = hasCustomConverter(field);
                if (optional.isPresent()) {
                    field.set(instance, optional.get().apply(formattedValue));
                } else if (isReferenceField(field)) {
                    setRefFieldValue(instance, field);
                } else {
                    field.set(instance, ConverterUtil.covert(formattedValue, field.getType()));
                }
            }
        } catch (Exception e) {
            String msg = "cell column index:" + columnIndex + ", msg:" + e.getMessage();
            throw new ExcelTransformException(msg, e);
        }
    }

    protected void afterObjectSetCompleted(Object instance) {
        // resolve reference field.
        beanFieldMap.values().stream()
                .filter(this::isReferenceField)
                .forEach(field -> {
                    try {
                        setRefFieldValue(instance, field);
                    } catch (Exception e) {
                        throw new ExcelTransformException(e.getMessage(), e);
                    }
                });
    }

    /// </editor-fold>

    /// <editor-fold desc=" Excel Convert Collection "  defaultstate="collapsed">

    public <C extends Collection<T>> C load(TypeRefFactory<C> factory) throws RuntimeException {
        return this.toList(factory);
    }

    public <C extends Collection<T>> C toList(TypeRefFactory<C> factory) throws RuntimeException {
        resolveBeanFields(this.clzOfBean);
        C list = factory.newInstance();
        onLoadExcelSheet(clzOfBean, list::add);
        return list;
    }

    /**
     * @param groupFunc the function to generate map key.
     * @param <G>       the group by function.
     * @param <C>       the collection class
     * @return return a map. the map's key generate by #groupFunc
     */
    public <G, C extends Collection<T>> Map<G, C> toListWithGroup(TypeRefFactory<C> factory, Function<T, G> groupFunc) throws RuntimeException {
        resolveBeanFields(this.clzOfBean);
        Map<G, C> map = new HashMap<>();
        onLoadExcelSheet(clzOfBean, obj -> {
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
                return ExcelUtil.newListOnly(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, groupByKey);
    }

    public <K, M extends Map<K, T>> M toMap(TypeRefFactory<M> factory, Function<T, K> func) throws RuntimeException {
        resolveBeanFields(this.clzOfBean);
        M map = factory.newInstance();
        onLoadExcelSheet(clzOfBean, obj -> map.put(func.apply(obj), obj));
        return map;
    }

    public <M extends Map<Object, T>> M toMap(TypeRefFactory<M> factory, String[] uniqueKey) throws RuntimeException {
        return this.toMap(factory, obj -> getFieldValueArray(obj, uniqueKey));
    }

    public <K, G, M extends Map<K, T>> Map<G, M> toMapWithGroup(TypeRefFactory<M> factory, Function<T, K> keyFunc, Function<T, G> groupFunc) throws RuntimeException {
        resolveBeanFields(this.clzOfBean);
        Map<G, M> result = new HashMap<>();
        onLoadExcelSheet(clzOfBean, obj -> {
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
                return ExcelUtil.newMap(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, uniqueKey, groupByKey);
    }

    /// </editor-fold>

    protected abstract void onLoadExcelSheetImpl(Consumer<T> cellHandler, ExcelSheet annotation, File file);

    public void onLoadExcelSheet(Class<T> clzOfBean, Consumer<T> cellHandler) {
        ExcelSheet annotation = AnnotationUtils.findAnnotation(ExcelSheet.class, clzOfBean);
        ExcelUtil.checkMissingExcelSheetAnnotation(annotation, clzOfBean);
        String filePath = ExcelUtil.resolveFilePath(this.rootPath, annotation);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }

        onLoadExcelSheetImpl(cellHandler, annotation, file);
    }
}
