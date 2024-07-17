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

package org.struct.exception;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EndOfExcelSheetExceptionTest {


    @Test()
    public void raiseException() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException();
        });
    }

    @Test()
    public void raiseException1() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg");
        });
    }

    @Test()
    public void raiseException2() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException());
        });
    }

    @Test()
    public void raiseException3() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException(new EndOfExcelSheetException());
        });
    }

    @Test()
    public void raiseException4() {
        Assertions.assertThrows(EndOfExcelSheetException.class, () -> {
            throw new EndOfExcelSheetException("msg", new EndOfExcelSheetException(), false, true);
        });
    }
}