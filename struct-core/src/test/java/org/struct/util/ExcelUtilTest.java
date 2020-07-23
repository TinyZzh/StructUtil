package org.struct.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ExcelUtilTest {

    @Test
    public void testNewListOnlyException() throws Exception {
        //
        try {
            WorkerUtil.newListOnly(Object.class);
        } catch (Exception e) {
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testNewInterface() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(List.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewAbstractClass() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(AbstractList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewArrayList() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(ArrayList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(HashSet.class).getClass()));
    }

    @Test
    public void testGetExcelCellValue() throws Exception {
        Cell cell = Mockito.mock(Cell.class);

        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Assertions.assertNull(WorkerUtil.getExcelCellValue(CellType._NONE, cell, formula));
        Assertions.assertEquals("", WorkerUtil.getExcelCellValue(CellType.BLANK, cell, formula));
        //   numeric
        Mockito.doReturn(1.00D).when(cell).getNumericCellValue();
        Assertions.assertEquals(1, WorkerUtil.getExcelCellValue(CellType.NUMERIC, cell, formula));
        CellValue cellValue = new CellValue(1.00D);
        Assertions.assertEquals(1, WorkerUtil.getExcelCellValue(CellType.NUMERIC, cellValue, formula));
        Assertions.assertEquals(0, WorkerUtil.getExcelCellValue(CellType.NUMERIC, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Assertions.assertEquals("1", WorkerUtil.getExcelCellValue(CellType.STRING, cell, formula));
        cellValue = new CellValue("1");
        Assertions.assertEquals("1", WorkerUtil.getExcelCellValue(CellType.STRING, cellValue, formula));
        Assertions.assertEquals("", WorkerUtil.getExcelCellValue(CellType.STRING, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.reset(formula);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Mockito.doReturn(new CellValue("1")).when(formula).evaluate(Mockito.any(Cell.class));
        Assertions.assertEquals("1", WorkerUtil.getExcelCellValue(CellType.FORMULA, cell, formula));
        Assertions.assertNull(WorkerUtil.getExcelCellValue(CellType.FORMULA, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn(true).when(cell).getBooleanCellValue();
        Assertions.assertEquals(true, WorkerUtil.getExcelCellValue(CellType.BOOLEAN, cell, formula));
        cellValue = CellValue.TRUE;
        Assertions.assertEquals(true, WorkerUtil.getExcelCellValue(CellType.BOOLEAN, cellValue, formula));
        Assertions.assertEquals(false, WorkerUtil.getExcelCellValue(CellType.BOOLEAN, new Object(), formula));
        //
        Mockito.reset(cell);
        try {
            Assertions.assertEquals(true, WorkerUtil.getExcelCellValue(CellType.ERROR, cell, formula));
        } catch (Exception e) {
            return;
        }
        Assertions.fail();
    }
}