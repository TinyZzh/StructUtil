package org.struct.spring.support;

import java.util.List;
import java.util.function.Consumer;
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

    Class<B> clzOfBean();

    void initialize();

    boolean isInitialized();

    void reload();

    void dispose();

    List<B> getAll();

    B get(K key);

    List<B> lookup(K... keys);

    List<B> lookup(Predicate<B> filter);

    void addShutdownHook(Consumer<List<B>> consumer);

}
