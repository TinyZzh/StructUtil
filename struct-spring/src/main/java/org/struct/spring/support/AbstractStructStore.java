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

package org.struct.spring.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.struct.store.StoreOptions;

/**
 * The spring compatible shell of {@link org.struct.store.AbstractStructStore}.
 * <p>
 * The core store doesn't implement any life cycle callback; this shell bridges the spring's
 * {@link InitializingBean} / {@link DisposableBean} callbacks to the explicit
 * {@link org.struct.store.StructStore#initialize()} / {@link org.struct.store.StructStore#dispose()}.
 *
 * @author TinyZ.
 * @version 2020.07.12
 * @deprecated use {@link org.struct.store.AbstractStructStore} instead. this shell will be removed in 6.0.
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public abstract class AbstractStructStore<K, B> extends org.struct.store.AbstractStructStore<K, B>
        implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * Spring application context.
     */
    protected ApplicationContext applicationContext;

    public AbstractStructStore() {
        //  spring bean definition
    }

    public AbstractStructStore(Class<B> clzOfBean) {
        super(clzOfBean);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        StoreLifecycleSupport.afterPropertiesSet(this, this.applicationContext);
    }

    @Override
    public void destroy() throws Exception {
        this.dispose();
    }

    @Override
    public void setOptions(StoreOptions options) {
        this.options = options;
    }
}
