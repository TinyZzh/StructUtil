/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

package org.struct.spring.support;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
public interface StructStore<K, B> {

    /**
     * Cache's unique identify.
     *
     * @return Cache's unique identify
     */
    String identify();

    void initialize();

    boolean isInitialized();

    /**
     * Get class of the store bean instances.
     *
     * @return class of the store bean instances.
     */
    Class<B> clzOfBean();

    void setClzOfBean(Class<B> clzOfBean);

    void reload();

    void dispose();

    /**
     * the store element's amount.
     *
     * @return store element's amount.
     */
    int size();

    List<B> getAll();

    B get(K key);

    B getOrDefault(K key, B dv);

    Optional<B> tryGet(K key);

    List<B> lookup(K... keys);

    List<B> lookup(Predicate<B> filter);

}
