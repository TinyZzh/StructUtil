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