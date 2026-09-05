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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.spring.annotation.StructScan;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * @author TinyZ.
 * @date 2020-10-13.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StructScannerRegistrarTest.class)
@ComponentScan(basePackages = "org.struct.spring.support")
@Configuration
@StructScan(basePackages = "org.struct.spring.support")
class ListStructStoreTest {

    @Mock
    private StructStoreConfig config;


    @Test
    public void test() {
        ListStructStore<String> lss = spy(new ListStructStore<>(String.class));
        doReturn(Collections.singletonList("xx")).when(lss).loadStructData();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            lss.get("xx");
        });
        lss.initialize();
        System.out.println(lss);
        Assertions.assertEquals(Collections.singletonList("xx"), lss.lookup(x -> x.equals("xx")));
        Assertions.assertEquals(1, lss.getAll().size());
        Assertions.assertEquals("xx", lss.getAll().get(0));
    }

    @Test
    public void test2() {
        ListStructStore<String> lss = spy(new ListStructStore<>(String.class));
        doReturn(Collections.singletonList("xx")).when(lss).loadStructData();
        lss.dispose();
    }

    @Test
    public void testInitSuc() {
        ListStructStore<Object> store = spy(new ListStructStore<>(Object.class));
        doReturn(new ArrayList<>()).when(store).loadStructData();
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    public void testInitFailure() {
        ListStructStore<Object> store = spy(new ListStructStore<>(Object.class));
        doReturn(new ArrayList<>()).when(store).loadStructData();
        store.casStatusInit();
        store.casStatusDone();
        Options options = new Options();
        options.setWaitForInit(true);
        store.setOptions(options);
        store.initialize();
        Assertions.assertTrue(store.isInitialized());
    }

    /**
     * The no-arg constructor is only meant for the spring bean definition, but it
     * must not blow up.
     */
    @Test
    public void testNoArgConstructor() {
        ListStructStore<String> store = new ListStructStore<>();
        Assertions.assertNull(store.clzOfBean());
        //  the key type parameter can only be bound by the spring bean definition
        Assertions.assertTrue(store.getAll().isEmpty());
    }

    /**
     * The real {@code loadStructData()} implementation (the other tests all mock it
     * out) must read the configured workspace.
     */
    @Test
    public void testLoadStructDataFromWorkspace() {
        Options options = new Options();
        options.setWorkspace("classpath:/org/struct/spring/support/");
        ListStructStore<StoreBean> store = new ListStructStore<>(StoreBean.class);
        store.setOptions(options);

        store.initialize();

        Assertions.assertTrue(store.isInitialized());
        Assertions.assertEquals(3, store.size());
        Assertions.assertEquals(3, store.getAll().size());
        Assertions.assertEquals(1, store.getAll().get(0).key);
        Assertions.assertEquals("11", store.getAll().get(0).val);

        //  the returned list is immutable
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> store.getAll().add(new StoreBean()));

        store.dispose();
        Assertions.assertTrue(store.getAll().isEmpty());
    }

    /**
     * A failing load is only logged, the store still ends up "initialized".
     * <p>
     * NOTE: this is a known P2 issue - a configuration error lets the application
     * start with empty data instead of failing fast.
     */
    @Test
    public void testLoadStructDataFailureIsLoggedOnly() {
        Options options = new Options();
        options.setWorkspace("classpath:/");
        ListStructStore<StoreBean> store = spy(new ListStructStore<>(StoreBean.class));
        store.setOptions(options);
        doThrow(new IllegalStateException("boom")).when(store).loadStructData();

        store.initialize();
        Assertions.assertTrue(store.isInitialized());
        Assertions.assertEquals(0, store.size());
        Assertions.assertTrue(store.getAll().isEmpty());
    }

    @StructSheet(fileName = "tpl_list_store.json")
    public static class StoreBean {
        public int key;
        public String val;
    }
}