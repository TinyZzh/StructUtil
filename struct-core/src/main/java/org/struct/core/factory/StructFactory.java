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

package org.struct.core.factory;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
public interface StructFactory {

    /**
     * Parse struct class.
     */
    void parseStruct();

    /**
     * Create struct class instance.
     *
     * @param structImpl the struct impl data.
     * @return optional of struct class instance.
     */
    Object newStructInstance(Object structImpl);

    /**
     * Get struct field's value.
     *
     * @param src     the struct instance.
     * @param refKeys ref keys array.
     * @return {@link Object[]} or field value.
     */
    Object getFieldValuesArray(Object src, String[] refKeys);
}
