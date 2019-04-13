package dev.tinyz.excel2json.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
