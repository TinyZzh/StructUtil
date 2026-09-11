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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.filter.StructBeanFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Branch coverage for {@link StructWorker} corner cases (e.g. abstract bean filters).
 */
class StructWorkerBranchesTest {

    @StructSheet(fileName = "abstract_filter.csv", filter = AbstractBeanFilter.class)
    static class BeanWithAbstractFilter {
        @StructField(name = "id")
        private int id;
        @StructField(name = "name")
        private String name;
    }

    /**
     * An abstract filter: {@code StructWorker#wrapCellFilter} must skip instantiation for abstract classes.
     */
    abstract static class AbstractBeanFilter extends StructBeanFilter<BeanWithAbstractFilter> {
        public AbstractBeanFilter(Consumer<BeanWithAbstractFilter> cellHandler) {
            super(cellHandler);
        }

        @Override
        public boolean test(BeanWithAbstractFilter bean) {
            return true;
        }
    }

    @Test
    void abstractFilterIsNotInstantiated() throws Exception {
        List<BeanWithAbstractFilter> list = new StructWorker<>("classpath:/org/struct/core/", BeanWithAbstractFilter.class)
                .toList(ArrayList::new);
        Assertions.assertFalse(list.isEmpty());
    }
}
