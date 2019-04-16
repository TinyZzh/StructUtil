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

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.excel.annotation.ExcelField;
import org.excel.annotation.ExcelSheet;
import org.excel.util.AnnotationUtils;
import org.excel.util.ConverterUtil;
import org.excel.util.ExcelUtil;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ExcelWorker
 *
 * @param <T>
 */
public class ExcelWorker<T> {

    /**
     * the working space path.
     */
    private String rootPath;
    /**
     *
     */
    private final Class<T> clzOfBean;
    /**
     * {@link #clzOfBean}'s all field.
     */
    private Map<String, Field> beanFieldMap = new HashMap<>();
    /**
     *
     */
    private Map<String, Map<Object, Object>> refFieldValueMap;

    public ExcelWorker(String rootPath, Class<T> clzOfBean) {
        this(rootPath, clzOfBean, new HashMap<>());
    }

    public ExcelWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        this.rootPath = rootPath;
        this.clzOfBean = clzOfBean;
        this.refFieldValueMap = refFieldValueMap;
    }

    public static <D> ExcelWorker<D> of(String rootPath, Class<D> clzOfBean) {
        return new ExcelWorker<>(rootPath, clzOfBean);
    }

    public static <D> ExcelWorker<D> of(String rootPath, Class<D> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        return new ExcelWorker<>(rootPath, clzOfBean, refFieldValueMap);
    }

    private void tryResolveFieldRef(Field field) throws Exception {
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        if (annotation == null || Object.class == annotation.ref()) {
            return;
        }
        String key = annotation.ref().getName() + ":" + resolveColumnName(field);
        if (refFieldValueMap.containsKey(key)) {
            throw new RuntimeException("loop dependent with key:" + key);
        }
        Class<?> targetType = field.getType();
        ExcelWorker<?> subWorker = new ExcelWorker<>(this.rootPath, annotation.ref(), this.refFieldValueMap);
        if (targetType.isArray()) {
            Map<Object, ?> map = subWorker.toListWithGroup(ArrayList::new, annotation.refGroupBy());
            Map<Object, Object> collect = map.entrySet().stream()
                    .filter(entry -> entry instanceof Collection)
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

    private void setRefFieldValue(Object obj, Field field) throws Exception {
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        if (annotation == null)
            return;
        String refFieldKey = getRefFieldKey(field, annotation);
        Map<Object, Object> map = refFieldValueMap.get(refFieldKey);
        if (annotation.required() && map == null || map.isEmpty()) {
            throw new IllegalArgumentException("unresolved loop dependence. key:" + refFieldKey);
        }
        ArrayKey keys = getFieldValueArray(obj, annotation.refUniqueKey());
        Object val = map.get(keys);
        if (annotation.required() && val == null) {
            throw new NoSuchFieldException("unknown dependent field. make sure field's type and name is right. " +
                    "unique keys:" + Arrays.toString(annotation.refUniqueKey())
                    + ", actual:" + keys);
        }
        field.set(obj, val);
    }

    private String getRefFieldKey(Field field, ExcelField annotation) {
        return annotation.ref().getName() + ":" + resolveColumnName(field);
    }

    public <C extends Collection<T>> C load(TypeRefFactory<C> factory) throws Exception {
        return this.toList(factory);
    }

    private ArrayKey getFieldValueArray(Object src, String[] refKeys) throws Exception {
        Object[] ary = new Object[refKeys.length];
        for (int i = 0; i < refKeys.length; i++) {
            Field keyField = beanFieldMap.get(refKeys[i].toLowerCase());
            if (keyField != null) {
                ary[i] = keyField.get(src);
            } else {
                throw new NoSuchFieldException("field: [" + refKeys[i] + "]. source obj:"
                        + src.getClass());
            }
        }
        return new ArrayKey(ary);
    }

    private Map<String, Field> resolveBeanFields(Class<?> clzBean) throws Exception {
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

    private String resolveColumnName(Field field) {
        ExcelField property = field.getAnnotation(ExcelField.class);
        if (property != null) {
            if (!property.name().isEmpty()) {
                return property.name();
            }
        }
        return field.getName();
    }

    private boolean isReferenceField(Field field) {
        Objects.requireNonNull(field);
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        return annotation != null && Object.class != annotation.ref();
    }

    private Optional<Converter<?>> hasCustomConverter(Field field) throws Exception {
        Objects.requireNonNull(field);
        ExcelField annotation = field.getAnnotation(ExcelField.class);
        if (annotation == null
                || Converter.class == annotation.converter()
                || Modifier.isInterface(annotation.converter().getModifiers())
                || Modifier.isAbstract(annotation.converter().getModifiers())
        )
            return Optional.empty();
        return Optional.of(ConverterRegistry.lookup(annotation.converter()));
    }

    /// Excel

    public <C extends Collection<T>> C toList(TypeRefFactory<C> factory) throws Exception {
        resolveBeanFields(this.clzOfBean);
        C list = factory.newInstance();
        onLoadExcelSheet(rootPath, clzOfBean, list::add);
        return list;
    }

    /**
     * @param groupFunc the function to generate map key.
     * @param <G>       the group by function.
     * @param <C>       the collection class
     * @return return a map. the map's key generate by #groupFunc
     */
    public <G, C extends Collection<T>> Map<G, C> toListWithGroup(TypeRefFactory<C> factory, Function<T, G> groupFunc) throws Exception {
        resolveBeanFields(this.clzOfBean);
        Map<G, C> map = new HashMap<>();
        onLoadExcelSheet(rootPath, clzOfBean, obj -> {
            try {
                Collection<T> list = map.computeIfAbsent(groupFunc.apply(obj), objects -> factory.newInstance());
                list.add(obj);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        return map;
    }

    public <C extends Collection<T>> Map<Object, C> toListWithGroup(TypeRefFactory<C> factory, String[] groupByKey) throws Exception {
        return this.toListWithGroup(factory, obj -> {
            try {
                return getFieldValueArray(obj, groupByKey);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public Map<Object, Collection<T>> toListWithGroup(Class<?> clzOfCollection, String[] groupByKey) throws Exception {
        return toListWithGroup(() -> {
            try {
                return ExcelUtil.newListOnly(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, groupByKey);
    }

    public <K, M extends Map<K, T>> M toMap(TypeRefFactory<M> factory, Function<T, K> func) throws Exception {
        resolveBeanFields(this.clzOfBean);
        M map = factory.newInstance();
        onLoadExcelSheet(rootPath, clzOfBean, obj -> map.put(func.apply(obj), obj));
        return map;
    }

    public <M extends Map<Object, T>> M toMap(TypeRefFactory<M> factory, String[] uniqueKey) throws Exception {
        return this.toMap(factory, obj -> {
            try {
                return getFieldValueArray(obj, uniqueKey);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public <K, G, M extends Map<K, T>> Map<G, M> toMapWithGroup(TypeRefFactory<M> factory, Function<T, K> keyFunc, Function<T, G> groupFunc) throws Exception {
        resolveBeanFields(this.clzOfBean);
        Map<G, M> result = new HashMap<>();
        onLoadExcelSheet(rootPath, clzOfBean, obj -> {
            try {
                G groupBy = groupFunc.apply(obj);
                M map = result.computeIfAbsent(groupBy, objects -> factory.newInstance());
                map.put(keyFunc.apply(obj), obj);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        return result;
    }

    public <M extends Map<Object, T>> Map<Object, M> toMapWithGroup(final TypeRefFactory<M> factory, final String[] uniqueKey, final String[] groupByKey) throws Exception {
        return this.toMapWithGroup(factory, obj -> {
            try {
                return getFieldValueArray(obj, groupByKey);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }, obj -> {
            try {
                return getFieldValueArray(obj, uniqueKey);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public Map<Object, Map<Object, T>> toMapWithGroup(Class<?> clzOfCollection, String[] uniqueKey, String[] groupByKey) throws Exception {
        return toMapWithGroup(() -> {
            try {
                return ExcelUtil.newMap(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, uniqueKey, groupByKey);
    }

    public void onLoadExcelSheet(String rootPath, Class<T> clzOfBean, Consumer<T> cellHandler) {
        ExcelSheet annotation = AnnotationUtils.findAnnotation(ExcelSheet.class, clzOfBean);
        if (null == annotation) {
            throw new IllegalArgumentException("class " + clzOfBean + " undefined @ExcelSheet annotation");
        }
        String filePath = resolveFilePath(annotation);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }
        try (FileInputStream fis = new FileInputStream(filePath)) {
            Workbook wb = WorkbookFactory.create(fis);
            Sheet sheet = wb.getSheet(annotation.sheetName());

            Row headRow = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, Field> columnFieldMap = resolveExcelColumnToField(headRow);
            // resolve reference field.
            List<Field> unresolvedField = this.beanFieldMap.values().stream()
                    .filter(this::isReferenceField)
                    .collect(Collectors.toList());
            FormulaEvaluator evaluator = getFormulaEvaluator(file, wb);
            IntStream.rangeClosed(getFirstRowOrder(annotation, sheet), getLastRowOrder(annotation, sheet))
                    .mapToObj(sheet::getRow)
                    .filter(Objects::nonNull)
                    .map(cells -> setObjectFieldValue(clzOfBean, cells, columnFieldMap, unresolvedField, evaluator))
                    .forEach(cellHandler);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Resolve excel file's path.
     * 1. classpath:   the path is class's path
     */
    private String resolveFilePath(ExcelSheet annotation) {
        if (this.rootPath.startsWith("classpath:")) {
            String path = this.rootPath.substring(this.rootPath.indexOf(":") + 1) + "/" + annotation.fileName();
            URL resource = ExcelWorker.class.getResource(path);
            return resource == null
                    ? (this.rootPath + (this.rootPath.endsWith("/") ? "" : "/") + annotation.fileName())
                    : resource.getPath();
        } else if (this.rootPath.startsWith("file:")) {
            return this.rootPath.substring(this.rootPath.indexOf(":") + 1) + "/" + annotation.fileName();
        } else {
            return this.rootPath + "/" + annotation.fileName();
        }
    }

    /**
     * @return return the excel sheet formula evaluator by file's name.
     */
    private FormulaEvaluator getFormulaEvaluator(File file, Workbook wb) {
        if (file.getName().toLowerCase().endsWith("xlsx")) {
            return new XSSFFormulaEvaluator((XSSFWorkbook) wb);
        } else {
            return new HSSFFormulaEvaluator((HSSFWorkbook) wb);
        }
    }

    private int getFirstRowOrder(ExcelSheet annotation, Sheet sheet) {
        if (annotation.startOrder() < 0) {
            return sheet.getFirstRowNum();
        }
        return Math.max(annotation.startOrder(), sheet.getFirstRowNum());
    }

    private int getLastRowOrder(ExcelSheet annotation, Sheet sheet) {
        if (annotation.endOrder() < 0) {
            return sheet.getLastRowNum();
        }
        return Math.min(annotation.endOrder(), sheet.getLastRowNum());
    }

    private T setObjectFieldValue(Class<T> clzOfBean, Row row, Map<Integer, Field> columnFieldMap,
                                  List<Field> unresolvedField, FormulaEvaluator evaluator) {
        try {
            T obj = clzOfBean.getConstructor().newInstance();
            IntStream.rangeClosed(row.getFirstCellNum(), row.getLastCellNum())
                    .mapToObj(row::getCell)
                    .filter(Objects::nonNull)
                    .forEach(cell -> {
                        try {
                            Field field = columnFieldMap.get(cell.getColumnIndex());
                            if (field != null) {
                                Optional<Converter<?>> optional = hasCustomConverter(field);
                                if (optional.isPresent()) {
                                    field.set(obj, optional.get().apply(ExcelUtil.getExcelCellValue(cell.getCellTypeEnum(), cell, evaluator)));
                                } else if (isReferenceField(field)) {
                                    this.setRefFieldValue(obj, field);
                                } else {
                                    field.set(obj, ConverterUtil.covert(ExcelUtil.getExcelCellValue(cell.getCellTypeEnum(), cell, evaluator), field.getType()));
                                }
                            }
                        } catch (Exception e) {
                            String msg = "cell column index:" + cell.getColumnIndex() + ", msg:" + e.getMessage();
                            throw new RuntimeException(msg, e);
                        }
                    });
            for (Field field : unresolvedField) {
                this.setRefFieldValue(obj, field);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("clz:" + clzOfBean.getName() + ", row:" + row.getRowNum() + ", msg:" + e.getMessage(), e);
        }
    }

    private Map<Integer, Field> resolveExcelColumnToField(Row headRow) {
        final Map<Integer, Field> map = new HashMap<>();
        for (Cell cell : headRow) {
            Field field = this.beanFieldMap.get(cell.getStringCellValue().toLowerCase().trim());
            if (null != field) {
                map.put(cell.getColumnIndex(), field);
            }
        }
        return map;
    }
}
