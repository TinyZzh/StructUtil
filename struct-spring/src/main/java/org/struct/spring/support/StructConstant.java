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

import org.struct.store.StoreConstant;

/**
 * The spring compatible shell of {@link StoreConstant}. all the values are delegated.
 *
 * @author TinyZ.
 * @version 2020.07.09
 * @deprecated use {@link StoreConstant} instead. this shell will be removed in 6.0.
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public final class StructConstant {

    public static final String CLZ_OF_BEAN = StoreConstant.CLZ_OF_BEAN;
    public static final String KEY_RESOLVER = StoreConstant.KEY_RESOLVER;
    public static final String KEY_RESOLVER_BEAN_NAME = StoreConstant.KEY_RESOLVER_BEAN_NAME;
    public static final String KEY_RESOLVER_BEAN_CLASS = StoreConstant.KEY_RESOLVER_BEAN_CLASS;
    public static final String KEY_OPTIONS = StoreConstant.KEY_OPTIONS;

    public static final String STRUCT_WORKSPACE = StoreConstant.STRUCT_WORKSPACE;

}
