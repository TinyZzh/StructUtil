/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author TinyZ.
 * @date 2020-09-15.
 */
class LocalDateTimeConverterTest {

    private LocalDateTimeConverter converter = new LocalDateTimeConverter();

    @BeforeEach
    public void beforeAll() {
        converter.setZoneId(ZoneId.systemDefault());
        converter.setFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Assertions.assertNotNull(converter.getZoneId());
        Assertions.assertNotNull(converter.getFormatter());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"2020-09-15 01:02:03", "1600102923", "1600102923000"})
    public void testStr(String str) {
        Object c = converter.convert(str, LocalDateTime.class);
        Assertions.assertTrue(c instanceof LocalDateTime);
        Assertions.assertEquals(2020, ((LocalDateTime) c).getYear());
        Assertions.assertEquals(9, ((LocalDateTime) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDateTime) c).getDayOfMonth());
        Assertions.assertEquals(1, ((LocalDateTime) c).getHour());
        Assertions.assertEquals(2, ((LocalDateTime) c).getMinute());
        Assertions.assertEquals(3, ((LocalDateTime) c).getSecond());
    }

    @Test
    public void testTimestamp() {
        Object c = converter.convert(1600102923, LocalDateTime.class);
        Assertions.assertTrue(c instanceof LocalDateTime);
        Assertions.assertEquals(2020, ((LocalDateTime) c).getYear());
        Assertions.assertEquals(9, ((LocalDateTime) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDateTime) c).getDayOfMonth());
        Assertions.assertEquals(1, ((LocalDateTime) c).getHour());
        Assertions.assertEquals(2, ((LocalDateTime) c).getMinute());
        Assertions.assertEquals(3, ((LocalDateTime) c).getSecond());
    }

    @Test
    public void testMills() {
        Object c = converter.convert(1600102923000L, LocalDateTime.class);
        Assertions.assertTrue(c instanceof LocalDateTime);
        Assertions.assertEquals(2020, ((LocalDateTime) c).getYear());
        Assertions.assertEquals(9, ((LocalDateTime) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDateTime) c).getDayOfMonth());
        Assertions.assertEquals(1, ((LocalDateTime) c).getHour());
        Assertions.assertEquals(2, ((LocalDateTime) c).getMinute());
        Assertions.assertEquals(3, ((LocalDateTime) c).getSecond());
    }

    @Test
    public void testOriginIsLocalDateTime() {
        Object c = converter.convert(LocalDateTime.of(2020, 9, 15, 1, 2, 3), LocalDateTime.class);
        Assertions.assertTrue(c instanceof LocalDateTime);
        Assertions.assertEquals(2020, ((LocalDateTime) c).getYear());
        Assertions.assertEquals(9, ((LocalDateTime) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDateTime) c).getDayOfMonth());
        Assertions.assertEquals(1, ((LocalDateTime) c).getHour());
        Assertions.assertEquals(2, ((LocalDateTime) c).getMinute());
        Assertions.assertEquals(3, ((LocalDateTime) c).getSecond());
    }

    @Test
    public void testTargetNotLocalDate() {
        Object c = converter.convert(1, int.class);
        Assertions.assertEquals(1, c);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"2020/09/15", "2020/09/15 01:02:03"})
    public void testFail(String str) {
        Object c = converter.convert(str, LocalDateTime.class);
        Assertions.assertFalse(c instanceof LocalDateTime);
        Assertions.assertEquals(str, c);
    }
}