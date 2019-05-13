package org.excel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.Assert;
import org.junit.Test;
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
            ExcelUtil.newListOnly(Object.class);
        } catch (Exception e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testNewInterface() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(List.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewAbstractClass() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(AbstractList.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewArrayList() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(ArrayList.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assert.assertTrue(HashSet.class.isAssignableFrom(ExcelUtil.newListOnly(HashSet.class).getClass()));
    }

    @Test
    public void testGetExcelCellValue() throws Exception {
        Cell cell = Mockito.mock(Cell.class);

        FormulaEvaluator formula = Mockito.mock(FormulaEvaluator.class);
        Assert.assertNull(ExcelUtil.getExcelCellValue(CellType._NONE, cell, formula));
        Assert.assertEquals("", ExcelUtil.getExcelCellValue(CellType.BLANK, cell, formula));
       //   numeric
        Mockito.doReturn(1.00D).when(cell).getNumericCellValue();
        Assert.assertEquals(1, ExcelUtil.getExcelCellValue(CellType.NUMERIC, cell, formula));
        CellValue cellValue = new CellValue(1.00D);
        Assert.assertEquals(1, ExcelUtil.getExcelCellValue(CellType.NUMERIC, cellValue, formula));
        Assert.assertEquals(0, ExcelUtil.getExcelCellValue(CellType.NUMERIC, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Assert.assertEquals("1", ExcelUtil.getExcelCellValue(CellType.STRING, cell, formula));
        cellValue = new CellValue("1");
        Assert.assertEquals("1", ExcelUtil.getExcelCellValue(CellType.STRING, cellValue, formula));
        Assert.assertEquals("", ExcelUtil.getExcelCellValue(CellType.STRING, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.reset(formula);
        Mockito.doReturn("1").when(cell).getStringCellValue();
        Mockito.doReturn(new CellValue("1")).when(formula).evaluate(Mockito.any(Cell.class));
        Assert.assertEquals("1", ExcelUtil.getExcelCellValue(CellType.FORMULA, cell, formula));
        Assert.assertNull(ExcelUtil.getExcelCellValue(CellType.FORMULA, new Object(), formula));
        //
        Mockito.reset(cell);
        Mockito.doReturn(true).when(cell).getBooleanCellValue();
        Assert.assertEquals(true, ExcelUtil.getExcelCellValue(CellType.BOOLEAN, cell, formula));
        cellValue = CellValue.TRUE;
        Assert.assertEquals(true, ExcelUtil.getExcelCellValue(CellType.BOOLEAN, cellValue, formula));
        Assert.assertEquals(false, ExcelUtil.getExcelCellValue(CellType.BOOLEAN, new Object(), formula));
        //
        Mockito.reset(cell);
        try {
            Assert.assertEquals(true, ExcelUtil.getExcelCellValue(CellType.ERROR, cell, formula));
        } catch (Exception e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testX() {
        ExcelUtil.test();
    }
}