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

package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * default {@link StructStore}'s service implements.
 *
 * @author TinyZ.
 * @version 2020.07.15
 */
public class StructStoreService implements BeanPostProcessor, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructStoreService.class);

    private StructStoreConfig config;

    private final ConcurrentHashMap<Class<?>, StructStore> structMap = new ConcurrentHashMap<>();

    public StructStoreService() {
    }

    public StructStoreService(StructStoreConfig config) {
        this.config = config;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof StructStoreConfig && null == this.config) {
            this.config = (StructStoreConfig) bean;
        } else if (bean instanceof StructStore) {
            StructStore store = (StructStore) bean;
            StructStore prev = structMap.putIfAbsent(store.clzOfBean(), store);
            if (null != prev) {
                LOGGER.debug("struct:{} has bean registered by {}.", store.clzOfBean().getName(), prev);
            } else {
                LOGGER.info("struct:{} register success.", store.clzOfBean().getName());
            }
        }
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        this.structMap.clear();
    }

    public void setConfig(StructStoreConfig config) {
        this.config = config;
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
