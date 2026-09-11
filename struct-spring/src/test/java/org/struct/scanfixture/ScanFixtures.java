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

package org.struct.scanfixture;

import java.util.List;
import java.util.function.Predicate;

/**
 * Scanning fixtures for {@code ClassPathStructScannerTest} - they live in their own package so
 * that the {@code @ComponentScan} based tests (which scan {@code org.struct.spring.support}) never
 * pick them up as real beans.
 *
 * @author TinyZ.
 */
public class ScanFixtures {

    /**
     * A concrete store that directly extends the core skeleton - the include filter has to match it.
     */
    public static class DirectCoreStore extends org.struct.store.AbstractStructStore<Integer, String> {

        @Override
        public void initialize() {
            if (casStatusInit()) {
                casStatusDone();
            }
        }

        @Override
        public void dispose() {
            casStatusReset();
        }

        @Override
        public List<String> getAll() {
            return List.of();
        }

        @Override
        public String get(Integer key) {
            return null;
        }

        @Override
        public List<String> lookup(Predicate<String> filter) {
            return List.of();
        }
    }

    /**
     * A concrete class that is not a store at all.
     */
    public static class Plain {
    }

    /**
     * An abstract class - never a scan candidate.
     */
    public abstract static class AbstractOne {
    }
}
