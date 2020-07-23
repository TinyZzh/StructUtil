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
import org.struct.spring.exceptions.NoSuchKeyResolverException;
import org.struct.util.WorkerUtil;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
public class MapStructStore<K, B> extends AbstractStructStore<K, B> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapStructStore.class);

    /**
     * {@link StructKeyResolver}'s bean name.
     *
     * @see StructConstant#KEY_RESOLVER_BEAN_NAME
     */
    protected String keyResolverBeanName;
    /**
     * {@link StructKeyResolver}'s bean class.
     *
     * @see StructConstant#KEY_RESOLVER_BEAN_CLASS
     */
    protected Class<? extends StructKeyResolver<K, B>> keyResolverBeanClass;
    protected StructKeyResolver<K, B> keyResolver;
    /**
     * the cached struct data map.
     */
    private ConcurrentHashMap<K, B> cached = new ConcurrentHashMap<>();

    public MapStructStore() {
        //  spring bean definition
    }

    public MapStructStore(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        resolveKeyResolver();
        super.afterSingletonsInstantiated();
    }

    private void resolveKeyResolver() {
        if (null == this.keyResolver) {
            try {
                String krbn = this.keyResolverBeanName;
                if (null != krbn
                        && !krbn.isEmpty()) {
                    this.keyResolver = this.applicationContext.getBean(StructKeyResolver.class, krbn);
                }
            } catch (Exception e) {
                //  no-op
            }
        }
        if (null == this.keyResolver) {
            try {
                Class<? extends StructKeyResolver> krbs = this.keyResolverBeanClass;
                if (null != krbs
                        && StructKeyResolver.class != krbs) {
                    assert !Modifier.isAbstract(krbs.getModifiers());
                    this.keyResolver = this.applicationContext.getBean(krbs);
                }
            } catch (Exception e) {
                //  no-op
            }
        }
        if (null == this.keyResolver) {
            throw new NoSuchKeyResolverException("Can not resolve store:" + this.getClass().getSimpleName() + " struct:" + clzOfBean()
                    + "'s keyResolver with beanName:" + this.keyResolverBeanName + ", beanClass:" + this.keyResolverBeanClass);
        }
    }

    @Override
    public void initialize() {
        if (!casStatusInit()) {
            return;
        }
        try {
            Map<K, B> collected = WorkerUtil.newWorker(this.config.getWorkspace(), this.clzOfBean())
                    .toList(ArrayList::new)
                    .stream()
                    .collect(Collectors.toMap(b -> this.keyResolver.resolve(b), b -> b));
            this.cached.clear();
            this.cached.putAll(collected);
            LOGGER.info("initialize struct data successfully. identify:{}, clz:{}", this.identify(), this.clzOfBean);
        } catch (Exception e) {
            LOGGER.error("initialize struct data failure. identify:{}, clz:{}", this.identify(), this.clzOfBean, e);
        } finally {
            casStatusInitDone();
        }
    }

    @Override
    public void dispose() {
        //  reset status.
        this.casStatusReset();
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
    public List<B> lookup(Predicate<B> filter) {
        return this.cached.values().stream().filter(filter).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String getKeyResolverBeanName() {
        return keyResolverBeanName;
    }

    public void setKeyResolverBeanName(String keyResolverBeanName) {
        this.keyResolverBeanName = keyResolverBeanName;
    }

    public Class<? extends StructKeyResolver<K, B>> getKeyResolverBeanClass() {
        return keyResolverBeanClass;
    }

    public void setKeyResolverBeanClass(Class<? extends StructKeyResolver<K, B>> keyResolverBeanClass) {
        this.keyResolverBeanClass = keyResolverBeanClass;
    }

    public StructKeyResolver<K, B> getKeyResolver() {
        return keyResolver;
    }

    public void setKeyResolver(StructKeyResolver<K, B> keyResolver) {
        this.keyResolver = keyResolver;
    }

    @Override
    public String toString() {
        return "MapStructStore{" +
                "keyResolverBeanName='" + keyResolverBeanName + '\'' +
                ", keyResolverBeanClass=" + keyResolverBeanClass +
                ", clzOfBean=" + clzOfBean +
                "} " + super.toString();
    }
}