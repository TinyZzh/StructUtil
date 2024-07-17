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
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

}