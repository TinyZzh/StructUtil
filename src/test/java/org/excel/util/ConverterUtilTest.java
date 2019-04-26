package org.excel.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class ConverterUtilTest {

    @Test
    public void covert() {
        Assert.assertEquals(Byte.valueOf("1"), ConverterUtil.covert("1", byte.class));
        Assert.assertEquals(Byte.valueOf("1"), ConverterUtil.covert("1", Byte.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterUtil.covert("1", short.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterUtil.covert("1", Short.class));
        Assert.assertEquals(1, ConverterUtil.covert("1", int.class));
        Assert.assertEquals(1, ConverterUtil.covert("1", Integer.class));
        Assert.assertEquals(1L, ConverterUtil.covert("1", long.class));
        Assert.assertEquals(1L, ConverterUtil.covert("1", Long.class));
        Assert.assertEquals(true, ConverterUtil.covert("true", boolean.class));
        Assert.assertEquals(false, ConverterUtil.covert("false", Boolean.class));
        Assert.assertEquals(BigInteger.valueOf(1L), ConverterUtil.covert("1", BigInteger.class));
        Assert.assertEquals("1", ConverterUtil.covert("1", String.class));
        Assert.assertEquals("1", ConverterUtil.covert("1", Object.class));

    }
}