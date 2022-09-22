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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/**
 * @author TinyZ.
 * @version 2022.09.15
 */
class BomInputStreamTest {

    @Test
    public void test() throws Exception {
        // Assertions.assertEquals("abc", readBytes(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF, 'a', 'b', 'c', '\r'}));
        // Assertions.assertEquals("abc", readBytes(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00, 'a', 'b', 'c', '\r'}));
        Assertions.assertEquals("abc", readBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'a', 'b', 'c', '\r'}));
        Assertions.assertEquals("abc", readBytes(new byte[]{(byte) 0xFE, (byte) 0xFF, 'a', 'b', 'c', '\r'}));
        Assertions.assertEquals("abc", readBytes(new byte[]{(byte) 0xFF, (byte) 0xFE, 'a', 'b', 'c', '\r'}));
    }

    private String readBytes(byte[] bytes) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             BomInputStream bis = new BomInputStream(bais);
             BufferedReader reader = new BufferedReader(new InputStreamReader(bis, bis.getBomCharset()))) {
            return reader.readLine();
        }
    }

}