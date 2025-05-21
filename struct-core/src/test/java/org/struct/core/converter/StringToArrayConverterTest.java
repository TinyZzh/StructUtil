/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.struct.core.converter;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringToArrayConverterTest {

    @Test
    public void test() {
        ArrayConverter converter = new ArrayConverter("xx", false, false);
        Assertions.assertEquals("xx", converter.getSeparator());
        Assertions.assertFalse(converter.isStrTrim());
        Assertions.assertFalse(converter.isIgnoreBlank());
        converter.setSeparator("yy");
        converter.setStrTrim(true);
        converter.setIgnoreBlank(true);
        Assertions.assertEquals("yy", converter.getSeparator());
        Assertions.assertTrue(converter.isStrTrim());
        Assertions.assertTrue(converter.isIgnoreBlank());
    }

    @Test
    public void convertParamNotString() {
        ArrayConverter converter = new ArrayConverter();
        Object result = converter.convert(null, 11, List.class);
        Assertions.assertNull(result);
    }

    @Test
    public void convertNotArray() {
        ArrayConverter converter = new ArrayConverter();
        Object result = converter.convert(null, "|11|22|333|4444", List.class);
        Assertions.assertNull(result);
    }

    @Test
    public void convert2StringArray() {
        ArrayConverter converter = new ArrayConverter();
        converter.setIgnoreBlank(true);
        String[] data = (String[]) converter.convert(null, "|11|22|333|4444", String[].class);
        Assertions.assertEquals("11", data[0]);
        Assertions.assertEquals("22", data[1]);
        Assertions.assertEquals("333", data[2]);
        Assertions.assertEquals("4444", data[3]);
    }

    @Test
    public void convert2longArray() {
        ArrayConverter converter = new ArrayConverter();
        converter.setIgnoreBlank(true);
        long[] data = (long[]) converter.convert(null, "|11|22|333|4444", long[].class);
        Assertions.assertEquals(11L, data[0]);
        Assertions.assertEquals(22L, data[1]);
        Assertions.assertEquals(333L, data[2]);
        Assertions.assertEquals(4444L, data[3]);
    }

    @Test
    public void convert2IntegerArray() {
        ArrayConverter converter = new ArrayConverter();
        converter.setIgnoreBlank(true);
        Integer[] data = (Integer[]) converter.convert(null, "|11|22|333|4444", Integer[].class);
        Assertions.assertEquals((Integer) 11, data[0]);
        Assertions.assertEquals((Integer) 22, data[1]);
        Assertions.assertEquals((Integer) 333, data[2]);
        Assertions.assertEquals((Integer) 4444, data[3]);
    }

    @Test
    public void convert2IntegerArrayWithBlank() {
        ArrayConverter converter = new ArrayConverter("\\|", false);
        Integer[] data = (Integer[]) converter.convert(null, "11|22|333|4444", Integer[].class);
        Assertions.assertEquals((Integer) 11, data[0]);
        Assertions.assertEquals((Integer) 22, data[1]);
        Assertions.assertEquals((Integer) 333, data[2]);
        Assertions.assertEquals((Integer) 4444, data[3]);
    }

}