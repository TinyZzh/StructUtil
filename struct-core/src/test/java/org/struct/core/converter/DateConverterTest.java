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
import org.struct.util.Strings;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
class DateConverterTest {

    @Test
    public void test() {
        DateConverter converter = new DateConverter();
        converter.setFormatPattern(Strings.DATE_FORMAT_PATTERN);
        Assertions.assertEquals(Strings.DATE_FORMAT_PATTERN, converter.getFormatPattern());
        converter.setFormatPattern(Strings.DATE_TIME_FORMAT_PATTERN);

        Assertions.assertNull(converter.convert(null, null, Date.class));

        long mills = System.currentTimeMillis();
        Date date = new Date(mills);
        Assertions.assertEquals(date, converter.convert(null, date, Date.class));

        long timestamp = System.currentTimeMillis() / 1000L;
        Assertions.assertEquals(new Date(timestamp * 1000L), converter.convert(null, (int) timestamp, Date.class));
        Assertions.assertEquals(new Date(timestamp * 1000L), converter.convert(null, timestamp, Date.class));

        SimpleDateFormat format = new SimpleDateFormat(Strings.DATE_TIME_FORMAT_PATTERN);
        String dateStr = format.format(date);
        Assertions.assertEquals(date.toString(), converter.convert(null, dateStr, Date.class).toString());

        Assertions.assertEquals(new Date(mills), converter.convert(null, String.valueOf(mills), Date.class));

        Assertions.assertEquals("xx", converter.convert(null, "xx", Date.class));
    }

}