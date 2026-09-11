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

/**
 * The spring compatible shell of {@link org.struct.store.StructStore}.
 * <p>
 * The store implementation has been sunk into {@code struct-core}. this interface only remains so
 * that the existing {@code import org.struct.spring.support.StructStore;} keeps compiling.
 * <strong>All the type judgements must use {@link org.struct.store.StructStore}.</strong>
 *
 * @author TinyZ.
 * @version 2020.07.12
 * @deprecated use {@link org.struct.store.StructStore} instead. this shell will be removed in 6.0.
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public interface StructStore<K, B> extends org.struct.store.StructStore<K, B> {

}
