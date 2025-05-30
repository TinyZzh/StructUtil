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

package org.struct.core.converter;

/**
 * The instance type converter.
 */
public interface Converter {

    /**
     * Covert origin value to target field's type.
     *
     * @param ctx
     * @param originValue the origin value.
     * @param targetType  the convert to field type.
     * @return return the converted value
     */
    Object convert(ConvertContext ctx, Object originValue, Class<?> targetType);


}
