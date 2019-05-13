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

package org.excel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.excel.annotation.ExcelSheet;
import org.excel.core.ExcelUserModelWorker;
import org.excel.core.ExcelWorker;
import org.excel.core.XlsxSaxWorker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TinyZ.
 * @version 2019.03.23
 */
public final class ExcelUtil {

    private ExcelUtil() {
        //  no-op
    }

    public static <T> ExcelWorker<T> newWorker(String rootPath, Class<T> clzOfBean) {
        return newWorker(rootPath, clzOfBean, new HashMap<>());
    }

    public static <T> ExcelWorker<T> newWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        ExcelSheet annotation = AnnotationUtils.findAnnotation(ExcelSheet.class, clzOfBean);
        checkMissingExcelSheetAnnotation(annotation, clzOfBean);
        switch (annotation.exportStrategy()) {
            case USER_MODEL:
                return new ExcelUserModelWorker<>(rootPath, clzOfBean, refFieldValueMap);
            case EVENT_MODEL:
                return new XlsxSaxWorker<>(rootPath, clzOfBean, refFieldValueMap);
            case AUTO:
                File file = new File(resolveFilePath(rootPath, annotation));
                if (file.exists() && file.canRead()) {
                    //  file large than 1mb
                    if (file.length() > 1048576) {
                        return new XlsxSaxWorker<>(rootPath, clzOfBean, refFieldValueMap);
                    } else {
                        return new ExcelUserModelWorker<>(rootPath, clzOfBean, refFieldValueMap);
                    }
                }
        }
        //  default
        return new XlsxSaxWorker<>(rootPath, clzOfBean, refFieldValueMap);
    }

    public static void checkMissingExcelSheetAnnotation(ExcelSheet annotation, Class<?> clz) {
        if (null == annotation) {
            throw new IllegalArgumentException("class: " + clz + " undefined @ExcelSheet annotation");
        }
    }

    /**
     * Resolve excel file's path.
     * 1. classpath:   the path is class's path
     */
    public static String resolveFilePath(String filepath, ExcelSheet annotation) {
        if (filepath.startsWith("classpath:")) {
            String path = filepath.substring(filepath.indexOf(":") + 1) + "/" + annotation.fileName();
            URL resource = ExcelWorker.class.getResource(path);
            return resource == null
                    ? (filepath + (filepath.endsWith("/") ? "" : "/") + annotation.fileName())
                    : resource.getPath();
        } else if (filepath.startsWith("file:")) {
            return filepath.substring(filepath.indexOf(":") + 1) + "/" + annotation.fileName();
        } else {
            return filepath + "/" + annotation.fileName();
        }
    }

    private static <T, C extends Collection<T>> C newList(Class<C> clzOfList) throws Exception {
        C list;
        if (!(Collection.class.isAssignableFrom(clzOfList))) {
            throw new IllegalArgumentException("class " + clzOfList + " is not Collection.");
        }
        if (clzOfList.isInterface()
                || Modifier.isAbstract(clzOfList.getModifiers())
                || Modifier.isInterface(clzOfList.getModifiers())) {
            list = (C) new ArrayList<T>();
        } else {
            Constructor<C> constructor = clzOfList.getConstructor();
            list = constructor.newInstance();
        }
        return list;
    }

    public static <T> Collection<T> newListOnly(Class<?> clzOfList) throws Exception {
        Collection<T> list;
        if (!(Collection.class.isAssignableFrom(clzOfList))) {
            throw new IllegalArgumentException("class " + clzOfList + " is not Collection.");
        }
        if (clzOfList.isInterface()
                || Modifier.isInterface(clzOfList.getModifiers())
                || Modifier.isAbstract(clzOfList.getModifiers())) {
            list = new ArrayList<>();
        } else {
            Constructor<?> constructor = clzOfList.getConstructor();
            list = (Collection<T>) constructor.newInstance();
        }
        return list;
    }

    public static <T> Map<Object, T> newMap(Class<?> clzOfMap) throws Exception {
        Map<Object, T> map;
        if (clzOfMap == null
                || clzOfMap.isInterface()
                || Modifier.isInterface(clzOfMap.getModifiers())
                || Modifier.isAbstract(clzOfMap.getModifiers())
        ) {
            map = new HashMap<>();
        } else {
            Constructor<?> constructor = clzOfMap.getConstructor();
            map = (Map<Object, T>) constructor.newInstance();
        }
        return map;
    }

    public static Object getExcelCellValue(CellType cellType, Object cell, FormulaEvaluator evaluator) throws Exception {
        switch (cellType) {
            case _NONE:
                return null;
            case BLANK:
                return "";
            case NUMERIC:
                Double numeric;
                if (cell instanceof Cell)
                    numeric = ((Cell) cell).getNumericCellValue();
                else if (cell instanceof CellValue)
                    numeric = ((CellValue) cell).getNumberValue();
                else
                    numeric = 0.0D;
                if (numeric == numeric.longValue()) {
                    if (numeric.longValue() > Integer.MAX_VALUE) {
                        return numeric.longValue();
                    } else
                        return numeric.intValue();
                } else {
                    return numeric;
                }
            case STRING:
                if (cell instanceof Cell) {
                    return ((Cell) cell).getStringCellValue();
                } else if (cell instanceof CellValue) {
                    return ((CellValue) cell).getStringValue();
                } else {
                    return "";
                }
            case FORMULA:
                if (cell instanceof Cell) {
                    CellValue val = evaluator.evaluate((Cell) cell);
                    return getExcelCellValue(val.getCellTypeEnum(), cell, evaluator);
                } else {
                    return null;
                }
            case BOOLEAN:
                if (cell instanceof Cell)
                    return ((Cell) cell).getBooleanCellValue();
                else if (cell instanceof CellValue)
                    return ((CellValue) cell).getBooleanValue();
                else
                    return false;
            default:
                throw new Exception("Unknown Cell type");
        }
    }

    public static void test() {
        System.out.println("x");
    }
}
