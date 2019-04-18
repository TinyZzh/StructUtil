package org.excel.core;

import org.junit.Assert;
import org.junit.Test;

public class ConverterRegistryTest {

    @Test
    public void register1() {
        //  lambda
        ConverterRegistry.register(Integer.class, o -> 1);
        Assert.assertEquals(Integer.valueOf(1), ConverterRegistry.lookup(Integer.class).apply(1));
        //  anonymous class
        ConverterRegistry.register(Long.class, new Converter<Long>() {
            @Override
            public Long apply(Object o) {
                return 1L;
            }
        });
        Assert.assertEquals(Long.valueOf(1), ConverterRegistry.lookup(Long.class).apply(1));

    }

    @Test
    public void registerWithConverterClass() {

    }

    @Test
    public void lookup() {
    }
}