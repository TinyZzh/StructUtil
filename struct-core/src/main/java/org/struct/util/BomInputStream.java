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

package org.struct.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author TinyZ.
 * @version 2022.09.14
 */
public final class BomInputStream extends PushbackInputStream {

    private static final int MAX_BOM_SIZE = 4;

    private static final ByteOrderMark[] BOM_PREFIX_ARRAY = new ByteOrderMark[]{
            new ByteOrderMark(Charset.forName("UTF-32BE"), (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF),
            new ByteOrderMark(Charset.forName("UTF-32LE"), (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00),
            new ByteOrderMark(StandardCharsets.UTF_8, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF),
            new ByteOrderMark(StandardCharsets.UTF_16BE, (byte) 0xFE, (byte) 0xFF),
            new ByteOrderMark(StandardCharsets.UTF_16LE, (byte) 0xFF, (byte) 0xFE)
    };

    private final Charset charset;

    public BomInputStream(InputStream in) throws IOException {
        super(in, MAX_BOM_SIZE);
        this.charset = this.checkAndSkipBom(in);
    }

    Charset checkAndSkipBom(InputStream in) throws IOException {
        // if file without BOM mark, unread all bytes
        Charset encoding = StandardCharsets.UTF_8;
        int n, unread;
        byte[] bom = new byte[MAX_BOM_SIZE];
        unread = n = in.read(bom, 0, bom.length);
        //
        for (ByteOrderMark mark : BOM_PREFIX_ARRAY) {
            boolean match = true;
            for (int i = 0; i < mark.bytes.length; i++) {
                if (mark.bytes[i] != bom[i]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                encoding = mark.charset;
                unread = n - mark.bytes.length;
                break;
            }
        }
        if (unread > 0)
            this.unread(bom, (n - unread), unread);
        return encoding;
    }

    public Charset getBomCharset() {
        return this.charset;
    }

    static class ByteOrderMark {

        private final Charset charset;
        private final byte[] bytes;

        public ByteOrderMark(Charset charset, byte... bytes) {
            this.charset = charset;
            this.bytes = bytes;
        }
    }
}
