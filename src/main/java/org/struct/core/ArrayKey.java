/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

import java.util.Arrays;

/**
 * @author TinyZ.
 * @version 2019.04.06
 */
public final class ArrayKey {

    private final Object[] ary;

    public ArrayKey(Object[] ary) {
        this.ary = ary;
    }

    public Object[] getAry() {
        return ary;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ary);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayKey))
            return false;
        return Arrays.equals(ary, ((ArrayKey) obj).getAry());
    }

    @Override
    public String toString() {
        return Arrays.toString(ary);
    }
}
