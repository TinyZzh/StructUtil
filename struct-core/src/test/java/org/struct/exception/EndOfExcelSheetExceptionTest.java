package org.struct.exception;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EndOfExcelSheetExceptionTest {


    @Test()
    public void raiseException() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException();
        });
    }

    @Test()
    public void raiseException1() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg");
        });
    }

    @Test()
    public void raiseException2() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException());
        });
    }

    @Test()
    public void raiseException3() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException(new EndOfExcelSheetException());
        });
    }

    @Test()
    public void raiseException4() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException(), false, true);
        });
    }
}