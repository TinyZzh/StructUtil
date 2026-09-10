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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
        //  the core store requires a resolved key resolver.
        ognStore.setKeyResolver(String::hashCode);
        MapStructStore<Integer, String> store = spy(ognStore);
        doReturn(String.class).when(store).clzOfBean();
        service.postProcessAfterInitialization(store, "store");

        //  the real load fails (no options injected yet) but the failure is only logged,
        //  so the store still ends up initialized with empty data.
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

    /**
     * A second store for the same struct bean is not registered - the first one wins.
     */
    @Test
    public void testDuplicateStoreIsIgnored() {
        StructStoreConfig config = new StructStoreConfig();
        StructStoreService service = new StructStoreService(config);

        MapStructStore<Integer, String> first = store("first");
        MapStructStore<Integer, String> second = store("second");
        service.postProcessAfterInitialization(first, "first");
        service.postProcessAfterInitialization(second, "second");

        Assertions.assertEquals(1, service.stores().size());
        Assertions.assertSame(first, service.stores().iterator().next());
    }

    /**
     * The bean may be an aop proxy - the target class decides whether it is a store.
     */
    @Test
    public void testAopProxiedBean() {
        StructStoreService service = new StructStoreService(new StructStoreConfig());
        MapStructStore<Integer, String> store = store("proxied");

        org.springframework.aop.framework.ProxyFactory factory =
                new org.springframework.aop.framework.ProxyFactory(store);
        factory.setProxyTargetClass(false);
        Object proxy = factory.getProxy();

        service.postProcessAfterInitialization(proxy, "proxied");
        Assertions.assertEquals(1, service.stores().size());
    }

    /**
     * The banner is printed only when it is enabled.
     */
    @Test
    public void testBanner() {
        StructStoreConfig config = new StructStoreConfig();
        config.setBanner(true);
        new StructStoreService(config).afterSingletonsInstantiated();
    }

    /**
     * ... and skipped when it is disabled (the default is on, so this is the other branch).
     */
    @Test
    public void testBannerDisabled() {
        StructStoreConfig config = new StructStoreConfig();
        config.setBanner(false);
        new StructStoreService(config).afterSingletonsInstantiated();
    }

    /**
     * Without lazy loading the lookup never triggers an initialization.
     */
    @Test
    public void testEagerLoadDoesNotInitializeOnLookup() {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(false);
        StructStoreService service = new StructStoreService(config);

        MapStructStore<Integer, String> store = spy(store("eager"));
        service.postProcessAfterInitialization(store, "eager");

        service.getAll(String.class);
        verify(store, never()).initialize();
    }

    /**
     * With lazy loading on, every lookup initializes the store on first access.
     */
    @Test
    public void testLazyLoadInitializesOnLookup() {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(true);
        StructStoreService service = new StructStoreService(config);

        MapStructStore<Integer, String> store = spy(store("lazy"));
        service.postProcessAfterInitialization(store, "lazy");
        Assertions.assertFalse(store.isInitialized());

        service.getAll(String.class);
        verify(store, times(1)).initialize();

        //  the second access finds it already initialized.
        service.getAll(String.class);
        verify(store, times(1)).initialize();
    }

    private static MapStructStore<Integer, String> store(String name) {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolver(String::hashCode);
        return store;
    }

}