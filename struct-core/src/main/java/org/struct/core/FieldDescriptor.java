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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class FieldDescriptor implements Serializable, Comparable<FieldDescriptor> {
    @Serial
    private static final long serialVersionUID = 8949543119635057452L;

    protected String name;

    public FieldDescriptor() {
    }

    public FieldDescriptor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FieldDescriptor{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldDescriptor that = (FieldDescriptor) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(FieldDescriptor o) {
        //  优先SingleFieldDescriptor > OptionalFieldDescriptor
        if (this instanceof OptionalDescriptor
                && o instanceof OptionalDescriptor) {
            return this.getName().compareTo(o.getName());
        } else if (this instanceof OptionalDescriptor) {
            return 1;
        } else if (o instanceof OptionalDescriptor) {
            return -1;
        } else if (this instanceof SingleFieldDescriptor
                && o instanceof SingleFieldDescriptor) {
            //  优先级  field > ref field > custom converter field
            SingleFieldDescriptor fd0 = (SingleFieldDescriptor) this;
            SingleFieldDescriptor fd1 = (SingleFieldDescriptor) o;
            if (fd0.isReferenceField() && fd1.isReferenceField()) {
                if (fd0.getConverter() == null && fd1.getConverter() == null) {
                    return this.getName().compareTo(o.getName());
                } else {
                    return fd0.getConverter() == null ? -1 : 1;
                }
            } else {
                return fd0.isReferenceField() ? 1 : -1;
            }
        }
        return 0;
    }
}
