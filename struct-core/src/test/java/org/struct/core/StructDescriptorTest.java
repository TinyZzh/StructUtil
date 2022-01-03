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

package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class StructDescriptorTest {

    @Test
    public void test0() {
        StructDescriptor sd0 = new StructDescriptor(SdData.class);
        sd0.setEndOrder(1);
        sd0.setFileName("2");
        sd0.setStartOrder(3);
        sd0.setSheetName("4");
        sd0.setFilter(StructBeanFilter.class);
        sd0.setMatcher(WorkerMatcher.class);
        Assertions.assertEquals(1, sd0.getEndOrder());
        Assertions.assertEquals("2", sd0.getFileName());
        Assertions.assertEquals(3, sd0.getStartOrder());
        Assertions.assertEquals("4", sd0.getSheetName());
        Assertions.assertEquals(StructBeanFilter.class, sd0.getFilter());
        Assertions.assertEquals(WorkerMatcher.class, sd0.getMatcher());
        System.out.println(sd0.toString());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StructDescriptor(SdDataWithoutAnnotation.class);
        });
    }

    @Test
    public void test() {
        StructDescriptor sd0 = new StructDescriptor();
        sd0.setEndOrder(1);
        sd0.setFileName("2");
        sd0.setStartOrder(3);
        sd0.setSheetName("4");
        sd0.setFilter(StructBeanFilter.class);
        sd0.setMatcher(FileExtensionMatcher.class);
        Assertions.assertEquals(1, sd0.getEndOrder());
        Assertions.assertEquals("2", sd0.getFileName());
        Assertions.assertEquals(3, sd0.getStartOrder());
        Assertions.assertEquals("4", sd0.getSheetName());
        Assertions.assertEquals(StructBeanFilter.class, sd0.getFilter());
        Assertions.assertEquals(FileExtensionMatcher.class, sd0.getMatcher());
        System.out.println(sd0.toString());
    }

    @Test
    public void testEquals() {
        StructDescriptor sd0 = new StructDescriptor();
        StructDescriptor sd1 = new StructDescriptor();
        Assertions.assertEquals(sd0.hashCode(), sd1.hashCode());
        Assertions.assertEquals(sd0, sd1);
    }

    @StructSheet(fileName = "2", sheetName = "4", endOrder = 1, startOrder = 3)
    static class SdData {

    }

    static class SdDataWithoutAnnotation {

    }
}