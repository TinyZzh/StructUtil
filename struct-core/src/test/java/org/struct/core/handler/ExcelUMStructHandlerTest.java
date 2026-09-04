/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.exception.StructTransformException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author TinyZ.
 * @version 2022.09.23
 */
class ExcelUMStructHandlerTest {

    @Test
    public void testGetExcelCellValue() throws Exception {
        Cell cell = Mockito.mock(Cell.class);

        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Assertions.assertNull(handler.getExcelCellValue(CellType._NONE, cell, formula));
        Assertions.assertEquals("", handler.getExcelCellValue(CellType.BLANK, cell, formula));
        //   numeric
        Mockito.doReturn(1.00D).when(cell).getNumericCellValue();
        Assertions.assertEquals(1, handler.getExcelCellValue(CellType.NUMERIC, cell, formula));
        CellValue cellValue = new CellValue(1.00D);
        Assertions.assertEquals(1, handler.getExcelCellValue(CellType.NUMERIC, cellValue, formula));
        Assertions.assertEquals(0, handler.getExcelCellValue(CellType.NUMERIC, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Assertions.assertEquals("1", handler.getExcelCellValue(CellType.STRING, cell, formula));
        cellValue = new CellValue("1");
        Assertions.assertEquals("1", handler.getExcelCellValue(CellType.STRING, cellValue, formula));
        Assertions.assertEquals("", handler.getExcelCellValue(CellType.STRING, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.reset(formula);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Mockito.doReturn(new CellValue("1")).when(formula).evaluate(Mockito.any(Cell.class));
        Assertions.assertEquals("1", handler.getExcelCellValue(CellType.FORMULA, cell, formula));
        Assertions.assertNull(handler.getExcelCellValue(CellType.FORMULA, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn(true).when(cell).getBooleanCellValue();
        Assertions.assertEquals(true, handler.getExcelCellValue(CellType.BOOLEAN, cell, formula));
        cellValue = CellValue.TRUE;
        Assertions.assertEquals(true, handler.getExcelCellValue(CellType.BOOLEAN, cellValue, formula));
        Assertions.assertEquals(false, handler.getExcelCellValue(CellType.BOOLEAN, new Object(), formula));
        //
        Mockito.reset(cell);
        try {
            Assertions.assertEquals(true, handler.getExcelCellValue(CellType.ERROR, cell, formula));
        } catch (Exception e) {
            return;
        }
        Assertions.fail();
    }

    /**
     * A formula cell that evaluates to a STRING.
     * <p>
     * POI forbids reading a typed value straight from a formula cell
     * ({@code getStringCellValue()} throws {@link IllegalStateException}), so the
     * handler must read the <em>evaluated</em> value. Previously it recursed with the
     * raw formula cell, the exception was swallowed and the cell value was lost.
     */
    @Test
    public void testFormulaProducingString() throws Exception {
        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Cell cell = Mockito.mock(Cell.class);
        //  real POI behaviour: cannot read a string from a formula cell
        Mockito.doThrow(new IllegalStateException("Cannot get a STRING value from a FORMULA cell"))
                .when(cell).getStringCellValue();
        Mockito.doReturn(new CellValue("hello")).when(formula).evaluate(Mockito.any(Cell.class));

        Assertions.assertEquals("hello", handler.getExcelCellValue(CellType.FORMULA, cell, formula));
    }

    /**
     * A formula cell that evaluates to a BOOLEAN - same problem as with strings.
     */
    @Test
    public void testFormulaProducingBoolean() throws Exception {
        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Cell cell = Mockito.mock(Cell.class);
        Mockito.doThrow(new IllegalStateException("Cannot get a BOOLEAN value from a FORMULA cell"))
                .when(cell).getBooleanCellValue();
        Mockito.doReturn(CellValue.TRUE).when(formula).evaluate(Mockito.any(Cell.class));

        Assertions.assertEquals(true, handler.getExcelCellValue(CellType.FORMULA, cell, formula));
    }

    /**
     * A formula cell that evaluates to a NUMBER still works.
     */
    @Test
    public void testFormulaProducingNumber() throws Exception {
        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Cell cell = Mockito.mock(Cell.class);
        Mockito.doReturn(new CellValue(42.0D)).when(formula).evaluate(Mockito.any(Cell.class));

        Assertions.assertEquals(42, handler.getExcelCellValue(CellType.FORMULA, cell, formula));
    }

    //  ------------------------------------------------------------------
    //  handler() level coverage: the branches above are only reachable through
    //  a real workbook, so the sheets are generated with POI into a temp folder.
    //  ------------------------------------------------------------------

    /**
     * A workbook without the configured sheet must fail loudly instead of silently
     * producing an empty result.
     */
    @Test
    public void testSheetNotFound(@TempDir Path tempDir) throws Exception {
        File file = this.writeWorkbook(tempDir, "Data", "missing.xlsx", sheet -> {
            sheet.createRow(0).createCell(0).setCellValue("key");
            sheet.createRow(1).createCell(0).setCellValue(1);
        });
        StructWorker<SheetBean> worker = this.newWorker(tempDir, SheetBean.class);

        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        StructTransformException e = Assertions.assertThrows(StructTransformException.class,
                () -> handler.handle(worker, SheetBean.class, b -> Assertions.fail("no bean expected"), file));
        Assertions.assertTrue(e.getMessage().contains("Sheet not found: Sheet1"), e.getMessage());
    }

    /**
     * One pass over a sheet that contains every numeric shape plus a blank header column
     * and a completely empty row.
     */
    @Test
    public void testHandleResolvesEveryCellTypeAndSkipsEmptyRows(@TempDir Path tempDir) throws Exception {
        File file = this.writeWorkbook(tempDir, "Sheet1", "sheet.xlsx", sheet -> {
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("key");
            //  a blank header -> the column is not mapped to any field
            head.createCell(1).setCellValue("   ");
            head.createCell(2).setCellValue("name");
            head.createCell(3).setCellValue("ratio");
            head.createCell(4).setCellValue("big");

            Row first = sheet.createRow(1);
            first.createCell(0).setCellValue(1);
            first.createCell(1).setCellValue("ignored");
            first.createCell(2).setCellValue("a");
            first.createCell(3).setCellValue(1.5D);
            first.createCell(4).setCellValue(3_000_000_000L);

            //  a row without any cell -> skipped
            sheet.createRow(2);

            Row third = sheet.createRow(3);
            third.createCell(0).setCellValue(2);
            third.createCell(1).setCellValue("ignored");
            third.createCell(2).setCellValue("b");
            third.createCell(3).setCellValue(2.0D);
            third.createCell(4).setCellValue(7);
        });
        StructWorker<SheetBean> worker = this.newWorker(tempDir, SheetBean.class);

        List<SheetBean> beans = new ArrayList<>();
        new ExcelUMStructHandler().handle(worker, SheetBean.class, beans::add, file);

        Assertions.assertEquals(2, beans.size());
        SheetBean first = beans.get(0);
        Assertions.assertEquals(1, first.key);
        Assertions.assertEquals("a", first.name);
        //  a fractional value keeps its fraction
        Assertions.assertEquals(1.5D, first.ratio);
        //  larger than Integer.MAX_VALUE -> kept as a long
        Assertions.assertEquals(3_000_000_000L, first.big);

        SheetBean second = beans.get(1);
        Assertions.assertEquals(2, second.key);
        Assertions.assertEquals("b", second.name);
        //  a whole number is narrowed to int, then widened back to double
        Assertions.assertEquals(2.0D, second.ratio);
        Assertions.assertEquals(7L, second.big);
    }

    /**
     * A negative {@code startOrder} means "start at the very first row of the sheet",
     * so the header row is consumed as a data row too.
     */
    @Test
    public void testNegativeStartOrder(@TempDir Path tempDir) throws Exception {
        File file = this.writeWorkbook(tempDir, "Sheet1", "start.xlsx", sheet -> {
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("key");
            head.createCell(1).setCellValue("name");

            Row first = sheet.createRow(1);
            first.createCell(0).setCellValue("1");
            first.createCell(1).setCellValue("a");

            Row second = sheet.createRow(2);
            second.createCell(0).setCellValue("2");
            second.createCell(1).setCellValue("b");
        });
        StructWorker<NegativeStartBean> worker = this.newWorker(tempDir, NegativeStartBean.class);

        List<NegativeStartBean> beans = new ArrayList<>();
        new ExcelUMStructHandler().handle(worker, NegativeStartBean.class, beans::add, file);

        Assertions.assertEquals(3, beans.size());
        //  the header row itself is the first data row
        Assertions.assertEquals("key", beans.get(0).key);
        Assertions.assertEquals("name", beans.get(0).name);
        Assertions.assertEquals("1", beans.get(1).key);
        Assertions.assertEquals("2", beans.get(2).key);
    }

    /**
     * A cell whose value cannot be extracted (an ERROR cell) is dropped, which leaves the
     * <em>required</em> field unresolved, so the whole row is rejected with a message that
     * points at the offending row number.
     */
    @Test
    public void testUnresolvableCellIsDroppedAndRowIsRejected(@TempDir Path tempDir) throws Exception {
        File file = this.writeWorkbook(tempDir, "Sheet1", "err.xlsx", sheet -> {
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("key");
            head.createCell(1).setCellValue("name");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellErrorValue(FormulaError.DIV0.getCode());
            row.createCell(1).setCellValue("boom");
        });
        StructWorker<RequiredKeyBean> worker = this.newWorker(tempDir, RequiredKeyBean.class);

        ExcelUMStructHandler handler = new ExcelUMStructHandler();
        StructTransformException e = Assertions.assertThrows(StructTransformException.class,
                () -> handler.handle(worker, RequiredKeyBean.class, b -> Assertions.fail("no bean expected"), file));
        Assertions.assertTrue(e.getMessage().contains("the row number:1"), e.getMessage());
    }

    /**
     * {@code row.getLastCellNum() < 0} while {@code getFirstCellNum() >= 0} cannot be
     * produced by POI (both are {@code -1} for a row without cells), so the row is mocked
     * and the row handler is invoked reflectively.
     */
    @Test
    public void testRowWithNegativeLastCellNum() throws Exception {
        Row row = Mockito.mock(Row.class);
        Mockito.doReturn((short) 0).when(row).getFirstCellNum();
        Mockito.doReturn((short) -1).when(row).getLastCellNum();
        Mockito.doReturn(7).when(row).getRowNum();

        Method method = ExcelUMStructHandler.class.getDeclaredMethod("handleObjField",
                StructWorker.class, Class.class, Row.class, Map.class, FormulaEvaluator.class, Consumer.class);
        method.setAccessible(true);
        method.invoke(new ExcelUMStructHandler(), null, SheetBean.class, row,
                Collections.emptyMap(), null, (Consumer<SheetBean>) b -> Assertions.fail("no bean expected"));
    }

    private <T> StructWorker<T> newWorker(Path dir, Class<T> clzOfStruct) {
        StructWorker<T> worker = new StructWorker<>(dir + "/", clzOfStruct);
        worker.checkStructFactory();
        return worker;
    }

    private File writeWorkbook(Path dir, String sheetName, String fileName, Consumer<Sheet> filler) throws Exception {
        File file = dir.resolve(fileName).toFile();
        try (Workbook wb = new XSSFWorkbook()) {
            filler.accept(wb.createSheet(sheetName));
            try (OutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        return file;
    }

    @StructSheet(fileName = "sheet.xlsx")
    public static class SheetBean {
        public int key;
        public String name;
        public double ratio;
        public long big;
    }

    @StructSheet(fileName = "start.xlsx", startOrder = -1)
    public static class NegativeStartBean {
        public String key;
        public String name;
    }

    @StructSheet(fileName = "err.xlsx")
    public static class RequiredKeyBean {
        @StructField(required = true)
        public int key;
        public String name;
    }

}