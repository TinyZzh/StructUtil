package org.struct.spi;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.exception.ServiceNotFoundException;
import org.struct.exception.ServiceNotFoundException;

public class ServiceNotFoundExceptionTest {
    @Test()
    public void raiseException() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            throw new ServiceNotFoundException();
        });
    }

    @Test()
    public void raiseException1() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            throw new ServiceNotFoundException("msg");
        });
    }

    @Test()
    public void raiseException2() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            throw new ServiceNotFoundException("msg", new ServiceNotFoundException());
        });
    }

    @Test()
    public void raiseException3() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            throw new ServiceNotFoundException(new ServiceNotFoundException());
        });
    }

    @Test()
    public void raiseException4() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            throw new ServiceNotFoundException("msg", new ServiceNotFoundException(), false, true);
        });
    }
}