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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The framework agnostic {@link StructStore} skeleton.
 * <p>
 * This class deliberately implements <b>no</b> life cycle callback: {@link #initialize()} is an
 * explicit call. a container integration (spring, ...) triggers it from its own callback, see
 * {@code org.struct.spring.support.AbstractStructStore}.
 *
 * @author TinyZ.
 * @version 2020.07.12
 */
public abstract class AbstractStructStore<K, B> implements StructStore<K, B> {

    protected static final int NORMAL = 0;
    protected static final int INITIALIZING = 1;
    protected static final int DONE = 2;
    private static final AtomicIntegerFieldUpdater<AbstractStructStore> STATUS_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(AbstractStructStore.class, "status");

    /**
     * the default bounded spin timeout(ms) of {@link #waitForStatus(int)} and {@link #casStatusReset()}.
     */
    public static final long DEFAULT_WAIT_FOR_INIT_TIMEOUT_MS = 30_000L;

    /**
     * the class of struct bean instances.
     *
     * @see StoreConstant#CLZ_OF_BEAN
     */
    protected Class<B> clzOfBean;
    /**
     * Struct util options.
     *
     * @see StoreConstant#KEY_OPTIONS
     */
    protected StoreOptions options;
    /**
     * store element's amount.
     */
    protected volatile int size;
    /**
     * The bounded spin timeout(ms). the waiting operation throws {@link IllegalStateException} on timeout
     * instead of hanging forever.
     */
    private volatile long waitForInitTimeoutMs = DEFAULT_WAIT_FOR_INIT_TIMEOUT_MS;
    /**
     * Store's status.
     *
     * @see #NORMAL
     * @see #INITIALIZING
     * @see #DONE
     */
    private volatile int status;

    /// --------------- constructor ------------------

    public AbstractStructStore() {
        //  bean definition
    }

    public AbstractStructStore(Class<B> clzOfBean) {
        this.clzOfBean = clzOfBean;
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

    public StoreOptions getOptions() {
        return options;
    }

    public void setOptions(StoreOptions options) {
        this.options = options;
    }

    public long getWaitForInitTimeoutMs() {
        return waitForInitTimeoutMs;
    }

    public void setWaitForInitTimeoutMs(long waitForInitTimeoutMs) {
        this.waitForInitTimeoutMs = waitForInitTimeoutMs;
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

    /**
     * Resolve the {@link #options} or fail fast with an explicit message.
     *
     * @return the injected options.
     * @throws IllegalStateException if the {@link #options} has not been injected yet.
     */
    protected StoreOptions requireOptions() {
        StoreOptions opt = this.options;
        if (null == opt) {
            throw new IllegalStateException("the store's options has not been injected. store:"
                    + this.getClass().getName() + ", struct:" + this.clzOfBean);
        }
        return opt;
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

    /**
     * Bounded spin wait. throws {@link IllegalStateException} when the
     * {@link #waitForInitTimeoutMs} elapsed instead of hanging forever.
     *
     * @param expect the expected status.
     */
    protected void waitForStatus(int expect) {
        final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(this.waitForInitTimeoutMs);
        for (; ; ) {
            if (expect == STATUS_UPDATER.get(this)) {
                return;
            }
            if (System.nanoTime() - deadline >= 0) {
                throw new IllegalStateException("timeout waiting for the store's status. identify:"
                        + (null == this.clzOfBean ? this.getClass().getName() : this.identify())
                        + ", expect:" + expect + ", timeoutMs:" + this.waitForInitTimeoutMs);
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Reset the status to {@link #NORMAL}. bounded spin, see {@link #waitForStatus(int)}.
     */
    protected void casStatusReset() {
        final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(this.waitForInitTimeoutMs);
        while (!STATUS_UPDATER.compareAndSet(this, STATUS_UPDATER.get(this), NORMAL)) {
            if (System.nanoTime() - deadline >= 0) {
                throw new IllegalStateException("timeout resetting the store's status. identify:"
                        + (null == this.clzOfBean ? this.getClass().getName() : this.identify())
                        + ", timeoutMs:" + this.waitForInitTimeoutMs);
            }
            Thread.onSpinWait();
        }
    }
}
