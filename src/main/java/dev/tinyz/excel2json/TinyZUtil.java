package dev.tinyz.excel2json;

import com.alibaba.fastjson.JSON;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author TinyZ on 2014/5/23.
 */
public class TinyZUtil {

    /**
     * Excel convert JSON data
     *
     * @param filePath The excel file path
     * @return Return the json string.
     */
    public static StringBuilder excel2Json(String filePath) {
        System.out.println("File : " + filePath);
        StringBuilder sb = new StringBuilder();
        try {
            InputStream inp = new FileInputStream(filePath);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            //
            FormulaEvaluator evaluator;
            if (new File(filePath).getName().toLowerCase().endsWith("xlsx")) {
                evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
            } else {
                evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
            }
            //
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
                        map.put(headRow.getCell(j).getStringCellValue(), covert(cell, evaluator));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }

    private static Object covert(Cell cell, FormulaEvaluator evaluator) throws Exception {
        Object obj = null;
        switch (cell.getCellTypeEnum()) {
            case _NONE:
            case BLANK:
                return "";
            case NUMERIC:
                return getNumericValue(cell);
            case STRING:
                return getStringValue(cell);
            case FORMULA:
                CellValue val = evaluator.evaluate(cell);
                switch (val.getCellTypeEnum()) {
                    case _NONE:
                    case BLANK:
                        return "";
                    case NUMERIC:
                        return getNumericValue(val);
                    case STRING:
                        return getStringValue(val);
                    case BOOLEAN:
                        return getBooleanValue(val);
                    default:
                        throw new Exception("Unknown Cell type");
                }
            case BOOLEAN:
                return getBooleanValue(cell);
            default:
                throw new Exception("Unknown Cell type");
        }
    }

    private static Object getNumericValue(Object cell) throws Exception {
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
        String str = "";
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
            String[] array = lowerStr.substring(2, lowerStr.length()).split("\\|");//str.split("\\|i");
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

    private static Object getBooleanValue(Object cell) throws Exception {
        return cell instanceof Cell ? ((Cell) cell).getBooleanCellValue() :
                cell instanceof CellValue && ((CellValue) cell).getBooleanValue();
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
