/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ServiceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String STRUCT_DIRECTORY = "META-INF/struct/";

    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap<Class, List<Class>> providers = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Class<?>> EXTENSION_NAMES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * Specify classLoader to load the service provider
     *
     * @param <S>     the type parameter
     * @param service the service
     * @param loader  the loader
     * @return s s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service, ClassLoader loader) throws ServiceNotFoundException {
        return loadFile(service, null, loader);
    }

    /**
     * load service provider
     *
     * @param <S>     the type parameter
     * @param service the service
     * @return s s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service) throws ServiceNotFoundException {
        return loadFile(service, null, findClassLoader());
    }

    /**
     * load service provider
     *
     * @param <S>          the type parameter
     * @param service      the service
     * @param activateName the activate name
     * @return s s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service, String activateName) throws ServiceNotFoundException {
        return loadFile(service, activateName, findClassLoader());
    }

    /**
     * Specify classLoader to load the service provider
     *
     * @param <S>          the type parameter
     * @param service      the service
     * @param activateName the activate name
     * @param loader       the loader
     * @return s s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service, String activateName, ClassLoader loader)
            throws ServiceNotFoundException {
        return loadFile(service, activateName, loader);
    }

    /**
     * Load s.
     *
     * @param <S>          the type parameter
     * @param service      the service
     * @param activateName the activate name
     * @param args         the args
     * @return the s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service, String activateName, Object[] args)
            throws ServiceNotFoundException {
        Class[] argsType = null;
        if (args != null && args.length > 0) {
            argsType = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argsType[i] = args[i].getClass();
            }
        }
        return loadFile(service, activateName, findClassLoader(), argsType, args);
    }

    /**
     * Load s.
     *
     * @param <S>          the type parameter
     * @param service      the service
     * @param activateName the activate name
     * @param argsType     the args type
     * @param args         the args
     * @return the s
     * @throws ServiceNotFoundException the enhanced service not found exception
     */
    public static <S> S load(Class<S> service, String activateName, Class[] argsType, Object[] args)
            throws ServiceNotFoundException {
        return loadFile(service, activateName, findClassLoader(), argsType, args);
    }

    /**
     * get all implements
     *
     * @param <S>     the type parameter
     * @param service the service
     * @return list list
     */
    public static <S> List<S> loadAll(Class<S> service) {
        return loadAll(service, null, null);
    }

    /**
     * get all implements
     *
     * @param <S>      the type parameter
     * @param service  the service
     * @param argsType the args type
     * @param args     the args
     * @return list list
     */
    public static <S> List<S> loadAll(Class<S> service, Class[] argsType, Object[] args) {
        List<S> allInstances = new ArrayList<>();
        List<Class> allClazzs = getAllExtensionClass(service);
        if (allClazzs.isEmpty()) {
            return allInstances;
        }
        for (Class clazz : allClazzs) {
            try {
                allInstances.add(createInstance(service, clazz, argsType, args));
            } catch (Throwable t) {
                LOGGER.warn("Load @SPI:{} failure. clazz:{}, argsType:{}, args:{}", service.getSimpleName(), clazz.getName(), Arrays.toString(argsType), Arrays.toString(args), t);
            }
        }
        return allInstances;
    }

    /**
     * Get all the extension classes, follow {@linkplain SPI} defined and sort order
     *
     * @param <S>     the type parameter
     * @param service the service
     * @return all extension class
     */
    @SuppressWarnings("rawtypes")
    static <S> List<Class> getAllExtensionClass(Class<S> service) {
        return findAllExtensionClass(service, null, findClassLoader());
    }

    /**
     * Get all the extension classes, follow {@linkplain SPI} defined and sort order
     *
     * @param <S>     the type parameter
     * @param service the service
     * @param loader  the loader
     * @return all extension class
     */
    @SuppressWarnings("rawtypes")
    static <S> List<Class> getAllExtensionClass(Class<S> service, ClassLoader loader) {
        return findAllExtensionClass(service, null, loader);
    }

    private static <S> S loadFile(Class<S> service, String activateName, ClassLoader loader) {
        return loadFile(service, activateName, loader, null, null);
    }

    @SuppressWarnings("rawtypes")
    private static <S> S loadFile(Class<S> service, String activateName, ClassLoader loader, Class[] argTypes,
                                  Object[] args) {
        try {
            List<Class> extensions = providers.computeIfAbsent(service, (s) -> {
                return findAllExtensionClass(service, activateName, loader);
            });

            if (activateName.isEmpty()) {
                loadFile(service, STRUCT_DIRECTORY + activateName.toLowerCase() + "/", loader, extensions);

                List<Class> activateExtensions = new ArrayList<>();
                for (Class clz : extensions) {
                    SPI activate = (SPI) clz.getAnnotation(SPI.class);
                    if (activate != null && activateName.equalsIgnoreCase(activate.name())) {
                        activateExtensions.add(clz);
                    }
                }

                extensions = activateExtensions;
            }

            if (extensions.isEmpty()) {
                throw new ServiceNotFoundException(
                        "not found service provider for : " + service.getName() + "[" + activateName
                                + "] and classloader : " + loader);
            }
            Class<?> extension = extensions.get(extensions.size() - 1);
            LOGGER.info("load " + service.getSimpleName() + "[" + activateName + "] extension by class[" + extension
                    .getName() + "]");
            return createInstance(service, extension, argTypes, args);
        } catch (Throwable e) {
            if (e instanceof ServiceNotFoundException) {
                throw (ServiceNotFoundException) e;
            } else {
                throw new ServiceNotFoundException(
                        "not found service provider for : " + service.getName() + " caused by " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static <S> List<Class> findAllExtensionClass(Class<S> service, String activateName, ClassLoader loader) {
        List<Class> extensions = new ArrayList<>();
        try {
            loadFile(service, SERVICES_DIRECTORY, loader, extensions);
            loadFile(service, STRUCT_DIRECTORY, loader, extensions);
        } catch (IOException e) {
            throw new ServiceNotFoundException(e);
        }
        if (extensions.isEmpty()) {
            return extensions;
        }
        extensions.sort((c1, c2) -> {
            int o1 = 0;
            int o2 = 0;
            SPI a1 = (SPI) c1.getAnnotation(SPI.class);
            SPI a2 = (SPI) c2.getAnnotation(SPI.class);
            if (a1 != null) {
                o1 = a1.order();
            }
            if (a2 != null) {
                o2 = a2.order();
            }
            return Integer.compare(o1, o2);
        });

        return extensions;
    }

    @SuppressWarnings("rawtypes")
    private static void loadFile(Class<?> service, String dir, ClassLoader classLoader, List<Class> extensions)
            throws IOException {
        String fileName = dir + service.getName();
        Enumeration<URL> urls;
        if (classLoader != null) {
            urls = classLoader.getResources(fileName);
        } else {
            urls = ClassLoader.getSystemResources(fileName);
        }

        if (urls != null) {
            while (urls.hasMoreElements()) {
                java.net.URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        final int ci = line.indexOf('#');
                        if (ci >= 0) {
                            line = line.substring(0, ci);
                        }
                        line = line.trim();
                        if (line.length() > 0) {
                            try {
                                extensions.add(Class.forName(line, true, classLoader));
                            } catch (LinkageError | ClassNotFoundException e) {
                                LOGGER.warn("load [{}] class fail. {}", line, e.getMessage());
                            }
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }

    /**
     * init instance
     *
     * @param <S>       the type parameter
     * @param service   the service
     * @param implClazz the impl clazz
     * @param argTypes  the arg types
     * @param args      the args
     * @return service implement
     */
    protected static <S> S createInstance(Class<S> service, Class implClazz, Class[] argTypes, Object[] args) {
        Object ins = EXTENSION_INSTANCES.computeIfAbsent(implClazz, ic -> {
            try {
                SPI annotation = AnnotationUtils.findAnnotation(SPI.class, implClazz);
                String extensionName = annotation != null ? annotation.name() : implClazz.getSimpleName();
                EXTENSION_NAMES.putIfAbsent(extensionName, implClazz);

                S s = null;
                if (argTypes != null && args != null) {
                    // Constructor with arguments
                    Constructor<S> constructor = implClazz.getDeclaredConstructor(argTypes);
                    s = service.cast(constructor.newInstance(args));
                } else {
                    // default Constructor
                    s = service.cast(implClazz.newInstance());
                }
                return s;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return (S) ins;
    }

    /**
     * Cannot use TCCL, in the pandora container will cause the class in the plugin not to be loaded
     */
    private static ClassLoader findClassLoader() {
        return ServiceLoader.class.getClassLoader();
    }
}
