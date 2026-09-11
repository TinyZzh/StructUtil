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

package org.struct.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.core.handler.CsvStructHandler;
import org.struct.core.handler.ExcelUMStructHandler;
import org.struct.core.handler.JsonStructHandler;
import org.struct.core.handler.StructHandler;
import org.struct.core.handler.XlsEventStructHandler;
import org.struct.core.handler.XlsxSaxStructHandler;
import org.struct.exception.ServiceNotFoundException;

import java.util.List;

public class EnhancedServiceLoaderTest {

    @Test
    public void testLoad() {
        ServiceLoader serviceLoader = new ServiceLoader();
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        Assertions.assertNotNull(loader.load(ServiceLoader.class.getClassLoader()));
        Assertions.assertEquals(CsvStructHandler.class, loader.load("csv").getClass());
        Assertions.assertEquals(CsvStructHandler.class, loader.load("csv", new Object[0]).getClass());
        Assertions.assertEquals(CsvStructHandler.class, loader.load("csv", ServiceLoader.class.getClassLoader()).getClass());
        Assertions.assertThrows(ServiceNotFoundException.class, () -> loader.load("csv-unknown"));
    }

    @Test
    public void testAllDefault() {
        {
            EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
            List<StructHandler> handlers = loader.loadAll();
            Assertions.assertFalse(handlers.isEmpty());
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == ExcelUMStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == CsvStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == JsonStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsEventStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsxSaxStructHandler.class));
        }
        {
            EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
            List<StructHandler> handlers = loader.loadAll(EnhancedServiceLoader.class.getClassLoader(), new Object[0]);
            Assertions.assertFalse(handlers.isEmpty());
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == ExcelUMStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == CsvStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == JsonStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsEventStructHandler.class));
            Assertions.assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsxSaxStructHandler.class));
        }
        {
            EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
            List<Class> list = loader.getAllExtensionClass();
            Assertions.assertTrue(list.stream().anyMatch(h -> h == ExcelUMStructHandler.class));
            Assertions.assertTrue(list.stream().anyMatch(h -> h == CsvStructHandler.class));
            Assertions.assertTrue(list.stream().anyMatch(h -> h == JsonStructHandler.class));
            Assertions.assertTrue(list.stream().anyMatch(h -> h == XlsEventStructHandler.class));
            Assertions.assertTrue(list.stream().anyMatch(h -> h == XlsxSaxStructHandler.class));
        }
    }

    /**
     * An interface nobody implements resolves to an empty definition list.
     */
    @Test
    public void testNoProvider() {
        EnhancedServiceLoader<TrulyEmptyService> loader = new EnhancedServiceLoader<>(TrulyEmptyService.class);
        Assertions.assertTrue(loader.lookupAllExtensionDefinition(defaultLoader()).isEmpty());
        Assertions.assertThrows(ServiceNotFoundException.class, () -> loader.load("any"));
        Assertions.assertThrows(ServiceNotFoundException.class, () -> loader.load(""));
        Assertions.assertThrows(ServiceNotFoundException.class, () -> loader.load((String) null));
    }

    /**
     * An interface with no definition file at all.
     */
    public interface TrulyEmptyService {
    }

    /**
     * A blank alias falls back to the highest order provider.
     */
    @Test
    public void testBlankAliasFallsBackToLast() {
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        Assertions.assertNotNull(loader.load(""));
        Assertions.assertNotNull(loader.load((String) null));
    }

    /**
     * A provider whose class can't be instantiated must surface as a
     * {@link ServiceNotFoundException}, not the raw cause.
     */
    @Test
    public void testCreateExtensionInstanceFailure() {
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        Assertions.assertThrows(ServiceNotFoundException.class, () ->
                loader.createExtensionInstance(new ExtensionDefinition("broken", NoProviderService.class, 0), new Object[0]));
    }

    /**
     * A provider whose constructor throws must be re-wrapped as a {@link ServiceNotFoundException}
     * by {@code getExtensionByAlias}'s {@code catch (Throwable)} (the non-ServiceNotFoundException branch).
     */
    @Test
    public void testGetExtensionByAliasInstantiationFailure() {
        EnhancedServiceLoader<BrokenService> loader = new EnhancedServiceLoader<>(BrokenService.class);
        Assertions.assertThrows(ServiceNotFoundException.class, () -> loader.load("broken"));
    }

    /**
     * The {@code @SPI} annotation is optional - a plain implementation just gets the defaults.
     */
    @Test
    public void testCreateExtensionDefinitionWithoutSpi() throws Exception {
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        ExtensionDefinition ed = loader.createExtensionDefinition(CsvStructHandler.class.getName(), defaultLoader());
        Assertions.assertEquals(CsvStructHandler.class, ed.clzOfService());
    }

    /**
     * A null class loader falls back to the system one, and blank / broken lines are skipped.
     */
    @Test
    public void testHandleDefinitionFile() throws Exception {
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        java.util.List<ExtensionDefinition> out = new java.util.ArrayList<>();
        //  a directory that simply holds nothing for this service
        loader.handleDefinitionFile("META-INF/struct/", null, out);
        Assertions.assertTrue(out.isEmpty());
    }

    /**
     * A definition file with blank lines and an entry that carries no {@code @SPI} - both are
     * handled without failing.
     */
    @Test
    public void testHandleDefinitionFileWithBlankAndPlainEntries() throws Exception {
        //  NoProviderService has a definition file in the test resources, containing blank lines
        //  and a plain implementation without @SPI.
        EnhancedServiceLoader<NoProviderService> loader = new EnhancedServiceLoader<>(NoProviderService.class);
        java.util.List<ExtensionDefinition> out = loader.lookupAllExtensionDefinition(defaultLoader());
        Assertions.assertFalse(out.isEmpty());
        //  the plain implementation gets the default name / order.
        Assertions.assertTrue(out.stream().allMatch(ed -> null == ed.service() || ed.service().isEmpty()));
        //  and it is loadable
        Assertions.assertNotNull(loader.load(""));
    }

    /**
     * The double checked lazy initialization - many threads racing on the same loader must each get
     * the very same instance.
     */
    @Test
    public void testConcurrentLoad() throws Exception {
        EnhancedServiceLoader<StructHandler> loader = new EnhancedServiceLoader<>(StructHandler.class);
        int n = 8;
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.Set<Object> instances = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException ignored) {
                }
                instances.add(loader.load("csv"));
            });
            threads[i].start();
        }
        start.countDown();
        for (Thread t : threads) {
            t.join(5_000L);
        }
        Assertions.assertEquals(1, instances.size(), "the extension must be created exactly once");
    }

    private static ClassLoader defaultLoader() {
        return EnhancedServiceLoaderTest.class.getClassLoader();
    }

    public interface NoProviderService {
    }

    /**
     * A provider that carries no {@code @SPI} annotation at all.
     */
    public static class PlainNoProviderService implements NoProviderService {
    }

    /**
     * A service whose only provider blows up inside its constructor, so that
     * {@code getExtensionByAlias}'s {@code catch (Throwable)} must wrap the raw cause.
     */
    public interface BrokenService {
    }

    public static class BrokenProvider implements BrokenService {
        public BrokenProvider() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    public void testAllClassLoader() {
        List<StructHandler> handlers = ServiceLoader.loadAll(StructHandler.class);
        Assertions.assertFalse(handlers.isEmpty());
        Assertions. assertTrue(handlers.stream().anyMatch(h -> h.getClass() == ExcelUMStructHandler.class));
        Assertions. assertTrue(handlers.stream().anyMatch(h -> h.getClass() == CsvStructHandler.class));
        Assertions. assertTrue(handlers.stream().anyMatch(h -> h.getClass() == JsonStructHandler.class));
        Assertions. assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsEventStructHandler.class));
        Assertions. assertTrue(handlers.stream().anyMatch(h -> h.getClass() == XlsxSaxStructHandler.class));
    }



}