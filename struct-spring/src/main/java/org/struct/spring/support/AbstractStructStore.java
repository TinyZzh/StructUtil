package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
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
        implements StructStore<K, B>, ApplicationContextAware, SmartInitializingSingleton, DisposableBean {

    private static final int NORMAL = 0;
    private static final int INITIALIZING = 1;
    private static final int INITIALIZED = 2;
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
     * Struct util configuration.
     *
     * @see StructConstant#CONFIG
     */
    protected StructConfig config;

    protected ApplicationContext applicationContext;

    private volatile int status;

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
    public void afterSingletonsInstantiated() {
        this.config = this.applicationContext.getBean(StructConfig.class);
        LOGGER.info("struct:{} store autowired properties completed.", clzOfBean());
        if (!this.config.isLazyLoad()) {
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
    public Class<B> clzOfBean() {
        return this.clzOfBean;
    }

    @Override
    public boolean isInitialized() {
        return INITIALIZED == STATUS_UPDATER.get(this);
    }

    public Class<B> getClzOfBean() {
        return clzOfBean;
    }

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

    protected boolean casStatusInitDone() {
        return STATUS_UPDATER.compareAndSet(this, INITIALIZING, INITIALIZED);
    }

    protected void casStatusReset() {
        while (!STATUS_UPDATER.compareAndSet(this, STATUS_UPDATER.get(this), NORMAL)) {

        }
    }
}
