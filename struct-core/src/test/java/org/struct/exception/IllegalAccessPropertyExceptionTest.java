package org.struct.exception;

import org.junit.Test;

public class IllegalAccessPropertyExceptionTest {

    @Test(expected = IllegalAccessPropertyException.class)
    public void raiseException() {
        throw new IllegalAccessPropertyException();
    }

    @Test(expected = IllegalAccessPropertyException.class)
    public void raiseException1() {
        throw new IllegalAccessPropertyException("msg");
    }

    @Test(expected = IllegalAccessPropertyException.class)
    public void raiseException2() {
        throw new IllegalAccessPropertyException("msg", new IllegalAccessPropertyException());
    }

    @Test(expected = IllegalAccessPropertyException.class)
    public void raiseException3() {
        throw new IllegalAccessPropertyException(new IllegalAccessPropertyException());
    }

    @Test(expected = IllegalAccessPropertyException.class)
    public void raiseException4() {
        throw new IllegalAccessPropertyException("msg", new IllegalAccessPropertyException(), false, true);
    }
}