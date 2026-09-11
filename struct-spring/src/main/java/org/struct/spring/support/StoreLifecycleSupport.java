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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.struct.store.AbstractStructStore;

import java.util.Objects;

/**
 * Bridges the spring's life cycle callbacks to the framework agnostic core store.
 * <p>
 * The core {@link AbstractStructStore} implements no callback and knows nothing about spring,
 * so every spring shell has to forward {@code afterPropertiesSet()} to {@link #afterPropertiesSet}.
 *
 * @author TinyZ.
 */
final class StoreLifecycleSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreLifecycleSupport.class);

    private StoreLifecycleSupport() {
    }

    /**
     * Resolve the options (fall back to the global {@link StructStoreConfig}) and trigger
     * {@link AbstractStructStore#initialize()} unless the store is configured lazily.
     *
     * @param store              the store being initialized.
     * @param applicationContext the spring application context.
     */
    static void afterPropertiesSet(AbstractStructStore<?, ?> store, ApplicationContext applicationContext) {
        if (null == store.getOptions()) {
            StructStoreConfig config = applicationContext.getBean(StructStoreConfig.class);
            Objects.requireNonNull(config, "config");
            store.setOptions(StoreOptionsFactory.generate(config));
        }
        LOGGER.debug("struct:{} store autowired properties completed.", store.clzOfBean());
        if (!store.getOptions().isLazyLoad()) {
            store.initialize();
        }
    }
}
