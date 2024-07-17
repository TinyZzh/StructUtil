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

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class MapStructStoreTest {

    @Test
    public void testInit() {
        MapStructStore<Integer, String> store = spy(new MapStructStore<>(String.class));
        doReturn(new HashMap<>()).when(store).loadStructData();
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    public void testInitFailure() {
        MapStructStore<Integer, String> store = spy(new MapStructStore<>(String.class));
        doReturn(new HashMap<>()).when(store).loadStructData();
        store.casStatusInit();
        store.casStatusDone();
        Options options = new Options();
        options.setWaitForInit(true);
        store.setOptions(options);
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    public void testSetter() {
        MapStructStore<Object, Object> store = spy(new MapStructStore<>(Object.class));
        store.setKeyResolverBeanName("xxkey");
        Assertions.assertEquals("xxkey", store.getKeyResolverBeanName());
        store.setKeyResolverBeanClass(MapKeyFieldResolver.class);
        Assertions.assertEquals(MapKeyFieldResolver.class, store.getKeyResolverBeanClass());
        Assertions.assertNull(store.getKeyResolver());
        store.setKeyResolver(new MapKeyFieldResolver("id"));
        Assertions.assertNotNull(store.getKeyResolver());
        Assertions.assertNotNull(store.toString());
    }
}