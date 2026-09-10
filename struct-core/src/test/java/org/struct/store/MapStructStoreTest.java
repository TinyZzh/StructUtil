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

package org.struct.store;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;

import java.util.HashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class MapStructStoreTest {

    private static StoreOptions options() {
        StoreOptions options = new StoreOptions();
        options.setWorkspace("classpath:/org/struct/store/");
        return options;
    }

    @Test
    public void testInit() {
        MapStructStore<Integer, String> store = spy(new MapStructStore<>(String.class));
        store.setKeyResolver(String::hashCode);
        doReturn(new HashMap<>()).when(store).loadStructData();
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    public void testInitWaitForInit() {
        MapStructStore<Integer, String> store = spy(new MapStructStore<>(String.class));
        store.setKeyResolver(String::hashCode);
        doReturn(new HashMap<>()).when(store).loadStructData();
        store.casStatusInit();
        store.casStatusDone();
        StoreOptions options = new StoreOptions();
        options.setWaitForInit(true);
        store.setOptions(options);
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    /**
     * A store without a {@link StructKeyResolver} must fail fast instead of starting with an
     * empty store.
     */
    @Test
    public void testNoKeyResolverFailsFast() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        Assertions.assertThrows(NoSuchKeyResolverException.class, store::initialize);
        //  the exception is produced by the overridable factory hook.
        Assertions.assertInstanceOf(NoSuchKeyResolverException.class, store.noSuchKeyResolverException("x"));
    }

    @Test
    public void testSetter() {
        MapStructStore<Object, Object> store = new MapStructStore<>(Object.class);
        Assertions.assertNull(store.getKeyResolver());
        MapKeyFieldResolver resolver = new MapKeyFieldResolver("id");
        store.setKeyResolver(resolver);
        Assertions.assertSame(resolver, store.getKeyResolver());
        Assertions.assertNotNull(store.toString());
    }

    /**
     * The real {@code loadStructData()} implementation reads the configured workspace.
     */
    @Test
    public void testLoadStructDataFromWorkspace() {
        MapStructStore<Object, StoreBean> store = new MapStructStore<>(StoreBean.class);
        store.setOptions(options());
        store.setKeyResolver(b -> b.key);

        store.initialize();

        Assertions.assertTrue(store.isInitialized());
        Assertions.assertEquals(3, store.size());
        Assertions.assertEquals(3, store.getAll().size());
        Assertions.assertEquals(1, store.getAll().get(0).key);
        Assertions.assertEquals("11", store.getAll().get(0).val);

        //  the key is resolved by the key resolver.
        Assertions.assertNotNull(store.get(1));
        Assertions.assertEquals("11", store.get(1).val);
        Assertions.assertNull(store.get(100));
        Assertions.assertNotNull(store.tryGet(2).orElse(null));
        Assertions.assertEquals(1, store.lookup(x -> x.key == 3).size());
        Assertions.assertEquals(0, store.lookup(x -> x.key == 300).size());
        Assertions.assertEquals(2, store.lookup(1, 2, 300).size());

        //  the returned collection is immutable
        Assertions.assertThrows(UnsupportedOperationException.class, () -> store.getAll().add(new StoreBean()));

        store.dispose();
        Assertions.assertTrue(store.getAll().isEmpty());
        Assertions.assertFalse(store.isInitialized());
    }

    /**
     * A failing load is only logged, the store still ends up "initialized".
     * <p>
     * NOTE: this is a known issue - a configuration error lets the application start with empty
     * data instead of failing fast.
     */
    @Test
    public void testLoadStructDataFailureIsLoggedOnly() {
        MapStructStore<Object, StoreBean> store = spy(new MapStructStore<>(StoreBean.class));
        store.setOptions(options());
        store.setKeyResolver(b -> b.key);
        doThrow(new IllegalStateException("boom")).when(store).loadStructData();

        store.initialize();
        Assertions.assertTrue(store.isInitialized());
        Assertions.assertEquals(0, store.size());
        Assertions.assertTrue(store.getAll().isEmpty());
    }

    /**
     * The options are mandatory for the real load.
     */
    @Test
    public void testLoadStructDataWithoutOptions() {
        MapStructStore<Object, StoreBean> store = new MapStructStore<>(StoreBean.class);
        store.setKeyResolver(b -> b.key);
        Assertions.assertThrows(IllegalStateException.class, store::loadStructData);
    }

    /**
     * A concurrent {@code initialize()} loses the init race: the store returns immediately and
     * must not wait when {@code waitForInit} is off.
     */
    @Test
    public void testInitializeAlreadyInitializingNotWaiting() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolver(String::hashCode);
        store.setOptions(new StoreOptions());
        Assertions.assertTrue(store.casStatusInit());

        store.initialize();
        Assertions.assertFalse(store.isInitialized());
    }

    /**
     * Same, but the options haven't been injected - the short circuit must not fall back to any wait.
     */
    @Test
    public void testInitializeAlreadyInitializingWithoutOptions() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolver(String::hashCode);
        Assertions.assertTrue(store.casStatusInit());

        store.initialize();
        Assertions.assertFalse(store.isInitialized());
    }

    /**
     * {@code initialize()} guards the missing resolver up front, so the {@code loadStructData()}
     * guard is only reachable by a direct call - keep it covered anyway.
     */
    @Test
    public void testLoadStructDataWithoutKeyResolver() {
        MapStructStore<Object, StoreBean> store = new MapStructStore<>(StoreBean.class);
        store.setOptions(options());
        Assertions.assertThrows(NoSuchKeyResolverException.class, store::loadStructData);
    }

    @StructSheet(fileName = "tpl_list_store.json")
    public static class StoreBean {
        public int key;
        public String val;
    }
}
