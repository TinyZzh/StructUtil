/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.struct.spring.annotation.StructScan;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author TinyZ.
 * @date 2020-10-30.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StructScannerRegistrarTest.class)
@ComponentScan(basePackages = "org.struct.spring.support")
@Configuration
@StructScan(basePackages = "org.struct.spring.support")
class AbstractStructStoreTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    StructStoreConfig config() {
        StructStoreConfig config = new StructStoreConfig();
        config.setLazyLoad(false);
        return config;
    }

    @Test
    public void test2() throws Exception {
        AbstractStructStore<Long, A> store = new AbstractStructStore<>(A.class) {
            @Override
            public void initialize() {

            }

            @Override
            public void dispose() {

            }

            @Override
            public List<A> getAll() {
                return null;
            }

            @Override
            public A get(Long key) {
                return null;
            }

            @Override
            public List<A> lookup(Predicate<A> filter) {
                return null;
            }
        };
        store.setApplicationContext(applicationContext);
        store.afterPropertiesSet();
        Assertions.assertEquals(A.class, store.clzOfBean());
        Assertions.assertEquals(A.class, store.getClzOfBean());
        Assertions.assertEquals(0, store.size());
        Assertions.assertEquals(A.class.getSimpleName() + StructStore.class.getSimpleName(), store.identify());
        Assertions.assertFalse(store.isInitialized());
        store.destroy();
        store.tryGet(1L);
        store.getOrDefault(1L, null);
        store.lookup(1L, 0L);
        Assertions.assertTrue(store.casStatusInit());
        Assertions.assertTrue(store.casStatusDone());
        store.waitForDone();
        store.reload();
        store.casStatusReset();
        store.reload();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    static class A {

    }

}