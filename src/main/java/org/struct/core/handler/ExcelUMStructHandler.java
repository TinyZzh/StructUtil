package org.struct.core.handler;

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
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.core.bean.FileExtensionMatcher;
import org.struct.core.bean.WorkerMatcher;
import org.struct.exception.ExcelTransformException;
import org.struct.spi.SPI;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * 使用POI的用户模式解析excel.
 * 优点：支持丰富excel特性
 * 缺点: 当excel文件比较大时，性能很差，会导致oom.
 */
@SPI(name = "excel-user", order = 0)
public class ExcelUMStructHandler implements StructHandler {

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(524288L, WorkerMatcher.HIGHEST,
            FileExtensionMatcher.FILE_XLSX, FileExtensionMatcher.FILE_XLS);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> structHandler, File file) {
        StructSheet annotation = AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct);
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook wb = WorkbookFactory.create(fis);
            Sheet sheet = wb.getSheet(annotation.sheetName());

            Row headRow = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, String> columnFieldMap = resolveExcelColumnToField(headRow);
            FormulaEvaluator evaluator = getFormulaEvaluator(file, wb);
            IntStream.rangeClosed(getFirstRowOrder(annotation, sheet), getLastRowOrder(annotation, sheet))
                    .mapToObj(sheet::getRow)
                    .filter(Objects::nonNull)
                    .map(cells -> setObjectFieldValue(worker, clzOfStruct, cells, columnFieldMap, evaluator))
                    .forEach(structHandler);
        } catch (Exception e) {
            throw new ExcelTransformException(e.getMessage(), e);
        }
    }

    /**
     * @return return the struct sheet formula evaluator by file's name.
     */
    private FormulaEvaluator getFormulaEvaluator(File file, Workbook wb) {
        if (file.getName().toLowerCase().endsWith("xlsx")) {
            return new XSSFFormulaEvaluator((XSSFWorkbook) wb);
        } else {
            return new HSSFFormulaEvaluator((HSSFWorkbook) wb);
        }
    }

    private int getFirstRowOrder(StructSheet annotation, Sheet sheet) {
        if (annotation.startOrder() < 0) {
            return sheet.getFirstRowNum();
        }
        return Math.max(annotation.startOrder(), sheet.getFirstRowNum());
    }

    private int getLastRowOrder(StructSheet annotation, Sheet sheet) {
        if (annotation.endOrder() < 0) {
            return sheet.getLastRowNum();
        }
        return Math.min(annotation.endOrder(), sheet.getLastRowNum());
    }

    private <T> T setObjectFieldValue(StructWorker<T> worker, Class<T> clzOfBean, Row row, Map<Integer, String> columnFieldMap,
                                      FormulaEvaluator evaluator) {
        try {
            T obj = Reflects.newInstance(clzOfBean);
            IntStream.rangeClosed(row.getFirstCellNum(), row.getLastCellNum())
                    .mapToObj(row::getCell)
                    .filter(Objects::nonNull)
                    .forEach(cell -> {
                        Object value = null;
                        try {
                            value = WorkerUtil.getExcelCellValue(cell.getCellTypeEnum(), cell, evaluator);
                        } catch (Exception e) {
                            //  no-op
                        }
                        worker.setObjectFieldValue(obj, columnFieldMap.get(cell.getColumnIndex()), cell.getColumnIndex(), value);
                    });
            worker.afterObjectSetCompleted(obj);
            return obj;
        } catch (Exception e) {
            throw new ExcelTransformException("clz:" + clzOfBean.getName() + ", row:" + row.getRowNum() + ", msg:" + e.getMessage(), e);
        }
    }

    private Map<Integer, String> resolveExcelColumnToField(Row headRow) {
        final Map<Integer, String> map = new HashMap<>();
        for (Cell cell : headRow) {
            map.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
        }
        return map;
    }
}
