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
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.annotation.StructSheet;
import org.struct.core.converter.LocalDateConverter;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldDescriptorTest {

    @Test
    public void constructor() {
        SingleFieldDescriptor descriptor = new SingleFieldDescriptor();
        new SingleFieldDescriptor("name", null, null, null, null, false, null);

        {
            Assertions.assertEquals(
                    new SingleFieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null),
                    new SingleFieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null)
            );
        }
        Assertions.assertNotNull(descriptor.toString());
    }

    @Test
    public void testSort() {
        List<Field> fields = Reflects.resolveAllFields(FieldDescriptorSort.class);
        List<FieldDescriptor> list = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            StructOptional anno;
            if (null != (anno = AnnotationUtils.findAnnotation(StructOptional.class, field))) {
                list.add(new OptionalDescriptor(field, anno,
                        (f, an) -> new SingleFieldDescriptor(f, an, false))) ;
            } else {
                list.add(new SingleFieldDescriptor(field, AnnotationUtils.findAnnotation(StructField.class, field), false));
            }
        }
        Collections.sort(list);
        Assertions.assertEquals(10, list.size());
        //  5,2,1,0,6,7,8,9,3,4

    }

    @StructSheet()
    static class FieldDescriptorSort {

        public int var0;
        @StructField()
        public int var1;
        @StructField()
        public int var2;
        @StructOptional(name = "var3", value = {
                @StructField()
        })
        public int var3;
        @StructOptional(name = "var4", value = {
                @StructField()
        })
        public int var4;
        @StructField(converter = LocalDateConverter.class)
        public int var5;
        @StructField(ref = FieldDescriptorSort.class)
        public int var6;
        @StructField(ref = FieldDescriptorSort.class)
        public int var7;
        @StructField(ref = FieldDescriptorSort.class, converter = LocalDateConverter.class)
        public int var8;
        @StructField(ref = FieldDescriptorSort.class, converter = LocalDateConverter.class)
        public int var9;

    }

}