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

    /**
     * Load and initialize {@link StructStore} data.
     * Update store status `NORMAL` -> `INITIALIZING` -> `DONE`
     */
    void initialize();

    /**
     * Is struct store initialized.
     *
     * @return true if struct store has been initialized, otherwise false.
     */
    boolean isInitialized();

    /**
     * Get class of the store bean instances.
     *
     * @return class of the store bean instances.
     */
    Class<B> clzOfBean();

    void setClzOfBean(Class<B> clzOfBean);

    /**
     * Reload struct store data.
     */
    void reload();

    /**
     * Reset status to `NORMAL` and clear struct store data.
     */
    void dispose();

    /**
     * the store element's amount.
     *
     * @return store element's amount.
     */
    int size();

    /**
     * Get struct store all element.
     *
     * @return struct store all element.
     */
    List<B> getAll();

    /**
     * Get struct store element by the key.
     *
     * @param key the element key.
     * @return the element by the key.
     */
    B get(K key);

    /**
     * Get struct store or return default instance.
     *
     * @param key the element key.
     * @param dv  the struct store default instance.
     * @return the struct store.
     */
    B getOrDefault(K key, B dv);

    /**
     * Try return struct store element by the key.
     *
     * @param key the element key.
     * @return optional with the element by the key.
     * @see Optional
     */
    Optional<B> tryGet(K key);

    /**
     * Get multiple element by keys array.
     *
     * @param keys multiple element key array.
     * @return element list.
     */
    List<B> lookup(K... keys);

    /**
     * Get elements by the filter.
     *
     * @param filter the element filter.
     * @return element list.
     */
    List<B> lookup(Predicate<B> filter);

}
