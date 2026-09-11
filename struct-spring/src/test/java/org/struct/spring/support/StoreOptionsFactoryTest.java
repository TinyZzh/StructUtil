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
import org.struct.spring.annotation.StructStoreOptions;
import org.struct.store.StoreOptions;
import org.struct.util.AnnotationUtils;

/**
 * The spring side keeps assembling the core {@link StoreOptions} out of the spring carriers.
 *
 * @author TinyZ.
 */
class StoreOptionsFactoryTest {

    @Test
    public void testGenerateAnnotation() {
        StructStoreOptions annotation = AnnotationUtils.findAnnotation(StructStoreOptions.class, AnnotationClz.class);
        StoreOptions options = StoreOptionsFactory.generate(annotation);
        Assertions.assertEquals("xx", options.getWorkspace());
        Assertions.assertFalse(options.isLazyLoad());
        Assertions.assertFalse(options.isWaitForInit());
    }

    @Test
    public void testGenerateConfig() {
        StructStoreConfig config = new StructStoreConfig();
        config.setWorkspace("xx");
        config.setLazyLoad(false);
        config.setSyncWaitForInit(false);
        StoreOptions options = StoreOptionsFactory.generate(config);
        Assertions.assertEquals("xx", options.getWorkspace());
        Assertions.assertFalse(options.isWaitForInit());
        Assertions.assertFalse(options.isLazyLoad());
    }

    @StructStoreOptions(workspace = "xx", lazyLoad = false, waitForInit = false)
    static class AnnotationClz {

    }
}
