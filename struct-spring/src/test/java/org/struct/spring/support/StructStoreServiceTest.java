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

import java.util.HashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author TinyZ.
 * @date 2020-10-30.
 */
class StructStoreServiceTest {

    @Test
    public void test_constructor() {
        StructStoreConfig config = new StructStoreConfig();
        StructStoreService s1 = new StructStoreService(config);
        StructStoreService s2 = new StructStoreService();
        s2.postProcessAfterInitialization(config, "");
        s2.initialize(String.class);
    }

    @Test
    public void test_operate() throws Exception {
        StructStoreConfig config = new StructStoreConfig();
        StructStoreService service = new StructStoreService();
        service.setConfig(config);
        service.postProcessAfterInitialization(config, "config");
        service.afterSingletonsInstantiated();
        MapStructStore<Integer, String> ognStore = new MapStructStore<>(String.class);
        MapStructStore<Integer, String> store = spy(ognStore);
        doReturn(String.class).when(store).clzOfBean();
        service.postProcessAfterInitialization(store, "store");

        doReturn(new HashMap<Integer, String>()).when(store).loadStructData();
        service.getAll(String.class);
        verify(store, times(1)).initialize();

        Assertions.assertNull(service.get(String.class, ""));
        Assertions.assertEquals("1000", service.getOrDefault(String.class, 1, "1000"));
        Assertions.assertFalse(service.tryGet(String.class, 1).isPresent());
        Assertions.assertTrue(service.getAll(String.class).isEmpty());
        Assertions.assertTrue(service.lookup(String.class, 1, 2, 3).isEmpty());
        Assertions.assertTrue(service.lookup(String.class, s -> true).isEmpty());
        service.isEmpty();
        service.dispose(String.class);
        verify(store, times(1)).dispose();
        service.reload(String.class);
        verify(store, times(1)).reload();
        service.destroy();

    }


}