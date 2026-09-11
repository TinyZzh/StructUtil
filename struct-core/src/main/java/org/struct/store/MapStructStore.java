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

package org.struct.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.TypeRefFactory;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The {@link StructKeyResolver} based {@link StructStore}.
 * <p>
 * The {@link #keyResolver} must be injected before {@link #initialize()}. a container integration
 * resolves it beforehand, see {@code org.struct.spring.support.MapStructStore}.
 *
 * @author TinyZ.
 * @version 2020.07.12
 */
public class MapStructStore<K, B> extends AbstractStructStore<K, B> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapStructStore.class);

    protected StructKeyResolver<K, B> keyResolver;
    /**
     * the cached struct data map.
     */
    private volatile Map<K, B> cached = Collections.EMPTY_MAP;

    /**
     * Only for the bean definition.
     */
    public MapStructStore() {
        //  bean definition
    }

    public MapStructStore(Class<B> clzOfBean) {
        super(clzOfBean);
    }

    @Override
    public void initialize() {
        if (null == this.keyResolver) {
            throw noSuchKeyResolverException("No such KeyResolver. the store:" + this.getClass().getSimpleName()
                    + ", struct:" + clzOfBean());
        }
        if (!casStatusInit()) {
            if (null != this.options && this.options.isWaitForInit())
                this.waitForDone();
            return;
        }
        try {
            Map<K, B> collected = this.loadStructData();
            this.cached = collected;
            this.size = collected.size();
            LOGGER.info("initialize [{} - {}] store successfully. total size:{}", this.clzOfBean.getName(), this.identify(), this.size);
        } catch (Exception e) {
            LOGGER.info("initialize [{} - {}] store failure.", this.clzOfBean.getName(), this.identify(), e);
        } finally {
            casStatusDone();
        }
    }

    protected Map<K, B> loadStructData() {
        StoreOptions opt = this.requireOptions();
        StructKeyResolver<K, B> resolver = this.keyResolver;
        if (null == resolver) {
            throw noSuchKeyResolverException("No such KeyResolver. the store:" + this.getClass().getSimpleName()
                    + ", struct:" + clzOfBean());
        }
        Map<K, B> map = WorkerUtil.newWorker(opt.getWorkspace(), this.clzOfBean())
                .toMap((TypeRefFactory<Map<K, B>>) HashMap::new, resolver::resolve);
        return Collections.unmodifiableMap(map);
    }

    /**
     * The factory hook of the {@code NoSuchKeyResolverException}.
     * <p>
     * The spring compatible shell overrides it so that the spring users keep catching
     * {@code org.struct.spring.exceptions.NoSuchKeyResolverException}.
     *
     * @param msg the exception's message.
     * @return the runtime exception to throw.
     */
    protected RuntimeException noSuchKeyResolverException(String msg) {
        return new NoSuchKeyResolverException(msg);
    }

    @Override
    public void dispose() {
        //  reset status.
        this.casStatusReset();
        this.cached = Collections.EMPTY_MAP;
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

    public StructKeyResolver<K, B> getKeyResolver() {
        return keyResolver;
    }

    public void setKeyResolver(StructKeyResolver<K, B> keyResolver) {
        this.keyResolver = keyResolver;
    }

    @Override
    public String toString() {
        return "MapStructStore{" +
                "keyResolver=" + keyResolver +
                ", clzOfBean=" + clzOfBean +
                ", size=" + size +
                '}';
    }
}
