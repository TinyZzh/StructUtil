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

package org.struct.spi;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceLoader {

    private static final ConcurrentHashMap<Class<?>, EnhancedServiceLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>();

    public static <S> List<S> loadAll(Class<S> service) {
        return loader(service).loadAll();
    }

    @SuppressWarnings("rawTypes")
    private static <S> EnhancedServiceLoader<S> loader(Class<S> service) {
        return (EnhancedServiceLoader<S>) EXTENSION_LOADER_MAP.computeIfAbsent(service, EnhancedServiceLoader::new);
    }
}
