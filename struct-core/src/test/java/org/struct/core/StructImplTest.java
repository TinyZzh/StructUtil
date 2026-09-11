/*
 * Copyright (c) 2024. - TinyZ.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.struct.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Branch coverage for {@link StructImpl#add(String, Object)} when field-name interning is disabled.
 */
class StructImplTest {

    @Test
    void addWithoutIntern() {
        //  toggle the static interning flag off to exercise the `if (INTERN_FIELD_NAME)` false branch.
        boolean original = StructInternal.INTERN_FIELD_NAME;
        StructInternal.INTERN_FIELD_NAME = false;
        try {
            StructImpl impl = new StructImpl();
            impl.add("name", "value");
            assertEquals("value", impl.get("name"));
            //  empty / null values are ignored by add.
            StructImpl ignored = new StructImpl();
            ignored.add("skip", "");
            ignored.add("skip2", null);
            assertNull(ignored.get("skip"));
            assertNull(ignored.get("skip2"));
        } finally {
            StructInternal.INTERN_FIELD_NAME = original;
        }
    }
}
