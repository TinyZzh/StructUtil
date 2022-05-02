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

import java.io.Serial;
import java.util.Arrays;

public class OptionalDescriptor extends FieldDescriptor {
    @Serial
    private static final long serialVersionUID = 8949543119635057452L;

    private SingleFieldDescriptor[] descriptors;

    public OptionalDescriptor() {
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
                "descriptors=" + Arrays.toString(descriptors) +
                ", name='" + name + '\'' +
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
