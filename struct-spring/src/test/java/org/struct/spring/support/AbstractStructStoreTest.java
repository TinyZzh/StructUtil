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
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Predicate;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * The spring shell bridges the spring life cycle to the core store's explicit
 * {@link org.struct.store.StructStore#initialize()}.
 *
 * @author TinyZ.
 */
class AbstractStructStoreTest {

    private static ApplicationContext context(boolean lazyLoad) {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(lazyLoad);
        ApplicationContext ctx = mock(ApplicationContext.class);
        doReturn(config).when(ctx).getBean(StructStoreConfig.class);
        return ctx;
    }

    @Test
    void testEagerInitialize() throws Exception {
        TestStore store = new TestStore(Bean.class);
        store.setApplicationContext(context(false));
        store.afterPropertiesSet();

        //  the options fall back to the global StructStoreConfig.
        Assertions.assertNotNull(store.getOptions());
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    void testLazyLoadSkipsInitialize() throws Exception {
        TestStore store = new TestStore(Bean.class);
        store.setApplicationContext(context(true));
        store.afterPropertiesSet();

        Assertions.assertNotNull(store.getOptions());
        Assertions.assertFalse(store.isInitialized());
    }

    @Test
    void testDestroyDelegatesToDispose() throws Exception {
        TestStore store = new TestStore(Bean.class);
        store.setApplicationContext(context(false));
        store.afterPropertiesSet();
        Assertions.assertTrue(store.isInitialized());
        store.destroy();
        Assertions.assertFalse(store.isInitialized());
    }

    @Test
    void testExplicitOptionsWin() throws Exception {
        TestStore store = new TestStore(Bean.class);
        org.struct.store.StoreOptions options = new org.struct.store.StoreOptions();
        options.setWorkspace("explicit");
        store.setOptions(options);
        store.setApplicationContext(context(false));
        store.afterPropertiesSet();
        Assertions.assertSame(options, store.getOptions());
    }

    static class Bean {
    }

    /**
     * Deliberately an inner (non independent) class so that the {@code @ComponentScan} of the
     * other tests in this package doesn't pick it up as a real store bean.
     */
    class TestStore extends AbstractStructStore<Long, Bean> {

        TestStore() {
        }

        TestStore(Class<Bean> clzOfBean) {
            super(clzOfBean);
        }

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
        public List<Bean> getAll() {
            return List.of();
        }

        @Override
        public Bean get(Long key) {
            return null;
        }

        @Override
        public List<Bean> lookup(Predicate<Bean> filter) {
            return List.of();
        }
    }
}
