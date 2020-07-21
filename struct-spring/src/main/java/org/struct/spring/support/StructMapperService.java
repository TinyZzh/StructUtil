package org.struct.spring.support;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * default {@link StructMapper}'s service implements.
 *
 * @author TinyZ.
 * @version 2020.07.15
 */
public class StructMapperService {

    private final ConcurrentHashMap<Class<?>, StructStore> structMap = new ConcurrentHashMap<>();

    private <K, B extends StructStore<K, B>> Optional<StructStore<K, B>> lookup(Class<B> clzOfBean) {
        return Optional.ofNullable(structMap.get(clzOfBean));
    }

    public <K, B extends StructStore<K, B>> void initialize(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::initialize);
    }

    public <K, B extends StructStore<K, B>> void reload(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::reload);
    }

    public <K, B extends StructStore<K, B>> void dispose(Class<B> clzOfBean) {
        lookup(clzOfBean).ifPresent(StructStore::dispose);
    }

    public <K, B extends StructStore<K, B>> List<B> getAll(Class<B> clzOfBean) {
        return lookup(clzOfBean).map(StructStore::getAll).orElse(Collections.emptyList());
    }

    public <K, B extends StructStore<K, B>> B get(Class<B> clzOfBean, K key) {
        return lookup(clzOfBean).map(m -> m.get(key)).orElse(null);
    }

    public <K, B extends StructStore<K, B>> List<B> lookup(Class<B> clzOfBean, K... keys) {
        return lookup(clzOfBean).map(m -> m.lookup(keys)).orElse(Collections.emptyList());
    }

    public <K, B extends StructStore<K, B>> List<B> lookup(Class<B> clzOfBean, Predicate<B> filter) {
        return lookup(clzOfBean).map(m -> m.lookup(filter)).orElse(Collections.emptyList());
    }

    public <K, B extends StructStore<K, B>> void addShutdownHook(Class<B> clzOfBean, Consumer<List<B>> consumer) {
        lookup(clzOfBean).ifPresent(m -> m.addShutdownHook(consumer));
    }


}
