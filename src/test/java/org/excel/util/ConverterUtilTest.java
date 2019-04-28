package org.excel.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

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
        Assert.assertEquals(1.0F, ConverterUtil.covert("1", float.class));
        Assert.assertEquals(1.0F, ConverterUtil.covert("1", Float.class));
        Assert.assertEquals(1.0D, ConverterUtil.covert("1", double.class));
        Assert.assertEquals(1.0D, ConverterUtil.covert("1", Double.class));
        Object obj = new Object();
        Assert.assertEquals(obj, ConverterUtil.covert(obj, ConverterUtil.class));
    }

    @Test
    public void testConvertNumberToTargetClass() {
        Assert.assertEquals(Byte.valueOf("1"), ConverterUtil.covert(1, byte.class));
        Assert.assertEquals(Byte.valueOf("1"), ConverterUtil.covert(1, Byte.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterUtil.covert(1, short.class));
        Assert.assertEquals(Short.valueOf("1"), ConverterUtil.covert(1, Short.class));
        Assert.assertEquals(1, ConverterUtil.covert(1, int.class));
        Assert.assertEquals(1, ConverterUtil.covert(1, Integer.class));
        Assert.assertEquals(1L, ConverterUtil.covert(1, long.class));
        Assert.assertEquals(1L, ConverterUtil.covert(1, Long.class));
        Assert.assertEquals(true, ConverterUtil.covert(1, boolean.class));
        Assert.assertEquals(false, ConverterUtil.covert(0, Boolean.class));
        Assert.assertEquals(BigInteger.valueOf(1L), ConverterUtil.covert(1, BigInteger.class));
        Assert.assertEquals("1", ConverterUtil.covert(1, String.class));
        Assert.assertEquals(1, ConverterUtil.covert(1, Object.class));
        Assert.assertEquals(1.0F, ConverterUtil.covert(1, float.class));
        Assert.assertEquals(1.0F, ConverterUtil.covert(1, Float.class));
        Assert.assertEquals(1.0D, ConverterUtil.covert(1, double.class));
        Assert.assertEquals(1.0D, ConverterUtil.covert(1, Double.class));
        Assert.assertEquals(BigInteger.valueOf(1), ConverterUtil.covert(BigDecimal.valueOf(1), BigInteger.class));
        Assert.assertEquals(1, ConverterUtil.covert(BigInteger.valueOf(1), Integer.class));
        Assert.assertEquals('a', ConverterUtil.covert('a', char.class));
        Assert.assertEquals('a', ConverterUtil.covert('a', Character.class));
    }

    @Test
    public void testOutOfRangeConvertNumberToTargetClass() {

        for (Object[] objs : Arrays.asList(
                new Object[] {Byte.MAX_VALUE + 1, Byte.class},
                new Object[] {Byte.MIN_VALUE - 1, Byte.class},
                new Object[] {Short.MAX_VALUE + 1, Short.class},
                new Object[] {Short.MIN_VALUE - 1, Short.class},
                new Object[] {Integer.MAX_VALUE + 1L, Integer.class},
                new Object[] {Integer.MIN_VALUE - 1L, Integer.class},
                new Object[] {BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), Long.class},
                new Object[] {BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), Long.class}
        )) {
            try {
                Assert.assertEquals(1.0D, ConverterUtil.covert(objs[0], (Class) objs[1]));
            } catch (Exception e){
                continue;
            }
            Assert.fail();
        }
    }
}