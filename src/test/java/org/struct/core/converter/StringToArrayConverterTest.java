package org.struct.core.converter;

import org.junit.Assert;
import org.junit.Test;

public class StringToArrayConverterTest {

    @Test
    public void test() {
        StringToArrayConverter converter = new StringToArrayConverter();
        long[] data = (long[]) converter.convert("|11|22|333|4444", long[].class);
        Assert.assertEquals(11L, data[0]);
        Assert.assertEquals(22L, data[1]);
        Assert.assertEquals(333L, data[2]);
        Assert.assertEquals(4444L, data[3]);
    }

}