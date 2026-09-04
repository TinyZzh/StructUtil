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

package org.struct.core;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructSheet;
import org.struct.core.factory.StructFactory;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.handler.StructHandler;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.util.AnnotationUtils;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <T> the target java bean class.
 */
public class StructWorker<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructWorker.class);
    /**
     * the working space path.
     */
    protected String workspace;
    /**
     *
     */
    protected final Class<T> clzOfStruct;
    /**
     * Struct configuration.
     */
    protected StructConfig config;

    protected StructDescriptor descriptor;

    protected StructFactory structFactory;
    /**
     * field namespace url - unique key - ref field's value
     */
    protected final Map<String, Map<Object, Object>> tempRefFieldValueMap;

    public StructWorker(String workspace, Class<T> clzOfStruct) {
        this(workspace, clzOfStruct, new StructDescriptor(clzOfStruct), new ConcurrentHashMap<>());
    }

    /**
     * Load a protobuf binary file directly into a protoc-generated {@link Message} subclass
     * without annotating that class with {@link StructSheet} (protoc regenerates it on every
     * build, so you cannot add annotations to it). The data file name is supplied explicitly
     * because it cannot be inferred from the generated class; the protobuf message name
     * defaults to the class's simple name.
     *
     * <p>This is path A of {@code ProtobufStructHandler}: the parsed message IS the bean,
     * no intermediate {@code StructImpl} is involved.
     *
     * @param workspace  working directory for relative file paths.
     * @param clzOfStruct a {@link Message} subclass produced by protoc (must NOT be @StructSheet-annotated).
     * @param fileName    the protobuf binary file name, e.g. {@code "item.bin"}.
     * @throws IllegalArgumentException if the class is neither @StructSheet-annotated nor a Message.
     */
    public StructWorker(String workspace, Class<T> clzOfStruct, String fileName) {
        this(workspace, clzOfStruct, resolveProtobufDescriptor(clzOfStruct, fileName), new ConcurrentHashMap<>());
    }

    private static <T> StructDescriptor resolveProtobufDescriptor(Class<T> clzOfStruct, String fileName) {
        StructSheet anno = AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct);
        if (anno != null) {
            //  Annotated class: prefers the annotation (fileName argument ignored for consistency
            //  with the primary constructor). Callers wanting annotation-driven loading should use it.
            return new StructDescriptor(anno);
        }
        if (!Message.class.isAssignableFrom(clzOfStruct)) {
            throw new IllegalArgumentException(
                    "clazz:" + clzOfStruct.getName() + " must be annotated by @StructSheet, "
                            + "or be a protobuf Message subclass when a fileName is supplied");
        }
        //  Zero-annotation path A: message name defaults to the simple class name;
        //  matcher/filter mirror the @StructSheet defaults (placeholders).
        return new StructDescriptor(fileName, clzOfStruct.getSimpleName(), 0, 0, WorkerMatcher.class, StructBeanFilter.class);
    }

    public StructWorker(String workspace, Class<T> clzOfStruct, StructDescriptor descriptor, Map<String, Map<Object, Object>> tempRefFieldValueMap) {
        this.workspace = workspace;
        this.clzOfStruct = clzOfStruct;
        this.descriptor = descriptor;
        this.tempRefFieldValueMap = tempRefFieldValueMap;
    }

    /// <editor-fold desc="   Protected Methods    "  defaultstate="collapsed">

    public void checkStructFactory() {
        StructFactory factory = this.structFactory;
        if (factory == null) {
            factory = WorkerUtil.structFactory(this.clzOfStruct, this);
            factory.parseStruct();
            this.structFactory = factory;
        }
    }

    public void handleReferenceFieldValue(StructFactory structFactory, SingleFieldDescriptor descriptor) throws RuntimeException {
        if (descriptor == null || !descriptor.isReferenceField() || descriptor.isBasicTypeCollection()) {
            return;
        }
        String clzFieldUrl = descriptor.getRefFieldUrl();
        if (tempRefFieldValueMap.containsKey(clzFieldUrl)) {
            LOGGER.debug("Struct circular references, clzFieldUrl:{}, prev:{}", clzFieldUrl, descriptor.getName());
            if (!StructConfig.INSTANCE.isAllowCircularReferences())
                throw new RuntimeException("loop dependent with key:" + clzFieldUrl + ", prev:" + descriptor.getName());
            return;
        }
        //  Mark this reference as "being resolved" BEFORE recursing.
        //  Without this placeholder the circular reference check above could never
        //  trigger: the sub worker re-enters this method with the very same key while
        //  the real value is only put into the map after the recursion returns,
        //  so a circular reference recursed until StackOverflowError.
        tempRefFieldValueMap.put(clzFieldUrl, Collections.emptyMap());
        Class<?> targetType = descriptor.getFieldType();
        if (descriptor.isAggregateField()) {
            targetType = descriptor.resolveAggregateWorkerType();
        }
        StructWorker<?> subWorker = WorkerUtil.newWorker(this.workspace, descriptor.getReference(), this.tempRefFieldValueMap);
        if (targetType.isArray()) {
            Map<Object, ?> map = subWorker.toListWithGroup(ArrayList::new, descriptor.getRefGroupBy());
            Map<Object, Object> collect = map.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof Collection)
                    .map(entry -> new Object[]{entry.getKey(), ((Collection) entry.getValue()).toArray()})
                    .collect(Collectors.toMap(o -> o[0], o -> o[1]));
            tempRefFieldValueMap.put(clzFieldUrl, Collections.unmodifiableMap(collect));
        } else if (Collection.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toListWithGroup(targetType, descriptor.getRefGroupBy());
            tempRefFieldValueMap.put(clzFieldUrl, Collections.unmodifiableMap(map));
        } else if (Map.class.isAssignableFrom(targetType)) {
            Map<Object, ?> map = subWorker.toMapWithGroup(targetType, descriptor.getRefUniqueKey(), descriptor.getRefGroupBy());
            tempRefFieldValueMap.put(clzFieldUrl, Collections.unmodifiableMap(map));
        } else {
            Map<Object, ?> map = subWorker.toMap(HashMap::new, descriptor.getRefUniqueKey());
            tempRefFieldValueMap.put(clzFieldUrl, Collections.unmodifiableMap(map));
        }
    }

    public Map<Object, Object> getRefFieldValuesMap(String fieldUrl) {
        return tempRefFieldValueMap.get(fieldUrl);
    }

    /// </editor-fold>

    /// <editor-fold desc=" Excel Convert Collection "  defaultstate="collapsed">

    public <C extends Collection<T>> C load(TypeRefFactory<C> factory) throws RuntimeException {
        return this.toList(factory);
    }

    public <C extends Collection<T>> C toList(TypeRefFactory<C> factory) throws RuntimeException {
        this.checkStructFactory();
        C list = factory.newInstance();
        handleDataFile(list::add);
        return list;
    }

    /**
     * @param groupFunc the function to generate map key.
     * @param <G>       the group by function.
     * @param <C>       the collection class
     * @return return a map. the map's key generate by #groupFunc
     */
    public <G, C extends Collection<T>> Map<G, C> toListWithGroup(TypeRefFactory<C> factory, Function<T, G> groupFunc) throws RuntimeException {
        this.checkStructFactory();
        Map<G, C> map = new HashMap<>();
        handleDataFile(obj -> {
            Collection<T> list = map.computeIfAbsent(groupFunc.apply(obj), objects -> factory.newInstance());
            list.add(obj);
        });
        return map;
    }

    public <C extends Collection<T>> Map<Object, C> toListWithGroup(TypeRefFactory<C> factory, String[] groupByKey) throws RuntimeException {
        return this.toListWithGroup(factory, obj -> structFactory.getFieldValuesArray(obj, groupByKey));
    }

    public Map<Object, Collection<T>> toListWithGroup(Class<?> clzOfCollection, String[] groupByKey) throws RuntimeException {
        return toListWithGroup(() -> {
            try {
                return WorkerUtil.newListOnly(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, groupByKey);
    }

    public <K, M extends Map<K, T>> M toMap(TypeRefFactory<M> factory, Function<T, K> func) throws RuntimeException {
        this.checkStructFactory();
        M map = factory.newInstance();
        handleDataFile(obj -> map.put(func.apply(obj), obj));
        return map;
    }

    public <M extends Map<Object, T>> M toMap(TypeRefFactory<M> factory, String[] uniqueKey) throws RuntimeException {
        return this.toMap(factory, obj -> structFactory.getFieldValuesArray(obj, uniqueKey));
    }

    public <K, G, M extends Map<K, T>> Map<G, M> toMapWithGroup(TypeRefFactory<M> factory, Function<T, K> keyFunc, Function<T, G> groupFunc) throws RuntimeException {
        this.checkStructFactory();
        Map<G, M> result = new HashMap<>();
        handleDataFile(obj -> {
            G groupBy = groupFunc.apply(obj);
            M map = result.computeIfAbsent(groupBy, objects -> factory.newInstance());
            map.put(keyFunc.apply(obj), obj);
        });
        return result;
    }

    public <M extends Map<Object, T>> Map<Object, M> toMapWithGroup(final TypeRefFactory<M> factory, final String[] uniqueKey, final String[] groupByKey) throws RuntimeException {
        return this.toMapWithGroup(factory, obj -> structFactory.getFieldValuesArray(obj, uniqueKey), obj -> structFactory.getFieldValuesArray(obj, groupByKey));
    }

    public Map<Object, Map<Object, T>> toMapWithGroup(Class<?> clzOfCollection, String[] uniqueKey, String[] groupByKey) throws RuntimeException {
        return toMapWithGroup(() -> {
            try {
                return WorkerUtil.newMap(clzOfCollection);
            } catch (Exception e) {
                throw new IllegalArgumentException("create newList failed. class:" + clzOfCollection);
            }
        }, uniqueKey, groupByKey);
    }

    /// </editor-fold>

    /**
     * Create a new {@link #clzOfStruct} instance.
     *
     * @param struct the struct data.
     * @return new instance.
     */
    public Optional<T> createInstance(StructImpl struct) {
        if (struct.isEmpty())
            return Optional.empty();
        return this.createInstance((Object) struct);
    }

    public Optional<T> createInstance(Object structImpl) {
        if (null == structImpl)
            return Optional.empty();
        return (Optional<T>) this.structFactory.newStructInstance(structImpl);
    }

    public StructDescriptor getDescriptor() {
        return descriptor;
    }

    public void handleDataFile(Consumer<T> cellHandler) {
        String filePath = WorkerUtil.resolveFilePath(this.workspace, this.descriptor.getFileName());
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }
        List<StructHandler> collected = WorkerUtil.lookupStructHandler(this.descriptor, file);
        for (StructHandler handler : collected) {
            try {
                handler.handle(this, this.clzOfStruct, wrapCellHandler(this.descriptor, cellHandler), file);
                return;
            } catch (Exception e) {
                //  try next handler.
                LOGGER.info("{} handle data file failure. please check the class of struct file and the matcher rules. file:{}",
                        handler.getClass().getName(), file.getName(), e);
            }
        }
        throw new IllegalArgumentException("unknown data file extension. file name:" + file.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Consumer<T> wrapCellHandler(StructDescriptor descriptor, Consumer<T> cellHandler) {
        Consumer<T> handler = cellHandler;
        Class<? extends StructBeanFilter> clzOfFilter = descriptor.getFilter();
        if (StructBeanFilter.class != clzOfFilter
                && !Modifier.isInterface(clzOfFilter.getModifiers())
                && !Modifier.isAbstract(clzOfFilter.getModifiers())
        ) {
            try {
                Constructor<? extends StructBeanFilter> constructor = clzOfFilter.getConstructor(Consumer.class);
                handler = constructor.newInstance(cellHandler);
            } catch (Exception e) {
                String msg = "wrap cell handler failure. struct:" + descriptor.getFileName() + ", sheet:" + descriptor.getSheetName();
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return handler;
    }
}
