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

package org.struct.spring.boot.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.struct.core.StructConfig;
import org.struct.spring.boot.autoconfigure.StructAutoConfiguration.AutoConfiguredMapperScannerRegistrar;
import org.struct.spring.support.StructStoreConfig;

import org.struct.bootfixture.BootSheetBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
@SpringBootTest(classes = {StructAutoConfiguration.class})
class StructAutoConfigurationTest {

    @Test
    public void testStructConfig() {
        StructAutoConfiguration configuration = new StructAutoConfiguration();
        StructProperties properties = new StructProperties();
        ArrayConverterProperties arrayConverterProperties = new ArrayConverterProperties();
        properties.setArrayConverter(arrayConverterProperties);
        StructConfig config = configuration.structConfig(properties);

        Assertions.assertEquals(config.isStructRequiredDefault(), properties.isStructRequiredDefault());
        Assertions.assertEquals(config.isIgnoreEmptyRow(), properties.isIgnoreEmptyRow());
    }

    /**
     * The array converter is only reconfigured when the properties actually carry one.
     */
    @Test
    public void testStructConfigWithoutArrayConverter() {
        StructAutoConfiguration configuration = new StructAutoConfiguration();
        StructProperties properties = new StructProperties();
        //  no array converter at all
        Assertions.assertNotNull(configuration.structConfig(properties));

        //  a converter that is not an ArrayConverter is left alone
        properties.setArrayConverter(new ArrayConverterProperties());
        Assertions.assertNotNull(configuration.structConfig(properties));
    }

    /**
     * The watcher's workspace must exist and be a directory.
     */
    @Test
    public void testFileWatcherServiceWorkspace() throws Exception {
        StructAutoConfiguration configuration = new StructAutoConfiguration();

        //  a plain file is not a valid workspace
        File file = File.createTempFile("struct-ws", ".tmp");
        try {
            StructStoreConfig config = new StructStoreConfig();
            config.setWorkspace(file.getAbsolutePath());
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> configuration.fileWatcherService(config, new ArrayList<>()));
        } finally {
            Assertions.assertTrue(file.delete() || !file.exists());
        }

        //  a missing directory is created on the fly
        File dir = new File(System.getProperty("java.io.tmpdir"), "struct-ws-" + System.nanoTime());
        try {
            StructStoreConfig config = new StructStoreConfig();
            config.setWorkspace(dir.getAbsolutePath());
            Assertions.assertNotNull(configuration.fileWatcherService(config, new ArrayList<>()));
            Assertions.assertTrue(dir.isDirectory());
        } finally {
            deleteRecursively(dir);
        }

        //  a parent that doesn't exist can not be created
        File impossible = new File(dir, "missing-parent/sub");
        StructStoreConfig badConfig = new StructStoreConfig();
        badConfig.setWorkspace(impossible.getAbsolutePath());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> configuration.fileWatcherService(badConfig, new ArrayList<>()));
    }

    /**
     * A real store is wired up with a reload hook per data file - the watcher has to walk the
     * resolved file names.
     */
    @Test
    public void testFileWatcherServiceRegistersHooks() throws Exception {
        StructAutoConfiguration configuration = new StructAutoConfiguration();
        File dir = new File(System.getProperty("java.io.tmpdir"), "struct-ws-" + System.nanoTime());
        try {
            StructStoreConfig config = new StructStoreConfig();
            config.setWorkspace(dir.getAbsolutePath());

            org.struct.store.MapStructStore<Integer, BootSheetBean> store =
                    new org.struct.store.MapStructStore<>(BootSheetBean.class);
            store.setKeyResolver(b -> b.key);

            Assertions.assertNotNull(configuration.fileWatcherService(config, new ArrayList<>(List.of(store))));
        } finally {
            deleteRecursively(dir);
        }
    }

    /**
     * Without a registered auto configuration package the scanning is disabled instead of failing.
     */
    @Test
    public void testRegistrarWithoutAutoConfigurationPackage() {
        AutoConfiguredMapperScannerRegistrar registrar = new AutoConfiguredMapperScannerRegistrar();
        BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
        //  AutoConfigurationPackages.has() resolves the bean by name+type, an unstubbed lookup
        //  yields null -> "has" is false.
        registrar.setBeanFactory(beanFactory);
        //  no resource loader at all
        registrar.registerBeanDefinitions(null, Mockito.mock(BeanDefinitionRegistry.class));
    }

    /**
     * A real bean factory that has the auto configuration packages registered: the scanner must run
     * and pick the store up.
     * <p>
     * NOTE: spring boot 4 resolves {@code AutoConfigurationPackages} through
     * {@code getBean(String, Class)} - stubbing only the single argument {@code getBean(String)}
     * leaves it null and blows up, so a real registry is used here.
     */
    @Test
    public void testAutoConfiguredMapperScannerRegistrar() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        AutoConfigurationPackages.register(registry, "org.struct.bootfixture");

        AutoConfiguredMapperScannerRegistrar registrar = new AutoConfiguredMapperScannerRegistrar();
        registrar.setBeanFactory(registry);
        registrar.setResourceLoader(new DefaultResourceLoader());

        registrar.registerBeanDefinitions(null, registry);

        Assertions.assertTrue(registry.getBeanDefinitionCount() > 0,
                "the scanner should have registered the stores found in the auto configuration package");
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (null != children) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}