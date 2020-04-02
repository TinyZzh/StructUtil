package org.struct.exception;

import org.junit.Test;

public class NoSuchFieldReferenceExceptionTest {

    @Test(expected = NoSuchFieldReferenceException.class)
    public void raiseException() {
        throw new NoSuchFieldReferenceException();
    }

    @Test(expected = NoSuchFieldReferenceException.class)
    public void raiseException1() {
        throw new NoSuchFieldReferenceException("msg");
    }

    @Test(expected = NoSuchFieldReferenceException.class)
    public void raiseException2() {
        throw new NoSuchFieldReferenceException("msg", new NoSuchFieldReferenceException());
    }

    @Test(expected = NoSuchFieldReferenceException.class)
    public void raiseException3() {
        throw new NoSuchFieldReferenceException(new NoSuchFieldReferenceException());
    }

    @Test(expected = NoSuchFieldReferenceException.class)
    public void raiseException4() {
        throw new NoSuchFieldReferenceException("msg", new NoSuchFieldReferenceException(), false, true);
    }
}