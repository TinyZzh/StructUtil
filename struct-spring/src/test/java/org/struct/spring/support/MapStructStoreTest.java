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
 * The spring {@link MapStructStore} shell resolves the key resolver from the spring context.
 *
 * @author TinyZ.
 */
@SuppressWarnings({"removal", "unchecked"})
class MapStructStoreTest {

    private static ApplicationContext context() {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(true);
        ApplicationContext ctx = mock(ApplicationContext.class);
        doReturn(config).when(ctx).getBean(StructStoreConfig.class);
        return ctx;
    }

    @Test
    public void testSetters() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        Assertions.assertNull(store.getKeyResolverBeanName());
        Assertions.assertNull(store.getKeyResolverBeanClass());
        store.setKeyResolverBeanName("xxkey");
        Assertions.assertEquals("xxkey", store.getKeyResolverBeanName());
        store.setKeyResolverBeanClass(IdResolver.class);
        Assertions.assertEquals(IdResolver.class, store.getKeyResolverBeanClass());
        Assertions.assertNull(store.getKeyResolver());
        Assertions.assertNotNull(store.toString());
    }

    @Test
    public void testResolveByBeanClass() throws Exception {
        ApplicationContext ctx = context();
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanClass(IdResolver.class);
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertInstanceOf(IdResolver.class, store.getKeyResolver());
        Assertions.assertNotNull(store.getOptions());
    }

    @Test
    public void testResolveByBeanName() throws Exception {
        ApplicationContext ctx = context();
        org.struct.store.StructKeyResolver<Integer, String> resolver = new IdResolver();
        doReturn(resolver).when(ctx).getBean("kr", org.struct.store.StructKeyResolver.class);

        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanName("kr");
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertSame(resolver, store.getKeyResolver());
    }

    @Test
    public void testResolveFromContainer() throws Exception {
        ApplicationContext ctx = context();
        IdResolver resolver = new IdResolver();
        doReturn(java.util.Map.of("idResolver", resolver)).when(ctx).getBeansOfType(IdResolver.class);

        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanClass(IdResolver.class);
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertSame(resolver, store.getKeyResolver());
    }

    /**
     * The shell must keep throwing the spring exception type.
     */
    @Test
    public void testNoKeyResolverThrowsSpringException() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        Assertions.assertThrows(org.struct.spring.exceptions.NoSuchKeyResolverException.class,
                store::afterPropertiesSet);
    }

    /**
     * An explicitly injected key resolver wins - no container lookup happens at all.
     */
    @Test
    public void testExplicitKeyResolverSkipsLookup() throws Exception {
        ApplicationContext ctx = context();
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        org.struct.store.StructKeyResolver<Integer, String> resolver = new IdResolver();
        store.setKeyResolver(resolver);
        //  would blow up if it were ever looked up
        store.setKeyResolverBeanName("notRegistered");
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertSame(resolver, store.getKeyResolver());
    }

    /**
     * An empty {@code keyResolverBeanName} must not trigger a lookup either.
     */
    @Test
    public void testEmptyKeyResolverBeanName() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanName("");
        store.setApplicationContext(context());
        Assertions.assertThrows(org.struct.spring.exceptions.NoSuchKeyResolverException.class,
                store::afterPropertiesSet);
    }

    /**
     * The container knows no bean of that type - the resolver is instantiated directly instead.
     */
    @Test
    public void testEmptyBeansOfTypeFallsBackToNewInstance() throws Exception {
        ApplicationContext ctx = context();
        doReturn(java.util.Collections.emptyMap()).when(ctx).getBeansOfType(IdResolver.class);

        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanClass(IdResolver.class);
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertInstanceOf(IdResolver.class, store.getKeyResolver());
    }

    /**
     * The bare {@code StructKeyResolver} interface is not a usable resolver: it is skipped both as
     * a container lookup key and as a class to instantiate.
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testBareKeyResolverInterfaceIsSkipped() {
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        store.setKeyResolverBeanClass((Class) org.struct.store.StructKeyResolver.class);
        store.setApplicationContext(context());
        Assertions.assertThrows(org.struct.spring.exceptions.NoSuchKeyResolverException.class,
                store::afterPropertiesSet);
    }

    /**
     * An already resolved key resolver short circuits the {@code beanClass} lookup as well.
     */
    @Test
    public void testExplicitKeyResolverSkipsBeanClassLookup() throws Exception {
        ApplicationContext ctx = context();
        MapStructStore<Integer, String> store = new MapStructStore<>(String.class);
        org.struct.store.StructKeyResolver<Integer, String> resolver = new IdResolver();
        store.setKeyResolver(resolver);
        store.setKeyResolverBeanClass(IdResolver.class);
        store.setApplicationContext(ctx);
        store.afterPropertiesSet();
        Assertions.assertSame(resolver, store.getKeyResolver());
    }

    static class IdResolver implements org.struct.store.StructKeyResolver<Integer, String> {
        @Override
        public Integer resolve(String bean) {
            return bean.hashCode();
        }
    }
}
