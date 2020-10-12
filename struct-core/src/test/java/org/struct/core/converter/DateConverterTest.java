/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.struct.util.Strings;
import sun.util.calendar.BaseCalendar;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author TinyZ.
 * @date 2020-09-15.
 */
class DateConverterTest {

    private DateConverter converter = new DateConverter();

    private Method method;

    @BeforeEach
    private void beforeAll() throws Exception {
        converter.setFormatPattern(Strings.DATE_TIME_FORMAT_PATTERN);
        Assertions.assertNotNull(converter.getFormatPattern());
        Assertions.assertEquals(Strings.DATE_TIME_FORMAT_PATTERN, converter.getFormatPattern());
        method = Date.class.getDeclaredMethod("normalize");
        method.setAccessible(true);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"2020-09-15 01:02:03", "1600102923", "1600102923000"})
    public void testStr(String str) {
        Object c = converter.convert(str, Date.class);
        c = this.getDate(c);
        Assertions.assertTrue(c instanceof BaseCalendar.Date);
        Assertions.assertEquals(2020, ((BaseCalendar.Date) c).getYear());
        Assertions.assertEquals(9, ((BaseCalendar.Date) c).getMonth());
        Assertions.assertEquals(15, ((BaseCalendar.Date) c).getDayOfMonth());
        Assertions.assertEquals(1, ((BaseCalendar.Date) c).getHours());
        Assertions.assertEquals(2, ((BaseCalendar.Date) c).getMinutes());
        Assertions.assertEquals(3, ((BaseCalendar.Date) c).getSeconds());
    }

    @Test
    public void testTimestamp() {
        Object c = converter.convert(1600102923, Date.class);
        c = this.getDate(c);
        Assertions.assertTrue(c instanceof BaseCalendar.Date);
        Assertions.assertEquals(2020, ((BaseCalendar.Date) c).getYear());
        Assertions.assertEquals(9, ((BaseCalendar.Date) c).getMonth());
        Assertions.assertEquals(15, ((BaseCalendar.Date) c).getDayOfMonth());
        Assertions.assertEquals(1, ((BaseCalendar.Date) c).getHours());
        Assertions.assertEquals(2, ((BaseCalendar.Date) c).getMinutes());
        Assertions.assertEquals(3, ((BaseCalendar.Date) c).getSeconds());
    }

    @Test
    public void testMills() {
        Object c = converter.convert(1600102923000L, Date.class);
        c = this.getDate(c);
        Assertions.assertTrue(c instanceof BaseCalendar.Date);
        Assertions.assertEquals(2020, ((BaseCalendar.Date) c).getYear());
        Assertions.assertEquals(9, ((BaseCalendar.Date) c).getMonth());
        Assertions.assertEquals(15, ((BaseCalendar.Date) c).getDayOfMonth());
        Assertions.assertEquals(1, ((BaseCalendar.Date) c).getHours());
        Assertions.assertEquals(2, ((BaseCalendar.Date) c).getMinutes());
        Assertions.assertEquals(3, ((BaseCalendar.Date) c).getSeconds());
    }

    @Test
    public void testOriginIsLocalDateTime() {
        Object c = converter.convert(new Date(2020, 9, 15, 1, 2, 3), Date.class);
        c = this.getDate(c);
        Assertions.assertTrue(c instanceof BaseCalendar.Date);
        Assertions.assertEquals(2020, ((BaseCalendar.Date) c).getYear());
        Assertions.assertEquals(9, ((BaseCalendar.Date) c).getMonth());
        Assertions.assertEquals(15, ((BaseCalendar.Date) c).getDayOfMonth());
        Assertions.assertEquals(1, ((BaseCalendar.Date) c).getHours());
        Assertions.assertEquals(2, ((BaseCalendar.Date) c).getMinutes());
        Assertions.assertEquals(3, ((BaseCalendar.Date) c).getSeconds());
    }

    @Test
    public void testTargetNotLocalDate() {
        Object c = converter.convert(1, int.class);
        Assertions.assertEquals(1, c);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"2020/09/15", "2020/09/15 01:02:03"})
    public void testFail(String str) {
        Object c = converter.convert(str, Date.class);
        Assertions.assertFalse(c instanceof Date);
        Assertions.assertEquals(str, c);
    }

    private BaseCalendar.Date getDate(Object c) {
        try {
            return (BaseCalendar.Date) method.invoke(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}