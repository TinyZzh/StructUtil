package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
public class StructMapper<K, B> implements StructStore<K, B> {

    private static final int NORMAL = 0;
    private static final int INITIALIZING = 0;
    private static final int INITIALIZED = 0;
    private static final AtomicIntegerFieldUpdater<StructMapper> STATUS_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(StructMapper.class, "status");

    private static final Logger LOGGER = LoggerFactory.getLogger(StructMapper.class);

    private String identify;
    private Class<B> clzOfBean;
    private StructKeyResolver<K, B> resolver;
    private final ConcurrentHashMap<K, B> cached = new ConcurrentHashMap<>();
    private volatile int status;

    public StructMapper() {
    }

    public StructMapper(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public String identify() {
        return this.identify;
    }

    @Override
    public Class<B> clzOfBean() {
        return this.clzOfBean;
    }

    public Class<B> getClzOfBean() {
        return clzOfBean;
    }

    public void setClzOfBean(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public void initialize() {
        if (!STATUS_UPDATER.compareAndSet(this, NORMAL, INITIALIZING)) {
            return;
        }
        try {
            ConcurrentMap<K, B> collected = WorkerUtil.newWorker("", this.clzOfBean)
                    .toList(ArrayList::new)
                    .stream()
                    .collect(Collectors.toConcurrentMap(b -> resolver.resolve(b), b -> b));
            cached.putAll(collected);
        } catch (Exception e) {
            LOGGER.error("initialize struct failure. identify:{}, clz:{}", this.identify, this.clzOfBean, e);
        } finally {
            STATUS_UPDATER.compareAndSet(this, INITIALIZING, INITIALIZED);
        }
    }

    @Override
    public boolean isInitialized() {
        return INITIALIZED == STATUS_UPDATER.get(this);
    }

    @Override
    public void reload() {
        if (!isInitialized())
            return;
        this.dispose();
        this.initialize();
    }

    @Override
    public void dispose() {
        //  reset status.
        STATUS_UPDATER.set(this, NORMAL);
        this.cached.clear();
    }

    @Override
    public List<B> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(this.cached.values()));
    }

    @Override
    public B get(K key) {
        return this.cached.get(key);
    }

    @Override
    public List<B> lookup(K... keys) {
        return Stream.of(keys).map(this::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<B> lookup(Predicate<B> filter) {
        return this.cached.values().stream().filter(filter).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void addShutdownHook(Consumer<List<B>> consumer) {
        // Runtime.getRuntime().addShutdownHook();
        consumer.accept(getAll());
    }
}
