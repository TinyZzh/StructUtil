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

package org.struct.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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
import java.util.Objects;
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
    private static final Holder<List<StructHandler>> HANDLERS_HOLDER = new Holder<>(() ->
            ServiceLoader.loadAll(StructHandler.class).stream().filter(Objects::nonNull).collect(Collectors.toList()));

    private static final Holder<List<StructFactoryBean>> FACTORY_BEAN_HOLDER = new Holder<>(() ->
            ServiceLoader.loadAll(StructFactoryBean.class).stream().filter(Objects::nonNull).collect(Collectors.toList()));

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

    private static <T, C extends Collection<T>> C newList(Class<C> clzOfList) throws Exception {
        C list;
        if (!(Collection.class.isAssignableFrom(clzOfList))) {
            throw new IllegalArgumentException("class " + clzOfList + " is not Collection.");
        }
        if (clzOfList.isInterface()
                || Modifier.isAbstract(clzOfList.getModifiers())
                || Modifier.isInterface(clzOfList.getModifiers())) {
            list = (C) new ArrayList<T>();
        } else {
            Constructor<C> constructor = clzOfList.getConstructor();
            list = constructor.newInstance();
        }
        return list;
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
                || clzOfMap.isInterface()
                || Modifier.isInterface(clzOfMap.getModifiers())
                || Modifier.isAbstract(clzOfMap.getModifiers())
        ) {
            map = new HashMap<>();
        } else {
            Constructor<?> constructor = clzOfMap.getConstructor();
            map = (Map<Object, T>) constructor.newInstance();
        }
        return map;
    }

    public static Object getExcelCellValue(CellType cellType, Object cell, FormulaEvaluator evaluator) throws Exception {
        switch (cellType) {
            case _NONE:
                return null;
            case BLANK:
                return "";
            case NUMERIC:
                Double numeric;
                if (cell instanceof Cell)
                    numeric = ((Cell) cell).getNumericCellValue();
                else if (cell instanceof CellValue)
                    numeric = ((CellValue) cell).getNumberValue();
                else
                    numeric = 0.0D;
                if (numeric == numeric.longValue()) {
                    if (numeric.longValue() > Integer.MAX_VALUE) {
                        return numeric.longValue();
                    } else
                        return numeric.intValue();
                } else {
                    return numeric;
                }
            case STRING:
                if (cell instanceof Cell) {
                    return ((Cell) cell).getStringCellValue();
                } else if (cell instanceof CellValue) {
                    return ((CellValue) cell).getStringValue();
                } else {
                    return "";
                }
            case FORMULA:
                if (cell instanceof Cell) {
                    CellValue val = evaluator.evaluate((Cell) cell);
                    return getExcelCellValue(val.getCellType(), cell, evaluator);
                } else {
                    return null;
                }
            case BOOLEAN:
                if (cell instanceof Cell)
                    return ((Cell) cell).getBooleanCellValue();
                else if (cell instanceof CellValue)
                    return ((CellValue) cell).getBooleanValue();
                else
                    return false;
            default:
                throw new Exception("Unknown Cell type");
        }
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
