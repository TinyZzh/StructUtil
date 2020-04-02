package org.struct.exception;

import org.junit.Test;

public class EndOfExcelSheetExceptionTest {

    @Test(expected = EndOfExcelSheetException.class)
    public void raiseException() {
        throw new EndOfExcelSheetException();
    }

    @Test(expected = EndOfExcelSheetException.class)
    public void raiseException1() {
        throw new EndOfExcelSheetException("msg");
    }

    @Test(expected = EndOfExcelSheetException.class)
    public void raiseException2() {
        throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException());
    }

    @Test(expected = EndOfExcelSheetException.class)
    public void raiseException3() {
        throw new EndOfExcelSheetException(new EndOfExcelSheetException());
    }

    @Test(expected = EndOfExcelSheetException.class)
    public void raiseException4() {
        throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException(), false, true);
    }
}