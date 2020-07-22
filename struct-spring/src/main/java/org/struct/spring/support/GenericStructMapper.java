package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class GenericStructMapper<K, B> implements StructStore<K, B> {

    private static final int NORMAL = 0;
    private static final int INITIALIZING = 0;
    private static final int INITIALIZED = 0;
    private static final AtomicIntegerFieldUpdater<GenericStructMapper> STATUS_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(GenericStructMapper.class, "status");

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericStructMapper.class);

    /**
     * the class of struct bean instances.
     *
     * @see StructConstant#CLZ_OF_BEAN
     */
    private Class<B> clzOfBean;
    /**
     * Struct util configuration.
     *
     * @see StructConstant#CONFIG
     */
    private StructConfig config;
    /**
     * {@link StructKeyResolver}'s bean name.
     *
     * @see StructConstant#KEY_RESOLVER_BEAN_NAME
     */
    private String keyResolverBeanName;
    private StructKeyResolver<K, B> resolver;
    /**
     * the cached struct data map.
     */
    private ConcurrentHashMap<K, B> cached = new ConcurrentHashMap<>();
    private volatile int status;

    public GenericStructMapper() {
        //  spring bean definition
    }

    public GenericStructMapper(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public String identify() {
        return this.clzOfBean.getSimpleName() + GenericStructMapper.class.getSimpleName();
    }

    @Override
    public Class<B> clzOfBean() {
        return this.clzOfBean;
    }

    public Class<B> getClzOfBean() {
        return clzOfBean;
    }

    public StructKeyResolver<K, B> getResolver() {
        return resolver;
    }

    public GenericStructMapper<K, B> setResolver(StructKeyResolver<K, B> resolver) {
        this.resolver = resolver;
        return this;
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
            ConcurrentMap<K, B> collected = WorkerUtil.newWorker(this.config.getWorkspace(), this.clzOfBean)
                    .toList(ArrayList::new)
                    .stream()
                    .collect(Collectors.toConcurrentMap(b -> this.resolver.resolve(b), b -> b));
            this.cached.clear();
            this.cached.putAll(collected);
            LOGGER.info("initialize struct successfully. identify:{}, clz:{}", this.identify(), this.clzOfBean);
        } catch (Exception e) {
            LOGGER.error("initialize struct failure. identify:{}, clz:{}", this.identify(), this.clzOfBean, e);
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
        while (!STATUS_UPDATER.compareAndSet(this, STATUS_UPDATER.get(this), NORMAL)) {

        }
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
    public B getOrDefault(K key, B dv) {
        return Optional.ofNullable(this.get(key)).orElse(dv);
    }

    @Override
    public Optional<B> tryGet(K key) {
        return Optional.ofNullable(this.get(key));
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
