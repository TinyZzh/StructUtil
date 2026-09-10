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

import org.struct.spring.annotation.StructStoreOptions;
import org.struct.store.StoreOptions;

/**
 * Assembles the core {@link StoreOptions} out of the spring specific configuration carriers.
 * <p>
 * The assembly stays in {@code struct-spring} so that {@code struct-core} never depends on spring.
 *
 * @author TinyZ.
 * @version 2020.09.19
 */
public final class StoreOptionsFactory {

    private StoreOptionsFactory() {
    }

    public static StoreOptions generate(StructStoreOptions annotation) {
        StoreOptions controller = new StoreOptions();
        controller.setWorkspace(annotation.workspace());
        controller.setLazyLoad(annotation.lazyLoad());
        controller.setWaitForInit(annotation.waitForInit());
        return controller;
    }

    public static StoreOptions generate(StructStoreConfig config) {
        StoreOptions controller = new StoreOptions();
        controller.setWorkspace(config.getWorkspace());
        controller.setLazyLoad(config.isLazyLoad());
        controller.setWaitForInit(config.isSyncWaitForInit());
        return controller;
    }
}
