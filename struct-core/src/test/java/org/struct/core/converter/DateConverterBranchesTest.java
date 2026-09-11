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

import java.util.Date;

/**
 * {@link DateConverter} accepts several shapes of input - a bare number is treated as SECONDS
 * while a large one is already in milliseconds.
 *
 * @author TinyZ.
 */
class DateConverterBranchesTest {

    private static final ConvertContext CTX = null;

    @Test
    void testPassthrough() {
        DateConverter c = new DateConverter();
        //  null stays null
        Assertions.assertNull(c.convert(CTX, null, Date.class));
        //  a target type that is not Date at all -> the raw value is returned
        Assertions.assertEquals("x", c.convert(CTX, "x", String.class));
        //  already a Date -> returned as is
        Date now = new Date();
        Assertions.assertSame(now, c.convert(CTX, now, Date.class));
    }

    @Test
    void testNumericSource() {
        DateConverter c = new DateConverter();
        //  an Integer is seconds
        Assertions.assertEquals(new Date(1_000L), c.convert(CTX, 1, Date.class));
        //  a small Long is seconds too
        Assertions.assertEquals(new Date(2_000L), c.convert(CTX, 2L, Date.class));
        //  a Long beyond the Integer range is already milliseconds
        Assertions.assertEquals(new Date(5_000_000_000L), c.convert(CTX, 5_000_000_000L, Date.class));
    }

    @Test
    void testStringSource() {
        DateConverter c = new DateConverter();
        //  a small number is seconds
        Assertions.assertEquals(new Date(1_000L), c.convert(CTX, "1", Date.class));
        //  a big number is milliseconds
        Assertions.assertEquals(new Date(5_000_000_000L), c.convert(CTX, "5000000000", Date.class));
    }

    @Test
    void testUnresolvableSource() {
        DateConverter c = new DateConverter();
        //  nothing matches -> the raw value is handed back and a warning is logged
        Assertions.assertEquals("not-a-date", c.convert(CTX, "not-a-date", Date.class));
    }

    @Test
    void testFormatPattern() {
        DateConverter c = new DateConverter();
        Assertions.assertNotNull(c.getFormatPattern());
        c.setFormatPattern("yyyy-MM-dd");
        Assertions.assertEquals("yyyy-MM-dd", c.getFormatPattern());
    }
}
