package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * default {@link StructStore}'s service implements.
 *
 * @author TinyZ.
 * @version 2020.07.15
 */
public class StructStoreService implements SmartInitializingSingleton, DisposableBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructStoreService.class);

    private StructConfig config;

    private ApplicationContext applicationContext;

    private final ConcurrentHashMap<Class<?>, StructStore> structMap = new ConcurrentHashMap<>();

    public StructStoreService() {
    }

    public StructStoreService(StructConfig config) {
        this.config = config;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (null == this.config) {
            this.config = this.applicationContext.getBean(StructConfig.class);
        }
        Map<String, StructStore> beansMap = this.applicationContext.getBeansOfType(StructStore.class);
        beansMap.values().forEach(ss -> {
            StructStore prev = structMap.putIfAbsent(ss.clzOfBean(), ss);
            if (null != prev) {
                LOGGER.info("struct:{} has bean registered  by {}.", ss.clzOfBean(), prev);
            }
        });
        LOGGER.info("struct store service initialize completed.");
    }

    @Override
    public void destroy() throws Exception {
        this.structMap.clear();
    }

    private <K, B> Optional<StructStore<K, B>> lookup(Class<B> clzOfBean) {
        Optional<StructStore<K, B>> optional = Optional.ofNullable(structMap.get(clzOfBean));
        if (config.isLazyLoad()) {
            optional.ifPresent(ss -> {
                if (!ss.isInitialized()) {
                    ss.initialize();
                }
            });
        }
        return optional;
    }

    public <K, B> void initialize(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::initialize);
    }

    public <K, B> void reload(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::reload);
    }

    public <K, B> void dispose(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::dispose);
    }

    public <K, B> List<B> getAll(Class<B> clzOfBean) {
        return lookup(clzOfBean).map(StructStore::getAll).orElse(Collections.emptyList());
    }

    public <K, B> B get(Class<B> clzOfBean, K key) {
        return lookup(clzOfBean).map(m -> m.get(key)).orElse(null);
    }

    public <K, B> B getOrDefault(Class<B> clzOfBean, K key, B dv) {
        return lookup(clzOfBean).map(m -> m.getOrDefault(key, dv)).orElse(null);
    }

    public <K, B> Optional<B> tryGet(Class<B> clzOfBean, K key) {
        return lookup(clzOfBean).flatMap(m -> m.tryGet(key));
    }

    public <K, B> List<B> lookup(Class<B> clzOfBean, K... keys) {
        return lookup(clzOfBean).map(m -> m.lookup(keys)).orElse(Collections.emptyList());
    }

    public <K, B> List<B> lookup(Class<B> clzOfBean, Predicate<B> filter) {
        return lookup(clzOfBean).map(m -> m.lookup(filter)).orElse(Collections.emptyList());
    }
}
