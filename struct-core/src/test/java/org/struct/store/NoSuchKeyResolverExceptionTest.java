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

package org.struct.store;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The core {@link NoSuchKeyResolverException} must keep all the 5 constructors so that the
 * spring shell can stay source compatible.
 *
 * @author TinyZ.
 */
class NoSuchKeyResolverExceptionTest {

    @Test
    void raiseException() {
        Assertions.assertThrows(NoSuchKeyResolverException.class, () -> {
            throw new NoSuchKeyResolverException();
        });
    }

    @Test
    void raiseException1() {
        NoSuchKeyResolverException e = new NoSuchKeyResolverException("msg");
        Assertions.assertEquals("msg", e.getMessage());
    }

    @Test
    void raiseException2() {
        Throwable cause = new IllegalStateException();
        NoSuchKeyResolverException e = new NoSuchKeyResolverException("msg", cause);
        Assertions.assertEquals("msg", e.getMessage());
        Assertions.assertSame(cause, e.getCause());
    }

    @Test
    void raiseException3() {
        Throwable cause = new IllegalStateException();
        NoSuchKeyResolverException e = new NoSuchKeyResolverException(cause);
        Assertions.assertSame(cause, e.getCause());
    }

    @Test
    void raiseException4() {
        NoSuchKeyResolverException e = new NoSuchKeyResolverException("msg", new NoSuchKeyResolverException(), false, true);
        Assertions.assertEquals("msg", e.getMessage());
    }
}
