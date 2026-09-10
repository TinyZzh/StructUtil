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

/**
 * The spring compatible shell of {@link org.struct.store.ListStructStore}.
 * <p>
 * Not support all <code>key</code> related method. like {@link #get(Object)}.
 *
 * @author TinyZ.
 * @version 2020.07.12
 * @deprecated use {@link org.struct.store.ListStructStore} instead. this shell will be removed in 6.0.
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public class ListStructStore<B> extends org.struct.store.ListStructStore<B>
        implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * Spring application context.
     */
    protected ApplicationContext applicationContext;

    /**
     * Only for spring framework bean definition.
     */
    public ListStructStore() {
        //  spring bean definition
    }

    public ListStructStore(Class<B> clzOfBean) {
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
}
