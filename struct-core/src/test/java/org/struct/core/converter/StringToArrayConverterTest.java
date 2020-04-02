package org.struct.core.converter;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class StringToArrayConverterTest {

    @Test
    public void convertParamNotString() {
        StringToArrayConverter converter = new StringToArrayConverter();
        Object result = converter.convert(11, List.class);
        Assert.assertNull(result);
    }

    @Test
    public void convertNotArray() {
        StringToArrayConverter converter = new StringToArrayConverter();
        Object result = converter.convert("|11|22|333|4444", List.class);
        Assert.assertNull(result);
    }

    @Test
    public void convert2StringArray() {
        StringToArrayConverter converter = new StringToArrayConverter();
        String[] data = (String[]) converter.convert("|11|22|333|4444", String[].class);
        Assert.assertEquals("11", data[0]);
        Assert.assertEquals("22", data[1]);
        Assert.assertEquals("333", data[2]);
        Assert.assertEquals("4444", data[3]);
    }

    @Test
    public void convert2longArray() {
        StringToArrayConverter converter = new StringToArrayConverter();
        long[] data = (long[]) converter.convert("|11|22|333|4444", long[].class);
        Assert.assertEquals(11L, data[0]);
        Assert.assertEquals(22L, data[1]);
        Assert.assertEquals(333L, data[2]);
        Assert.assertEquals(4444L, data[3]);
    }

    @Test
    public void convert2IntegerArray() {
        StringToArrayConverter converter = new StringToArrayConverter();
        Integer[] data = (Integer[]) converter.convert("|11|22|333|4444", Integer[].class);
        Assert.assertEquals((Integer) 11, data[0]);
        Assert.assertEquals((Integer) 22, data[1]);
        Assert.assertEquals((Integer) 333, data[2]);
        Assert.assertEquals((Integer) 4444, data[3]);
    }

    @Test
    public void convert2IntegerArrayWithBlank() {
        StringToArrayConverter converter = new StringToArrayConverter("\\|", false);
        Integer[] data = (Integer[]) converter.convert("11 |22|333 |4444", Integer[].class);
        Assert.assertEquals((Integer) 11, data[0]);
        Assert.assertEquals((Integer) 22, data[1]);
        Assert.assertEquals((Integer) 333, data[2]);
        Assert.assertEquals((Integer) 4444, data[3]);
    }

}