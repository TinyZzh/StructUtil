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

import org.struct.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

/**
 * {@link MapStructStore} map key field's value resolver.
 *
 * @author TinyZ.
 * @date 2022-03-01.
 */
public class MapKeyFieldResolver implements StructKeyResolver<Object, Object> {

    /**
     * The {@link MapStructStore}'s key field's name.
     */
    public final String fieldName;

    /**
     * Constructor for mapped field.
     *
     * @param fieldName the {@link MapStructStore} key field's name.
     */
    public MapKeyFieldResolver(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Get mapped field's value from bean.
     *
     * @param bean the struct bean instance.
     * @return the mapped field's value.
     * @throws RuntimeException the unknown bean field.
     */
    @Override
    public Object resolve(Object bean) throws RuntimeException {
        Optional<MethodHandle> mh = Reflects.lookupFieldGetter(bean.getClass(), this.fieldName);
        return mh.map(m -> {
            try {
                return m.invoke(bean);
            } catch (Throwable e) {
                throw new RuntimeException("unknown bean field:" + fieldName + ".");
            }
        }).orElse(null);
    }
}
