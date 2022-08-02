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

package org.struct.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.exception.ServiceNotFoundException;
import org.struct.util.AnnotationUtils;
import org.struct.util.Reflects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced service loader.
 *
 * @param <S>
 * @see SPI
 */
final class EnhancedServiceLoader<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedServiceLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String STRUCT_DIRECTORY = "META-INF/struct/";

    /**
     * the service interface.
     */
    private final Class<S> service;

    private final Holder<List<ExtensionDefinition>> definitionsHolder = new Holder<>();

    private final ConcurrentHashMap<Class<?>, Holder<S>> extensionsMap = new ConcurrentHashMap<>();

    public EnhancedServiceLoader(Class<S> service) {
        this.service = service;
    }

    /**
     * Load service provider by service class and {@link ClassLoader}.
     *
     * @param loader the class loader
     * @return service instance.
     * @throws ServiceNotFoundException the service not found exception
     */
    public S load(ClassLoader loader) throws ServiceNotFoundException {
        return load(null, loader);
    }

    /**
     * Load service provider by service class and service alias.
     *
     * @param alias the service alias. {@link SPI#name()}
     * @return service instance.
     * @throws ServiceNotFoundException the service not found exception
     */
    public S load(String alias) throws ServiceNotFoundException {
        return load(alias, defaultClassLoader());
    }

    /**
     * Load service provider by service class and service alias.
     *
     * @param alias  the service alias. {@link SPI#name()}
     * @param loader the class loader
     * @return service instance.
     * @throws ServiceNotFoundException the service not found exception
     */
    public S load(String alias, ClassLoader loader) throws ServiceNotFoundException {
        return loadExtension(alias, loader, null);
    }

    /**
     * Load service provider by service alias and create instance with special args.
     *
     * @param alias the service alias. {@link SPI#name()}
     * @param args  the service constructor's args.
     * @return service instance.
     * @throws ServiceNotFoundException the service not found exception
     */
    public S load(String alias, Object[] args) throws ServiceNotFoundException {
        return loadExtension(alias, defaultClassLoader(), args);
    }

    /**
     * Load all service provider.
     *
     * @return service instance list.
     * @throws ServiceNotFoundException the service not found exception
     */
    public List<S> loadAll() {
        return loadAll(defaultClassLoader());
    }

    /**
     * Load all service provider with {@link ClassLoader}.
     *
     * @param loader the class loader
     * @return service instance list.
     * @throws ServiceNotFoundException the service not found exception
     */
    public List<S> loadAll(ClassLoader loader) {
        return loadAll(loader, null);
    }

    /**
     * Load all service provider.
     *
     * @param loader the class loader
     * @param args   the service constructor's args.
     * @return service instance list.
     * @throws ServiceNotFoundException the service not found exception
     */
    public List<S> loadAll(ClassLoader loader, Object[] args) {
        List<S> list = new ArrayList<>();
        for (ExtensionDefinition definition : this.lookupAllExtensionDefinition(loader)) {
            list.add(this.createExtensionInstance(definition, args));
        }
        return list;
    }

    /**
     * Get all the extension classes.
     *
     * @return all extension class
     */
    public List<Class> getAllExtensionClass() {
        return this.getAllExtensionClass(defaultClassLoader());
    }

    /**
     * Get all the extension classes with {@link ClassLoader}.
     *
     * @param loader the class loader
     * @return extension classes list.
     * @throws ServiceNotFoundException the service not found exception
     */
    public List<Class> getAllExtensionClass(ClassLoader loader) {
        return this.lookupAllExtensionDefinition(loader).stream().map(df -> (Class) df.clzOfService()).toList();
    }

    /**
     * Load service provider by service alias and create instance with special args.
     *
     * @param alias the service alias. {@link SPI#name()}
     * @param args  the service constructor's args.
     * @return service instance.
     * @throws ServiceNotFoundException the service not found exception
     */
    private S loadExtension(String alias, ClassLoader loader, Object[] args) {
        try {
            List<ExtensionDefinition> definitions = this.lookupAllExtensionDefinition(loader);
            ExtensionDefinition definition = null;
            if (!definitions.isEmpty()) {
                if (null == alias || alias.length() == 0) {
                    definition = definitions.get(definitions.size() - 1);
                } else {
                    for (int i = definitions.size() - 1; i >= 0; i--) {
                        ExtensionDefinition ed = definitions.get(i);
                        if (Objects.equals(ed.service(), alias)) {
                            definition = ed;
                            break;
                        }
                    }
                }
            }
            if (null == definition)
                throw new ServiceNotFoundException("not found service provider for : " + service.getName() + "[" + alias + "] and classloader : " + loader);

            S obj = this.createExtensionInstance(definition, args);
            LOGGER.info("load " + service.getSimpleName() + "[" + alias + "] extension by class[" + service.getName() + "] completed.");
            return obj;
        } catch (Throwable e) {
            if (e instanceof ServiceNotFoundException) {
                throw (ServiceNotFoundException) e;
            } else {
                throw new ServiceNotFoundException("not found service provider for : " + service.getName() + " caused by " + e.getMessage());
            }
        }
    }

    S createExtensionInstance(ExtensionDefinition definition, Object[] args) {
        Holder<S> holder = extensionsMap.computeIfAbsent(definition.clzOfService(), c -> new Holder<>());
        if (null == holder.get()) {
            synchronized (this) {
                if (null == holder.get()) {
                    try {
                        holder.instance = (S) Reflects.newInstance(definition.clzOfService(), args);
                    } catch (Throwable throwable) {
                        throw new ServiceNotFoundException("Instance extension:" + definition + " failure. args:" + Arrays.toString(args), throwable);
                    }
                }
            }
        }
        return holder.get();
    }

    List<ExtensionDefinition> lookupAllExtensionDefinition(ClassLoader loader) {
        if (null == definitionsHolder.get()) {
            synchronized (definitionsHolder) {
                if (null == definitionsHolder.get()) {
                    List<ExtensionDefinition> extensions = new ArrayList<>();
                    try {
                        handleDefinitionFile(SERVICES_DIRECTORY, loader, extensions);
                        handleDefinitionFile(STRUCT_DIRECTORY, loader, extensions);
                    } catch (IOException e) {
                        throw new ServiceNotFoundException(e);
                    }
                    if (!extensions.isEmpty()) {
                        Collections.sort(extensions);
                    }
                    definitionsHolder.instance = extensions;
                }
            }
        }
        return definitionsHolder.get();
    }

    void handleDefinitionFile(String dir, ClassLoader classLoader, List<ExtensionDefinition> output) throws IOException {
        String fileName = dir + this.service.getName();
        Enumeration<URL> urls = classLoader != null
                ? classLoader.getResources(fileName)
                : ClassLoader.getSystemResources(fileName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String line = null;
                while (null != (line = reader.readLine())) {
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            output.add(this.createExtensionDefinition(line, classLoader));
                        } catch (LinkageError | ClassNotFoundException e) {
                            LOGGER.warn("load [{}] class failure. {}", line, e.getMessage());
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("handle extension definition file error.", e);
            }
        }
    }

    ExtensionDefinition createExtensionDefinition(String clzName, ClassLoader loader) throws ClassNotFoundException {
        Class<?> clzOfService = Class.forName(clzName, true, loader);
        String name = null;
        int order = 0;
        SPI anno = AnnotationUtils.findAnnotation(SPI.class, clzOfService);
        if (anno != null) {
            name = anno.name();
            order = anno.order();
        }
        return new ExtensionDefinition(name, clzOfService, order);
    }

    ClassLoader defaultClassLoader() {
        return EnhancedServiceLoader.class.getClassLoader();
    }

    class Holder<V> {

        volatile V instance;

        V get() {
            return instance;
        }

    }
}
