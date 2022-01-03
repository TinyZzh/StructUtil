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

package org.struct.core.matcher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class FileExtensionMatcherTest {

    @Test
    public void testConstructors() {
        new FileExtensionMatcher(FileExtensionMatcher.FILE_BINARY);
        new FileExtensionMatcher(1, FileExtensionMatcher.FILE_BINARY);
        new FileExtensionMatcher(10L, FileExtensionMatcher.FILE_BINARY);
        new FileExtensionMatcher(10L, 1, FileExtensionMatcher.FILE_BINARY);
    }

    @Test
    public void testEquals() {
        FileExtensionMatcher o0 = new FileExtensionMatcher(FileExtensionMatcher.FILE_BINARY);
        FileExtensionMatcher o1 = new FileExtensionMatcher(FileExtensionMatcher.FILE_BINARY);
        System.out.println(o1);
        Assertions.assertEquals(o0.hashCode(), o1.hashCode());
        Assertions.assertEquals(o0.order(), o1.order());
        Assertions.assertEquals(o0, o1);
    }

    @Test
    public void testMatchFile() {
        FileExtensionMatcher fem = new FileExtensionMatcher(100L, FileExtensionMatcher.FILE_BINARY);
        File file = mock(File.class);
        doReturn(true).when(file).exists();
        doReturn(false).when(file).canRead();
        Assertions.assertFalse(fem.matchFile(file));
        Mockito.reset(file);
        //
        doReturn(true).when(file).exists();
        doReturn(true).when(file).canRead();
        doReturn(1000L).when(file).length();
        Assertions.assertFalse(fem.matchFile(file));
        Mockito.reset(file);
        //
        doReturn("xx.dd").when(file).getName();
        doReturn(true).when(file).exists();
        doReturn(true).when(file).canRead();
        doReturn(10L).when(file).length();
        Assertions.assertFalse(fem.matchFile(file));
        Mockito.reset(file);
        //
        doReturn(true).when(file).exists();
        doReturn(true).when(file).canRead();
        doReturn(10L).when(file).length();
        doReturn("xx.binary").when(file).getName();
        Assertions.assertTrue(fem.matchFile(file));
    }

}