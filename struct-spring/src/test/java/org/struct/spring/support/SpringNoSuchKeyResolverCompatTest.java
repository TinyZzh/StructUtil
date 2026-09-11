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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The spring {@code NoSuchKeyResolverException} is a standalone class - it must <b>not</b> be a
 * subclass of the core one, otherwise the old catch clauses silently swallow the core exception
 * (and vice versa).
 *
 * @author TinyZ.
 */
@SuppressWarnings("removal")
class SpringNoSuchKeyResolverCompatTest {

    @Test
    void testSpringExceptionIsNotCoreSubclass() {
        Assertions.assertFalse(org.struct.store.NoSuchKeyResolverException.class
                .isAssignableFrom(org.struct.spring.exceptions.NoSuchKeyResolverException.class));
        Assertions.assertFalse(org.struct.spring.exceptions.NoSuchKeyResolverException.class
                .isAssignableFrom(org.struct.store.NoSuchKeyResolverException.class));
    }

    @Test
    void testSpringStoreThrowsSpringException() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, store::initialize);
        Assertions.assertInstanceOf(org.struct.spring.exceptions.NoSuchKeyResolverException.class, e);
        Assertions.assertFalse(e instanceof org.struct.store.NoSuchKeyResolverException);
    }

    @Test
    void testCoreStoreThrowsCoreException() {
        org.struct.store.MapStructStore<Integer, String> store = new org.struct.store.MapStructStore<>(String.class);
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, store::initialize);
        Assertions.assertInstanceOf(org.struct.store.NoSuchKeyResolverException.class, e);
        Assertions.assertFalse(e instanceof org.struct.spring.exceptions.NoSuchKeyResolverException);
    }
}
