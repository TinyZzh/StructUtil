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
import org.struct.spring.exceptions.NoSuchKeyResolverException;
import org.struct.store.StoreConstant;
import org.struct.util.Reflects;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

/**
 * The spring compatible shell of {@link org.struct.store.MapStructStore}.
 * <p>
 * The shell keeps resolving the {@link org.struct.store.StructKeyResolver} from the spring
 * {@code ApplicationContext}; the core store only accepts a resolved instance.
 *
 * @author TinyZ.
 * @version 2020.07.12
 * @deprecated use {@link org.struct.store.MapStructStore} instead. this shell will be removed in 6.0.
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public class MapStructStore<K, B> extends org.struct.store.MapStructStore<K, B>
        implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * {@link org.struct.store.StructKeyResolver}'s bean name.
     *
     * @see StoreConstant#KEY_RESOLVER_BEAN_NAME
     */
    protected String keyResolverBeanName;
    /**
     * {@link org.struct.store.StructKeyResolver}'s bean class.
     *
     * @see StoreConstant#KEY_RESOLVER_BEAN_CLASS
     */
    protected Class<? extends org.struct.store.StructKeyResolver<K, B>> keyResolverBeanClass;
    /**
     * Spring application context.
     */
    protected ApplicationContext applicationContext;

    /**
     * Only for spring framework bean definition.
     */
    public MapStructStore() {
        //  spring bean definition
    }

    public MapStructStore(Class<B> clzOfBean) {
        super(clzOfBean);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        resolveKeyResolver();
        StoreLifecycleSupport.afterPropertiesSet(this, this.applicationContext);
    }

    @Override
    public void destroy() throws Exception {
        this.dispose();
    }

    /**
     * If {@link #keyResolver}'s value is null,
     * try resolve the store's {@link #keyResolver} by {@link #keyResolverBeanName} or {@link #keyResolverBeanClass}.
     */
    protected void resolveKeyResolver() {
        org.struct.store.StructKeyResolver<K, B> resolver = this.keyResolver;
        if (null == resolver) {
            String beanName = this.keyResolverBeanName;
            if (beanName != null && !beanName.isEmpty()) {
                resolver = this.applicationContext.getBean(beanName, org.struct.store.StructKeyResolver.class);
            }
        }
        Class<? extends org.struct.store.StructKeyResolver<K, B>> beanClass = this.keyResolverBeanClass;
        if (null != beanClass) {
            if (null == resolver
                    && !Objects.equals(org.struct.store.StructKeyResolver.class, beanClass)) {
                Map<String, ? extends org.struct.store.StructKeyResolver<K, B>> beansOfTypeMap
                        = this.applicationContext.getBeansOfType(beanClass);
                //  any bean of that type will do - the caller asked for a specific class.
                if (!beansOfTypeMap.isEmpty()) {
                    resolver = beansOfTypeMap.values().iterator().next();
                }
            }
            //  create new key resolver out of spring framework. an interface is abstract too.
            if (null == resolver
                    && !Modifier.isAbstract(beanClass.getModifiers())) {
                resolver = Reflects.newInstance(beanClass);
            }
        }
        if (null == resolver) {
            throw noSuchKeyResolverException("No such KeyResolver. the store:" + this.getClass().getSimpleName() + ", struct:" + clzOfBean()
                    + ", keyBeanName:" + this.keyResolverBeanName + ", keyBeanClass:" + this.keyResolverBeanClass);
        }
        this.keyResolver = resolver;
    }

    /**
     * Keep the spring's exception type for the existing catch clauses.
     */
    @Override
    protected RuntimeException noSuchKeyResolverException(String msg) {
        return new NoSuchKeyResolverException(msg);
    }

    public String getKeyResolverBeanName() {
        return keyResolverBeanName;
    }

    public void setKeyResolverBeanName(String keyResolverBeanName) {
        this.keyResolverBeanName = keyResolverBeanName;
    }

    public Class<? extends org.struct.store.StructKeyResolver<K, B>> getKeyResolverBeanClass() {
        return keyResolverBeanClass;
    }

    public void setKeyResolverBeanClass(Class<? extends org.struct.store.StructKeyResolver<K, B>> keyResolverBeanClass) {
        this.keyResolverBeanClass = keyResolverBeanClass;
    }

    @Override
    public String toString() {
        return "MapStructStore{" +
                "keyResolverBeanName='" + keyResolverBeanName + '\'' +
                ", keyResolverBeanClass=" + keyResolverBeanClass +
                ", clzOfBean=" + clzOfBean +
                '}';
    }
}
