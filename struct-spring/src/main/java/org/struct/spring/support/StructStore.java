package org.struct.spring.support;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
public interface StructStore<K, B> {

    /**
     * Cache's unique identify.
     *
     * @return Cache's unique identify
     */
    String identify();

    /**
     * Get class of the store bean instances.
     *
     * @return class of the store bean instances.
     */
    Class<B> clzOfBean();

    void initialize();

    boolean isInitialized();

    void reload();

    void dispose();

    List<B> getAll();

    B get(K key);

    B getOrDefault(K key, B dv);

    Optional<B> tryGet(K key);

    List<B> lookup(K... keys);

    List<B> lookup(Predicate<B> filter);

}
