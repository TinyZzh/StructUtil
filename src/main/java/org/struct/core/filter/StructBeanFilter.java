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

package org.struct.core.filter;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Java Bean Filter. filter the java bean after struct data convert done.
 *
 * @param <T>
 */
public abstract class StructBeanFilter<T> implements Consumer<T>, Predicate<T> {

    /**
     * the real cell handler.
     */
    private final Consumer<T> cellHandler;

    /**
     * the constructor must be implement.
     *
     * @param cellHandler the real cell handler.
     */
    public StructBeanFilter(Consumer<T> cellHandler) {
        this.cellHandler = cellHandler;
    }

    /**
     * Unchangeable.
     */
    @Override
    public final void accept(T t) {
        if (test(t)) {
            cellHandler.accept(t);
        }
    }

    /**
     * the user custom filter implement.
     *
     * @param t the java bean instance.
     * @return return true if this bean `{@code t}` can be cell handler accepted. otherwise false.
     */
    @Override
    public abstract boolean test(T t);
}
