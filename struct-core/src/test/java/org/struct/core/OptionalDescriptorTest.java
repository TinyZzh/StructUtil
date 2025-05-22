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

package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;

import java.lang.reflect.Field;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class OptionalDescriptorTest {

    @Test
    public void test() {
        SingleFieldDescriptor[] descriptors = new SingleFieldDescriptor[1];
        for (Field field : Clz.class.getDeclaredFields()) {
            descriptors[0] = new SingleFieldDescriptor(field, field.getAnnotation(StructField.class));
        }
        OptionalDescriptor descriptor1 = new OptionalDescriptor();
        descriptor1.setDescriptors(descriptors);
        OptionalDescriptor descriptor2 = new OptionalDescriptor();
        descriptor2.setDescriptors(descriptors);
        Assertions.assertNotNull(descriptor1.toString());
        Assertions.assertEquals(descriptor1.getDescriptors(), descriptor2.getDescriptors());
        Assertions.assertEquals(descriptor1.hashCode(), descriptor2.hashCode());
        Assertions.assertEquals(descriptor1, descriptor2);
    }

    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class Clz {
        @StructField(name = "id", ref = String.class)
        private int id;
    }
}