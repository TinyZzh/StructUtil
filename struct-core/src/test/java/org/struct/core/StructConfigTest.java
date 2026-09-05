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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class StructConfigTest {

    /**
     * {@link StructConfig} is a process wide singleton. A test that mutates it must
     * restore the original values, otherwise the change leaks into every test that
     * runs afterwards (JUnit gives no ordering guarantee).
     */
    private boolean prevIgnoreEmptyRow;
    private boolean prevAllowCircularReferences;
    private boolean prevStructRequiredDefault;

    @BeforeEach
    public void remember() {
        StructConfig config = StructConfig.INSTANCE;
        this.prevIgnoreEmptyRow = config.isIgnoreEmptyRow();
        this.prevAllowCircularReferences = config.isAllowCircularReferences();
        this.prevStructRequiredDefault = config.isStructRequiredDefault();
    }

    @AfterEach
    public void restore() {
        StructConfig config = StructConfig.INSTANCE;
        config.setIgnoreEmptyRow(this.prevIgnoreEmptyRow);
        config.setAllowCircularReferences(this.prevAllowCircularReferences);
        config.setStructRequiredDefault(this.prevStructRequiredDefault);
    }

    @Test
    public void test() {
        StructConfig config = StructConfig.INSTANCE;
        config.setIgnoreEmptyRow(false);
        config.setAllowCircularReferences(false);
        config.setStructRequiredDefault(false);
        Assertions.assertFalse(config.isIgnoreEmptyRow());
        Assertions.assertFalse(config.isStructRequiredDefault());
        Assertions.assertFalse(config.isAllowCircularReferences());
    }
}