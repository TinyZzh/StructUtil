/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
public abstract class AbstractStructStore<K, B>
        implements StructStore<K, B>, ApplicationContextAware, InitializingBean, DisposableBean {

    private static final int NORMAL = 0;
    private static final int INITIALIZING = 1;
    private static final int DONE = 2;
    private static final AtomicIntegerFieldUpdater<AbstractStructStore> STATUS_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(AbstractStructStore.class, "status");

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStructStore.class);

    /**
     * the class of struct bean instances.
     *
     * @see StructConstant#CLZ_OF_BEAN
     */
    protected Class<B> clzOfBean;
    /**
     * Struct util options.
     * <p>
     * Options priority:
     * 1. {@link org.struct.spring.annotation.StructStoreOptions}
     * 2. {@link StructStoreConfig}'s global configuration
     *
     * @see StructConstant#KEY_OPTIONS
     */
    protected Options options;
    /**
     * store element's amount.
     */
    protected volatile int size;

    protected ApplicationContext applicationContext;

    private volatile int status;

    /// --------------- constructor ------------------

    public AbstractStructStore() {
        //  spring bean definition
    }

    public AbstractStructStore(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == this.options) {
            StructStoreConfig config = this.applicationContext.getBean(StructStoreConfig.class);
            Objects.requireNonNull(config, "config");
            this.options = Options.generate(config);
        }
        LOGGER.debug("struct:{} store autowired properties completed.", clzOfBean());
        if (!this.options.isLazyLoad()) {
            this.initialize();
        }
    }

    @Override
    public void destroy() throws Exception {
        this.dispose();
    }

    @Override
    public String identify() {
        return this.clzOfBean.getSimpleName() + StructStore.class.getSimpleName();
    }

    @Override
    public boolean isInitialized() {
        return DONE == STATUS_UPDATER.get(this);
    }

    @Override
    public Class<B> clzOfBean() {
        return this.clzOfBean;
    }

    public Class<B> getClzOfBean() {
        return clzOfBean;
    }

    @Override
    public void setClzOfBean(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
    }

    @Override
    public void reload() {
        if (!isInitialized())
            return;
        casStatusReset();
        this.initialize();
    }

    @Override
    public int size() {
        return size;
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

    //  cas

    protected boolean casStatusInit() {
        return STATUS_UPDATER.compareAndSet(this, NORMAL, INITIALIZING);
    }

    protected boolean casStatusDone() {
        return STATUS_UPDATER.compareAndSet(this, INITIALIZING, DONE);
    }

    /**
     * Wait for {@link #status} value change until the value equals {@link #DONE}.
     * Avoid multiple threads read {@link StructStore} data's operation, before {@link StructStore} initialize done.
     */
    protected void waitForDone() {
        this.waitForStatus(DONE);
    }

    private void waitForStatus(int expect) {
        for (; ; ) {
            if (expect == STATUS_UPDATER.get(this)) {
                break;
            }
        }
    }

    protected void casStatusReset() {
        while (!STATUS_UPDATER.compareAndSet(this, STATUS_UPDATER.get(this), NORMAL)) {

        }
    }
}
