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

import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class OptionalDescriptor extends FieldDescriptor {
    @Serial
    private static final long serialVersionUID = 8949543119635057452L;

    private SingleFieldDescriptor[] descriptors;

    public OptionalDescriptor() {
    }

    public OptionalDescriptor(Object fieldOrRc, StructOptional anno, BiFunction<Object, StructField, SingleFieldDescriptor> func) {
        Objects.requireNonNull(fieldOrRc, "fieldOrRc");
        Objects.requireNonNull(anno, "anno");
        if (!anno.name().isEmpty()) {
            this.setName(anno.name());
        }
        String name = this.getName();
        //  handle default field name.
        if (null == name || name.isEmpty()) {
            if (fieldOrRc instanceof RecordComponent rc) {
                this.setName(rc.getName());
            } else {
                this.setName(((Field) fieldOrRc).getName());
            }
        }
        this.descriptors = Stream.of(anno.value())
                .map(sf -> func.apply(fieldOrRc, sf))
                .toArray(SingleFieldDescriptor[]::new);
    }

    public SingleFieldDescriptor[] getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(SingleFieldDescriptor[] descriptors) {
        this.descriptors = descriptors;
    }

    @Override
    public String toString() {
        return "OptionalDescriptor{" +
                "name='" + name + '\'' +
                ", descriptors=" + Arrays.toString(descriptors) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OptionalDescriptor that = (OptionalDescriptor) o;
        return Arrays.equals(descriptors, that.descriptors);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(descriptors);
        return result;
    }
}
