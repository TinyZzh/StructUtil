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

/**
 * The {@link StructStore}'s bean definition constant keys and default values.
 * <p>
 * These keys are part of the (de)serialization contract between the bean definition
 * generator and the store beans - changing a value silently breaks every store
 * registered by the generator.
 *
 * @author TinyZ.
 * @version 2020.07.09
 */
public final class StoreConstant {

    public static final String CLZ_OF_BEAN = "clzOfBean";
    public static final String KEY_RESOLVER = "keyResolver";
    public static final String KEY_RESOLVER_BEAN_NAME = "keyResolverBeanName";
    public static final String KEY_RESOLVER_BEAN_CLASS = "keyResolverBeanClass";
    public static final String KEY_OPTIONS = "options";

    public static final String STRUCT_WORKSPACE = "./data/";

}
