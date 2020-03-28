package org.struct.spi;

import org.junit.Assert;
import org.junit.Test;
import org.struct.core.handler.StructHandler;

import java.util.List;

import static org.junit.Assert.*;

public class ServiceLoaderTest {

    @Test
    public void test() {
        List<StructHandler> handlers = ServiceLoader.loadAll(StructHandler.class);
        Assert.assertFalse(handlers.isEmpty());
    }
}