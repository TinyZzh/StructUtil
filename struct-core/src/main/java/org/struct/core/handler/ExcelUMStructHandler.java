/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

package org.struct.core.handler;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructInternal;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.File;
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

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(StructInternal.HANDLER_XLSX_UM_LENGTH_THRESHOLD, WorkerMatcher.HIGHEST,
            FileExtensionMatcher.FILE_XLSX, FileExtensionMatcher.FILE_XLS);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        try (Workbook wb = WorkbookFactory.create(file, null, true)) {
            Sheet sheet = wb.getSheet(descriptor.getSheetName());
            if (sheet == null)
                throw new StructTransformException("Sheet not found: " + descriptor.getSheetName());

            int firstRowOrder = this.getFirstRowOrder(descriptor, sheet);
            Row headRow = sheet.getRow(Math.max(0, firstRowOrder - 1));
            Map<Integer, String> columnFieldMap = resolveExcelColumnToField(headRow);
            FormulaEvaluator evaluator = getFormulaEvaluator(file, wb);
            IntStream.rangeClosed(firstRowOrder, getLastRowOrder(descriptor, sheet))
                    .mapToObj(sheet::getRow)
                    .filter(Objects::nonNull)
                    .forEach(cells -> handleObjField(worker, clzOfStruct, cells, columnFieldMap, evaluator, cellHandler));
        } catch (Exception e) {
            throw new StructTransformException(e.getMessage(), e);
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

    private int getFirstRowOrder(StructDescriptor descriptor, Sheet sheet) {
        if (descriptor.getStartOrder() < 0) {
            return sheet.getFirstRowNum();
        }
        return Math.max(descriptor.getStartOrder(), sheet.getFirstRowNum());
    }

    private int getLastRowOrder(StructDescriptor descriptor, Sheet sheet) {
        if (descriptor.getEndOrder() < 0) {
            return sheet.getLastRowNum();
        }
        return Math.min(descriptor.getEndOrder(), sheet.getLastRowNum());
    }

    private <T> void handleObjField(StructWorker<T> worker, Class<T> clzOfBean, Row row, Map<Integer, String> columnFieldMap,
                                    FormulaEvaluator evaluator, Consumer<T> cellHandler) {
        if (row.getFirstCellNum() < 0 || row.getLastCellNum() < 0) {
            //  this row is empty row. the row does not contain any cells
            return;
        }
        try {
            StructImpl rowStruct = new StructImpl();
            IntStream.rangeClosed(row.getFirstCellNum(), row.getLastCellNum())
                    .mapToObj(row::getCell)
                    .filter(Objects::nonNull)
                    .forEach(cell -> {
                        Object value = null;
                        try {
                            value = this.getExcelCellValue(cell.getCellType(), cell, evaluator);
                        } catch (Exception e) {
                            //  no-op
                        }
                        rowStruct.add(columnFieldMap.get(cell.getColumnIndex()), value);
                    });
            worker.createInstance(rowStruct).ifPresent(cellHandler);
        } catch (Exception e) {
            throw new StructTransformException("clz:" + clzOfBean.getName() + ", the row number:" + row.getRowNum() + ", msg:" + e.getMessage(), e);
        }
    }

    private Map<Integer, String> resolveExcelColumnToField(Row headRow) {
        final Map<Integer, String> map = new HashMap<>();
        for (Cell cell : headRow) {
            map.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
        }
        return map;
    }

    Object getExcelCellValue(CellType cellType, Object cell, FormulaEvaluator evaluator) throws Exception {
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
                    return getExcelCellValue(val.getCellType(), cell, evaluator);
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
}
