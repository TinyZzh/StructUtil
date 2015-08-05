package dev.tinyz.excel2json;

import com.alibaba.fastjson.JSON;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author TinyZ on 2014/5/23.
 */
public class TinyZUtil {

    public static String E2Json(String filePath) {
        try {
            InputStream inp = new FileInputStream(filePath);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            boolean isLoadData = false;
            Map<Object, Object> cache = new HashMap<>();
            StringBuilder sbData = null;
            Map<String, Integer> except = new HashMap<>();
            for (int i = firstRowNum; i < lastRowNum + 1; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (!isLoadData) {
                        Cell cell = row.getCell(0);
                        String obj = String.valueOf(readCell(cell));
                        switch (obj.toLowerCase()) {
                            case "class":
                            case "key":
                            case "redis":
                            case "fields":
                            case "groupby":
                                cache.put(obj, readCell(row.getCell(1)));
                                break;
                            case "except":
                                List<String> eList = (List<String>) readCell(row.getCell(1));
                                for (String s : eList) {
                                    except.put(s, 1);
                                }
                                break;
                            case "data":
                                isLoadData = true;
                                break;
                            default:
                                cache.put(obj, readCell(row.getCell(1)));
                                break;
                        }
                    } else {
                        List<Object> data = null;
                        if (except.isEmpty()) {
                            data = TinyZUtil.loadCache(sheet, i, lastRowNum);
                        } else {
                            data = TinyZUtil.loadCache(sheet, i, lastRowNum, except);
                        }
//                        cache.put("data", TinyZUtil.loadCache(sheet, i, lastRowNum));
//                        List<Object> data = TinyZUtil.loadCache(sheet, i, lastRowNum);
                        if (!data.isEmpty()) {
                            cache.put("data", "<data>");
                            sbData = new StringBuilder();
                            sbData.append("\r\n[\r\n");
                            for (Object o : data) {
                                sbData.append(JSON.toJSONString(o)).append(",\r\n");
                            }
                            sbData.delete(sbData.length() - 3, sbData.length()).append("\r\n]\r\n");
//                            System.out.println(String.valueOf(sbData));
                        }
                        break;
                    }
                }
            }
            if (cache.containsKey("data")) {
                String json = JSON.toJSONString(cache);
                if (sbData != null && sbData.length() > 0) {
                    json = json.replace("\"<data>\"", String.valueOf(sbData));
                }
                return json;
            } else {
                return null;
            }
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从excel表中加载配置表数据
     *
     * @param sheet       表单
     * @param firstRowNum 首行
     * @param lastRowNum  尾行
     * @param except      排除列表
     * @return 数据列表
     */
    public static List<Object> loadCache(Sheet sheet, int firstRowNum, int lastRowNum, Map<String, Integer> except) {
        // The first row must be the field name row, defined the field name. and the cell value must be not null.
        Row headRow = sheet.getRow(firstRowNum);
        int physicalNumberOfCells = headRow.getPhysicalNumberOfCells();
        List<Object> data = new ArrayList<>();
        for (int i = firstRowNum + 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<String, Object> map = new TreeMap<String, Object>();
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null) {
                    String fieldName = headRow.getCell(j).getStringCellValue();
                    if (!except.containsKey(fieldName)) {
                        map.put(fieldName, readCell(cell));
                    }
                }
            }
            // the filed name count must equal the map size
            if (!map.isEmpty() && physicalNumberOfCells == map.size()) {
                data.add(map);
            }
        }
        return data;
    }

    /**
     * 从excel表中加载配置表数据
     *
     * @param sheet       表单
     * @param firstRowNum 首行
     * @param lastRowNum  尾行
     * @return 数据列表
     */
    public static List<Object> loadCache(Sheet sheet, int firstRowNum, int lastRowNum) {
        // The first row must be the field name row, defined the field name. and the cell value must be not null.
        Row headRow = sheet.getRow(firstRowNum);
        int physicalNumberOfCells = headRow.getPhysicalNumberOfCells();
        List<Object> data = new ArrayList<>();
        for (int i = firstRowNum + 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<String, Object> map = new TreeMap<String, Object>();
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null) {
                    map.put(headRow.getCell(j).getStringCellValue(), readCell(cell));
                }
            }
            // the filed name count must equal the map size
            if (!map.isEmpty() && physicalNumberOfCells == map.size()) {
                data.add(map);
            }
        }
        return data;
    }

    /**
     * Excel convert JSON data
     *
     * @param filePath The excel file path
     * @return Return the json string.
     */
    public static StringBuilder Excel2Json(String filePath) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream inp = new FileInputStream(filePath);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            sb.append("[\r\n");
            // The first row must be the field name row, defined the field name. and the cell value must be not null.
            Row headRow = sheet.getRow(firstRowNum);
            int physicalNumberOfCells = headRow.getPhysicalNumberOfCells();
            for (int i = firstRowNum + 1; i < lastRowNum + 1; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, Object> map = new TreeMap<String, Object>();
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        map.put(headRow.getCell(j).getStringCellValue(), readCell(cell));
                    }
                }
                // the filed name count must equal the map size
                if (!map.isEmpty() && physicalNumberOfCells == map.size()) {
                    String content = JSON.toJSONString(map);
                    if (!"".equals(content)) {
                        sb.append(content).append(",\r\n");
                    }
                }
            }
            sb.delete(sb.length() - 3, sb.length()).append("\r\n]");
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    /**
     * 获取Cell
     */
    private static Object readCell(Cell cell) {
        String str = "";
        Object obj = null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                Double d = cell.getNumericCellValue();
                str = new DecimalFormat("#.00000").format(d);
                if (str.substring(str.indexOf(".")).equals(".00000")) {
                    Double dt = Double.parseDouble(str);
                    obj = dt.intValue();
                } else {
                    obj = Double.parseDouble(str);
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_STRING:
                str = cell.getStringCellValue();
                if (str.indexOf("|") == 0) {
                    // |i1988|1909|1890
                    char switchChar = str.charAt(1);
                    String[] array = str.substring(2, str.length()).split("\\|");//str.split("\\|i");
                    List<Object> list = new ArrayList<Object>();
                    if (array.length > 0) {
                        for (String s : array) {
                            switch (switchChar) {
                                case 'i':
                                case 'I':
                                    list.add(Integer.parseInt(s));
                                    break;
                                case 'd':
                                case 'D':
                                    list.add(Double.parseDouble(s));
                                    break;
                                case 's':
                                case 'S':
                                    list.add(s);
                                    break;
                                case 'b':
                                case 'B':
                                    list.add(s.toLowerCase().equals("true"));
                                    break;
                                case 'a':
                                case 'A':
                                    // Array  split with ','
                                    list.add(Arrays.asList(s.split(",")));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    if (!list.isEmpty()) {
                        obj = list.toArray();
                    } else {
                        obj = str;
                    }
                } else if (str.equals("true") || str.equals("false")) {
                    obj = str.equals("true");
                } else {
                    obj = str;
                }
                // Method 1 : (Deprecated)
//                if ((str.equals("true")) || (str.equals("false"))) {
//                    obj = Boolean.parseBoolean(str);
//                } else if (str.contains("|i")) {
//                    String[] array = str.substring(2, str.length() - 1).split("\\|");//str.split("\\|i");
//                    if (array.length != 0) {
//                        Integer[] numArray = new Integer[array.length - 1];
//                        for (int k = 1; k < array.length; k++) {
//                            numArray[(k - 1)] = Integer.parseInt(array[k]);
//                        }
//                        obj = numArray;
//                    }
//                } else if (str.contains("|d")) {
//                    String[] array = str.split("\\|d");
//                    if (array.length != 0) {
//                        Double[] numArray = new Double[array.length - 1];
//                        for (int k = 1; k < array.length; k++) {
//                            numArray[(k - 1)] = Double.parseDouble(array[k]);
//                        }
//                        obj = numArray;
//                    }
//                } else if (str.contains("|s")) {
//                    String[] array = str.split("\\|s");
//                    if (array.length != 0) {
//                        String[] strArray = new String[array.length - 1];
//                        System.arraycopy(array, 1, strArray, 0, array.length - 1);
//                        obj = strArray;
//                    }
//                } else {
//                    obj = str;
//                }
                break;
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_ERROR:
                return null;
//                throw new UnknownError("Unknown Cell type" + cell.getCellType());
            default:
                throw new UnknownError("Unknown Cell type");
        }
        return obj;
    }

    /**
     * 写入文件
     *
     * @param filePath The write file path
     * @param content  The content
     */
    public static void writeFile(String filePath, String content) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            bw.write(content);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
