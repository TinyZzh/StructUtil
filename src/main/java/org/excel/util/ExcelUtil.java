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
import org.excel.core.ExcelWorker;
import org.excel.core.XlsxSaxWorker;
import org.excel.core.ExcelUserModelWorker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        if (null == annotation){
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
                || Modifier.isAbstract(clzOfList.getModifiers())
                || Modifier.isInterface(clzOfList.getModifiers())) {
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
                || Modifier.isAbstract(clzOfMap.getModifiers())
                || Modifier.isInterface(clzOfMap.getModifiers())
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
                return getNumericValue(cell);
            case STRING:
                return getStringValue(cell);
            case FORMULA:
                if (cell instanceof Cell) {
                    CellValue val = evaluator.evaluate((Cell) cell);
                    return getExcelCellValue(val.getCellTypeEnum(), cell, evaluator);
                } else {
                    return null;
                }
            case BOOLEAN:
                return getBooleanValue(cell);
            default:
                throw new Exception("Unknown Cell type");
        }
    }

    private static Object getNumericValue(Object cell) {
        Double val = cell instanceof Cell ? ((Cell) cell).getNumericCellValue() :
                cell instanceof CellValue ? ((CellValue) cell).getNumberValue() : 0.0D;
        String str = new DecimalFormat("#.00000").format(val);
        if (str.substring(str.indexOf(".")).equals(".00000")) {
            Double dt = Double.parseDouble(str);
            if (dt.longValue() > Integer.MAX_VALUE) {
                return dt.longValue();
            } else
                return dt.intValue();
        } else {
            return Double.parseDouble(str);
        }
    }

    private static Object getStringValue(Object cell) throws Exception {
        String str;
        if (cell instanceof CellValue) {
            str = ((CellValue) cell).getStringValue();
        } else if (cell instanceof Cell) {
            str = ((Cell) cell).getStringCellValue();
        } else {
            throw new Exception("Unknown cell type");
        }
        String lowerStr = str.toLowerCase();
        if (lowerStr.indexOf("|") == 0) {
            // |i1988|1909|1890
            char switchChar = lowerStr.charAt(1);
            String[] array = lowerStr.substring(2).split("\\|");//str.split("\\|i");
            List<Object> list = new ArrayList<Object>();
            if (array.length > 0) {
                for (String s : array) {
                    switch (switchChar) {
                        case 'i':
                            list.add(Integer.parseInt(s));
                            break;
                        case 'd':
                            list.add(Double.parseDouble(s));
                            break;
                        case 's':
                            list.add(s);
                            break;
                        case 'b':
                            list.add(s.toLowerCase().equals("true"));
                            break;
                        default:
                            break;
                    }
                }
            }
            if (list.isEmpty()) {
                return str;
            } else {
                return list.toArray();
            }
        } else if (lowerStr.equals("true") || lowerStr.equals("false")) {
            return lowerStr.equals("true");
        } else {
            return str;
        }
    }

    private static Object getBooleanValue(Object cell) {
        return cell instanceof Cell ? ((Cell) cell).getBooleanCellValue() :
                cell instanceof CellValue && ((CellValue) cell).getBooleanValue();
    }
}
