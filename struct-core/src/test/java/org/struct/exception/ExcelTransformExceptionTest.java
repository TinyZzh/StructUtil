package org.struct.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExcelTransformExceptionTest {

    @Test()
    public void raiseException() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new StructTransformException();
        });
    }

    @Test()
    public void raiseException1() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new StructTransformException("msg");
        });
    }

    @Test()
    public void raiseException2() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new StructTransformException("msg", new StructTransformException());
        });
    }

    @Test()
    public void raiseException3() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new StructTransformException(new StructTransformException());
        });
    }

    @Test()
    public void raiseException4() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new StructTransformException("msg", new StructTransformException(), false, true);
        });
    }
}