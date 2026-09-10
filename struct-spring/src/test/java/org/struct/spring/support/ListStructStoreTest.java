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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * The spring {@link ListStructStore} shell only adds the life cycle bridging; the behaviour
 * itself is covered by {@code org.struct.store.ListStructStoreTest}.
 *
 * @author TinyZ.
 */
@SuppressWarnings("removal")
class ListStructStoreTest {

    @Test
    public void testLifecycle() throws Exception {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(true);
        ApplicationContext ctx = mock(ApplicationContext.class);
        doReturn(config).when(ctx).getBean(StructStoreConfig.class);

        ListStructStore<String> store = new ListStructStore<>(String.class);
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertNotNull(store.getOptions());
        //  the key based access stays unsupported.
        Assertions.assertThrows(UnsupportedOperationException.class, () -> store.get("xx"));
        store.destroy();
    }

    @Test
    public void testIsCoreStore() {
        Assertions.assertTrue(org.struct.store.StructStore.class.isAssignableFrom(ListStructStore.class));
        Assertions.assertInstanceOf(org.struct.store.ListStructStore.class, new ListStructStore<String>());
    }
}
