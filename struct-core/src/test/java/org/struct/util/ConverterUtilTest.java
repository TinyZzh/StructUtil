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

package org.struct.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

import static org.struct.util.ConverterUtil.*;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class ConverterUtilTest {

    @Test
    public void testDecodeBigInteger() {
        Assertions.assertTrue(isBooleanTrue("true"));
        Assertions.assertTrue(isBooleanTrue("1"));
        Assertions.assertTrue(isBooleanTrue("y"));
        Assertions.assertTrue(isBooleanTrue("yes"));

        Assertions.assertTrue(isHexNumber("0x1"));
        Assertions.assertTrue(isHexNumber("0X1"));
        Assertions.assertTrue(isHexNumber("#1"));

        Assertions.assertEquals(BigInteger.valueOf(1), decodeBigInteger("0x1"));
        Assertions.assertEquals(BigInteger.valueOf(1), decodeBigInteger("#1"));
        Assertions.assertEquals(BigInteger.valueOf(1), decodeBigInteger("01"));
        Assertions.assertEquals(BigInteger.valueOf(-1), decodeBigInteger("-01"));

        Assertions.assertEquals(1L, checkedLongValue(BigDecimal.valueOf(1L), long.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkedLongValue(BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE), long.class));
    }

    @Test
    public void testIsBasicType() {
        Assertions.assertTrue(isBasicType(int.class));
        Assertions.assertTrue(isBasicType(Integer.class));
        Assertions.assertTrue(isBasicType(long.class));
        Assertions.assertTrue(isBasicType(Long.class));
        Assertions.assertTrue(isBasicType(Date.class));
        Assertions.assertTrue(isBasicType(LocalDate.class));

        Assertions.assertFalse(isBasicType(Object.class));
    }

}