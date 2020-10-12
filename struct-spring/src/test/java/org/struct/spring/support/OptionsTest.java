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

package org.struct.spring.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.spring.annotation.StructStoreOptions;
import org.struct.util.AnnotationUtils;

/**
 * @author TinyZ.
 * @date 2020-10-12.
 */
class OptionsTest {

    @Test
    public void test() {
        Options options = new Options();
        options.setWorkspace("xx");
        options.setLazyLoad(false);
        options.setWaitForInit(false);
        Assertions.assertEquals("xx", options.getWorkspace());
        Assertions.assertFalse(options.isLazyLoad());
        Assertions.assertFalse(options.isWaitForInit());
    }

    @Test
    public void testGenerateAnnotation() {
        StructStoreOptions annotation = AnnotationUtils.findAnnotation(StructStoreOptions.class, AnnotationClz.class);
        Options options = Options.generate(annotation);
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
        Options options = Options.generate(config);
        Assertions.assertEquals("xx", options.getWorkspace());
        Assertions.assertFalse(options.isWaitForInit());
        Assertions.assertFalse(options.isLazyLoad());
    }

    @StructStoreOptions(workspace = "xx", lazyLoad = false, waitForInit = false)
    static class AnnotationClz {

    }

}