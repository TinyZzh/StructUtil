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

package org.struct.util;

import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.factory.StructFactory;
import org.struct.core.factory.StructFactoryBean;
import org.struct.core.handler.StructHandler;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.spi.ServiceLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author TinyZ.
 * @version 2019.03.23
 */
public final class WorkerUtil {

    /**
     * {@link StructHandler}'s holder.
     */
    private static final Holder<List<StructHandler>> HANDLERS_HOLDER = new Holder<>(() -> ServiceLoader.loadAll(StructHandler.class));

    private static final Holder<List<StructFactoryBean>> FACTORY_BEAN_HOLDER = new Holder<>(() -> ServiceLoader.loadAll(StructFactoryBean.class));

    private WorkerUtil() {
        //  no-op
    }

    public static <T> StructWorker<T> newWorker(String rootPath, Class<T> clzOfBean) {
        return newWorker(rootPath, clzOfBean, new StructDescriptor(clzOfBean), new ConcurrentHashMap<>());
    }

    public static <T> StructWorker<T> newWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        return newWorker(rootPath, clzOfBean, new StructDescriptor(clzOfBean), refFieldValueMap);
    }

    public static <T> StructWorker<T> newWorker(String rootPath, Class<T> clzOfBean, StructDescriptor descriptor, Map<String, Map<Object, Object>> refFieldValueMap) {
        return new StructWorker<>(rootPath, clzOfBean, descriptor, refFieldValueMap);
    }

    public static List<StructHandler> lookupStructHandler(StructDescriptor descriptor, File file) {
        List<StructHandler> handlers = HANDLERS_HOLDER.get();
        Stream<StructHandler> stream = handlers.stream();
        if (WorkerMatcher.class != descriptor.getMatcher()) {
            stream = stream.filter(handler -> handler.matcher().getClass().isAssignableFrom(descriptor.getMatcher()));
        } else {
            stream = stream.filter(handler -> handler.matcher().matchFile(file));
        }
        return stream.sorted(Comparator.comparingInt(o -> o.matcher().order())).collect(Collectors.toList());
    }

    /**
     * Resolve struct file's path.
     * 1. classpath:   the path is class's path
     *
     * @param filepath file's path
     * @param fileName file Name.
     */
    public static String resolveFilePath(String filepath, String fileName) {
        if (filepath.startsWith("classpath:")) {
            String path = filepath.substring(filepath.indexOf(":") + 1) + "/" + fileName;
            URL resource = StructWorker.class.getResource(path);
            return resource == null
                    ? (filepath + (filepath.endsWith("/") ? "" : "/") + fileName)
                    : resource.getPath();
        } else if (filepath.startsWith("file:")) {
            return filepath.substring(filepath.indexOf(":") + 1) + "/" + fileName;
        } else {
            return filepath + "/" + fileName;
        }
    }

    public static <T> Collection<T> newListOnly(Class<?> clzOfList) throws Exception {
        Collection<T> list;
        if (!(Collection.class.isAssignableFrom(clzOfList))) {
            throw new IllegalArgumentException("class " + clzOfList + " is not Collection.");
        }
        if (clzOfList.isInterface()
                || Modifier.isInterface(clzOfList.getModifiers())
                || Modifier.isAbstract(clzOfList.getModifiers())) {
            if (Set.class.isAssignableFrom(clzOfList)) {
                list = new HashSet<>();
            } else {
                list = new ArrayList<>();
            }
        } else {
            Constructor<?> constructor = clzOfList.getConstructor();
            list = (Collection<T>) constructor.newInstance();
        }
        return list;
    }

    public static <T> Map<Object, T> newMap(Class<?> clzOfMap) throws Exception {
        Map<Object, T> map;
        if (clzOfMap == null
                || Modifier.isAbstract(clzOfMap.getModifiers())
                || Modifier.isInterface(clzOfMap.getModifiers())
        ) {
            map = new HashMap<>();
        } else {
            Constructor<?> constructor = clzOfMap.getConstructor();
            map = (Map<Object, T>) constructor.newInstance();
        }
        return map;
    }

    public static <T> StructFactory structFactory(Class<T> clzOfStruct, StructWorker<T> worker) {
        List<StructFactoryBean> beans = FACTORY_BEAN_HOLDER.get();
        for (StructFactoryBean factoryBean : beans) {
            try {
                return factoryBean.newInstance(clzOfStruct, worker);
            } catch (Exception e) {
                //  no-op
            }
        }
        throw new IllegalStateException("No such factory bean for struct:" + clzOfStruct);
    }

    static class Holder<T> {

        volatile T value;

        final Supplier<T> supplier;

        Holder(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        T get() {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        this.value = supplier.get();
                    }
                }
            }
            return this.value;
        }
    }
}
