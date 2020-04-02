package org.struct.spi;

import org.junit.Test;

public class ServiceNotFoundExceptionTest {
    @Test(expected = ServiceNotFoundException.class)
    public void raiseException() {
        throw new ServiceNotFoundException();
    }

    @Test(expected = ServiceNotFoundException.class)
    public void raiseException1() {
        throw new ServiceNotFoundException("msg");
    }

    @Test(expected = ServiceNotFoundException.class)
    public void raiseException2() {
        throw new ServiceNotFoundException("msg", new ServiceNotFoundException());
    }

    @Test(expected = ServiceNotFoundException.class)
    public void raiseException3() {
        throw new ServiceNotFoundException(new ServiceNotFoundException());
    }

    @Test(expected = ServiceNotFoundException.class)
    public void raiseException4() {
        throw new ServiceNotFoundException("msg", new ServiceNotFoundException(), false, true);
    }
}