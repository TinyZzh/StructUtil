package org.struct.exception;

import org.junit.Test;

public class ExcelTransformExceptionTest {

    @Test(expected = StructTransformException.class)
    public void raiseException() {
        throw new StructTransformException();
    }

    @Test(expected = StructTransformException.class)
    public void raiseException1() {
        throw new StructTransformException("msg");
    }

    @Test(expected = StructTransformException.class)
    public void raiseException2() {
        throw new StructTransformException("msg", new StructTransformException());
    }

    @Test(expected = StructTransformException.class)
    public void raiseException3() {
        throw new StructTransformException(new StructTransformException());
    }

    @Test(expected = StructTransformException.class)
    public void raiseException4() {
        throw new StructTransformException("msg", new StructTransformException(), false, true);
    }
}