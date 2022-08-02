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
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;
import org.struct.spring.annotation.StructScan;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.doReturn;
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


}