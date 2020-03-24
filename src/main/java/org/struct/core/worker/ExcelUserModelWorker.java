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

package org.struct.core.worker;

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
import org.struct.exception.ExcelTransformException;
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
 * StructWorker.
 *
 * @param <T>
 */
public class ExcelUserModelWorker<T> extends StructWorker<T> {

    public ExcelUserModelWorker(String workspace, Class<T> clzOfBean) {
        this(workspace, clzOfBean, new HashMap<>());
    }

    public ExcelUserModelWorker(String workspace, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        super(workspace, clzOfBean, refFieldValueMap);
    }

    @Override
    protected void onLoadStructSheetImpl(Consumer<T> cellHandler, StructSheet annotation, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook wb = WorkbookFactory.create(fis);
            Sheet sheet = wb.getSheet(annotation.sheetName());

            Row headRow = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, String> columnFieldMap = resolveExcelColumnToField(headRow);
            FormulaEvaluator evaluator = getFormulaEvaluator(file, wb);
            IntStream.rangeClosed(getFirstRowOrder(annotation, sheet), getLastRowOrder(annotation, sheet))
                    .mapToObj(sheet::getRow)
                    .filter(Objects::nonNull)
                    .map(cells -> setObjectFieldValue(clzOfBean, cells, columnFieldMap, evaluator))
                    .forEach(cellHandler);
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

    private T setObjectFieldValue(Class<T> clzOfBean, Row row, Map<Integer, String> columnFieldMap,
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
                        setObjectFieldValue(obj, columnFieldMap.get(cell.getColumnIndex()), cell.getColumnIndex(), value);
                    });
            this.afterObjectSetCompleted(obj);
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
