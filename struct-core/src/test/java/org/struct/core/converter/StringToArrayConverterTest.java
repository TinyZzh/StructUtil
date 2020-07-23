package org.struct.core.converter;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringToArrayConverterTest {

    @Test
    public void convertParamNotString() {
        ArrayConverter converter = new ArrayConverter();
        Object result = converter.convert(11, List.class);
        Assertions.assertNull(result);
    }

    @Test
    public void convertNotArray() {
        ArrayConverter converter = new ArrayConverter();
        Object result = converter.convert("|11|22|333|4444", List.class);
        Assertions.assertNull(result);
    }

    @Test
    public void convert2StringArray() {
        ArrayConverter converter = new ArrayConverter();
        String[] data = (String[]) converter.convert("|11|22|333|4444", String[].class);
        Assertions.assertEquals("11", data[0]);
        Assertions.assertEquals("22", data[1]);
        Assertions.assertEquals("333", data[2]);
        Assertions.assertEquals("4444", data[3]);
    }

    @Test
    public void convert2longArray() {
        ArrayConverter converter = new ArrayConverter();
        long[] data = (long[]) converter.convert("|11|22|333|4444", long[].class);
        Assertions.assertEquals(11L, data[0]);
        Assertions.assertEquals(22L, data[1]);
        Assertions.assertEquals(333L, data[2]);
        Assertions.assertEquals(4444L, data[3]);
    }

    @Test
    public void convert2IntegerArray() {
        ArrayConverter converter = new ArrayConverter();
        Integer[] data = (Integer[]) converter.convert("|11|22|333|4444", Integer[].class);
        Assertions.assertEquals((Integer) 11, data[0]);
        Assertions.assertEquals((Integer) 22, data[1]);
        Assertions.assertEquals((Integer) 333, data[2]);
        Assertions.assertEquals((Integer) 4444, data[3]);
    }

    @Test
    public void convert2IntegerArrayWithBlank() {
        ArrayConverter converter = new ArrayConverter("\\|", false);
        Integer[] data = (Integer[]) converter.convert("11 |22|333 |4444", Integer[].class);
        Assertions.assertEquals((Integer) 11, data[0]);
        Assertions.assertEquals((Integer) 22, data[1]);
        Assertions.assertEquals((Integer) 333, data[2]);
        Assertions.assertEquals((Integer) 4444, data[3]);
    }

}