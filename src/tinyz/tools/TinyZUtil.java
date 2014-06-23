package tinyz.tools;

import com.google.gson.Gson;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Created by TinyZ on 2014/5/23.
 */
public class TinyZUtil {

    public static final Gson GSON = new Gson();

    /**
     * Excel convert JSON data
     * @param filePath
     * @return
     */
    public static StringBuffer Excel2Json(String filePath) {
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
                        map.put(headRow.getCell(j).getStringCellValue(), readCell(cell));
                    }
                }
                if (!map.isEmpty()) {
                    String content = GSON.toJson(map, HashMap.class);
                    sb.append(content);
                    if (i < totalRowNum - 1) {
                        sb.append(",");
                    }
                    sb.append("\r\n");
                }
            }
            sb.append("]");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    /**
     * 获取Cell
     * @param cell
     * @return
     */
    private static Object readCell(Cell cell) {
        String str = "";
        Object obj = null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                Double d = Double.valueOf(cell.getNumericCellValue());
                str = new DecimalFormat("#.00000").format(d);
                if (str.substring(str.indexOf(".")).equals(".00000")) {
                    Double dt = Double.valueOf(Double.parseDouble(str));
                    obj = Integer.valueOf(dt.intValue());
                } else {
                    obj = Double.valueOf(Double.parseDouble(str));
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_STRING:
                str = cell.getStringCellValue();
                if ((str.equals("true")) || (str.equals("false"))) {
                    obj = Boolean.valueOf(Boolean.parseBoolean(str));
                } else if (str.contains("|i")) {
                    String[] array = str.split("\\|i");
                    if (array.length != 0) {
                        Integer[] numArray = new Integer[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            numArray[(k - 1)] = Integer.valueOf(Integer.parseInt(array[k]));
                        }
                        obj = numArray;
                    }
                } else if (str.contains("|d")) {
                    String[] array = str.split("\\|d");
                    if (array.length != 0) {
                        Double[] numArray = new Double[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            numArray[(k - 1)] = Double.valueOf(Double.parseDouble(array[k]));
                        }
                        obj = numArray;
                    }
                } else if (str.contains("|s")) {
                    String[] array = str.split("\\|s");
                    if (array.length != 0) {
                        String[] strArray = new String[array.length - 1];
                        for (int k = 1; k < array.length; k++) {
                            strArray[(k - 1)] = array[k];
                        }
                        obj = strArray;
                    }
                } else {
                    obj = str;
                }
                break;
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_ERROR:
                throw new UnknownError("Unknown Cell type" + cell.getCellType());
            default:
                throw new UnknownError("Unknown Cell type");
        }
        return obj;
    }

    /**
     * 写入文件
     * @param filePath
     * @param sb
     */
    public static void writeFile(String filePath, StringBuffer sb) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
            bw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
