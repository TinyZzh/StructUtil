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

package org.excel.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Support Utilitily.
 *
 * @author TinyZ.
 * @version 2020.03.06
 */
public class SupportUtil {

    /**
     * 是否输出全部sheet
     */
    private static final String SUPPORT_OUTPUT_SHEET_ALL = "support.output.excel.all";

    private SupportUtil() {
        //  no-op
    }

    /**
     * Convert Excel to JSON data
     */
    public static void supportExcel2Xml(String resPath, String outputPath) {
        System.getProperty(SUPPORT_OUTPUT_SHEET_ALL, "false");
        try {
            InputStream inp = new FileInputStream(resPath);
            Workbook wb = WorkbookFactory.create(inp);
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                outputXml(sheet, outputPath);
            }
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputXml(Sheet sheet, String outputPath) {
        try {
            int firstRowNum = sheet.getFirstRowNum();
            int totalRowNum = sheet.getPhysicalNumberOfRows();

            List<Map<String, Object>> list = new ArrayList<>();

            // 读取首行 -> 设置为字段名
            Row headRow = sheet.getRow(firstRowNum);
            for (int i = firstRowNum + 1; i < totalRowNum; i++) {
                Row row = sheet.getRow(i);
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String headCellStr = headRow.getCell(j).getStringCellValue();
                        if (headCellStr != null && !headCellStr.isEmpty()) {
                            map.put(headCellStr, covertExcelCellToData(cell));
                        }
                    }
                }
                list.add(map);
            }
            if (list.isEmpty()) {
                return;
            }
            Document document = DocumentHelper.createDocument();
            document.setXMLEncoding("utf-8");
            Element root = document.addElement("root");
            for (Map<String, Object> map : list) {
                Element child = root.addElement("child");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getValue() == null) {
                        child.addElement(entry.getKey());
                    } else if (entry.getValue().getClass().isArray()) {
                        child.addElement(entry.getKey()).setText(String.valueOf(entry.getValue()));
                    } else {
                        child.addElement(entry.getKey()).setText(String.valueOf(entry.getValue()));
                    }
                }
            }
            File outputFile = null;
            File file = new File(outputPath);
            if (file.isDirectory()) {
                outputFile = new File(file.getAbsolutePath() + "/" + sheet.getSheetName() + ".xml");
            } else {
                outputFile = file;
            }

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");// 设置XML文件的编码格式
            XMLWriter writer = new XMLWriter(new FileWriter(outputFile), format);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert Excel to JSON data
     */
    public static StringBuffer supportExcel2Json(String filePath) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStream inp = new FileInputStream(filePath);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            int firstRowNum = sheet.getFirstRowNum();
            int totalRowNum = sheet.getPhysicalNumberOfRows();

            sb.append("[");
            sb.append("\r\n");
            // 读取首行 -> 设置为字段名
            Row headRow = sheet.getRow(firstRowNum);
            for (int i = firstRowNum + 1; i < totalRowNum; i++) {
                Row row = sheet.getRow(i);
                Map<String, Object> map = new TreeMap<String, Object>();
                for (int j = row.getFirstCellNum(); j < row.getPhysicalNumberOfCells(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        map.put(headRow.getCell(j).getStringCellValue(), covertExcelCellToData(cell));
                    }
                }
                if (!map.isEmpty()) {
//                    String content = GSON.toJson(map, HashMap.class);
//                    sb.append(content);
                    if (i < totalRowNum - 1) {
                        sb.append(",");
                    }
                    sb.append("\r\n");
                }
            }
            sb.append("]");
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    /**
     * 解析Excel的Cell值
     */
    private static Object covertExcelCellToData(Cell cell) {
        String str = "";
        Object obj = null;
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                Double d = cell.getNumericCellValue();
                str = new DecimalFormat("#.00000").format(d);
                if (str.substring(str.indexOf(".")).equals(".00000")) {
                    obj = ((Double) Double.parseDouble(str)).intValue();
                } else {
                    obj = Double.parseDouble(str);
                }
                break;
            case BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case STRING:
                str = cell.getStringCellValue();
                if ((str.equals("true")) || (str.equals("false"))) {
                    obj = Boolean.parseBoolean(str);
                } else if (str.startsWith("|i_")) {
                    String[] array = str.substring(3).split("\\|");
                    if (array.length != 0) {
                        Integer[] numArray = new Integer[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            numArray[(k - 1)] = Integer.parseInt(array[k]);
                        }
                        obj = numArray;
                    }
                } else if (str.startsWith("|f_")) {
                    String[] array = str.substring(3).split("\\|");
                    if (array.length != 0) {
                        Float[] numArray = new Float[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            numArray[(k - 1)] = Float.parseFloat(array[k]);
                        }
                        obj = numArray;
                    }
                } else if (str.startsWith("|d_")) {
                    String[] array = str.substring(3).split("\\|");
                    if (array.length != 0) {
                        Double[] numArray = new Double[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            numArray[(k - 1)] = Double.parseDouble(array[k]);
                        }
                        obj = numArray;
                    }
                } else if (str.startsWith("|s_")) {
                    String[] array = str.substring(3).split("\\|");
                    if (array.length != 0) {
                        String[] strArray = new String[array.length - 1];
                        System.arraycopy(array, 1, strArray, 0, array.length - 1);
                        obj = strArray;
                    }
                } else {
                    obj = str;
                }
                break;
            case BLANK:
                return obj;
            case FORMULA:
            case ERROR:
                throw new UnknownError("Unknown Cell type" + cell.getCellType());
            default:
                throw new UnknownError("Unknown Cell type");
        }
        return obj;
    }
}
