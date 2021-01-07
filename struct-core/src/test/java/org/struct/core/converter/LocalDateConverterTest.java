/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author TinyZ.
 * @date 2020-09-15.
 */
class LocalDateConverterTest {

    private LocalDateConverter converter = new LocalDateConverter();

    @BeforeEach
    private void beforeAll() {
        converter.setZoneId(ZoneId.systemDefault());
        converter.setFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Assertions.assertNotNull(converter.getZoneId());
        Assertions.assertNotNull(converter.getFormatter());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"1600128000", "1600128000000", "2020-09-15"})
    public void testStr(String str) {
        Object c = converter.convert(str, LocalDate.class);
        Assertions.assertTrue(c instanceof LocalDate);
        Assertions.assertEquals(2020, ((LocalDate) c).getYear());
        Assertions.assertEquals(9, ((LocalDate) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDate) c).getDayOfMonth());
    }

    @Test
    public void testTimestamp() {
        Object c = converter.convert(1600128000, LocalDate.class);
        Assertions.assertTrue(c instanceof LocalDate);
        Assertions.assertEquals(2020, ((LocalDate) c).getYear());
        Assertions.assertEquals(9, ((LocalDate) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDate) c).getDayOfMonth());
    }

    @Test
    public void testMills() {
        Object c = converter.convert(1600128000000L, LocalDate.class);
        Assertions.assertTrue(c instanceof LocalDate);
        Assertions.assertEquals(2020, ((LocalDate) c).getYear());
        Assertions.assertEquals(9, ((LocalDate) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDate) c).getDayOfMonth());
    }

    @Test
    public void testOriginIsLocalDate() {
        Object c = converter.convert(LocalDate.of(2020, 9, 15), LocalDate.class);
        Assertions.assertTrue(c instanceof LocalDate);
        Assertions.assertEquals(2020, ((LocalDate) c).getYear());
        Assertions.assertEquals(9, ((LocalDate) c).getMonthValue());
        Assertions.assertEquals(15, ((LocalDate) c).getDayOfMonth());
    }

    @Test
    public void testTargetNotLocalDate() {
        Object c = converter.convert(1, int.class);
        Assertions.assertEquals(1, c);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"2020/09/15", "2020/09/15 00:00:01"})
    public void testFail(String str) {
        Object c = converter.convert(str, LocalDate.class);
        Assertions.assertFalse(c instanceof LocalDate);
        Assertions.assertEquals(str, c);
    }
}