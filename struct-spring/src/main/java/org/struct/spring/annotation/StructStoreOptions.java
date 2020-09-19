/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

package org.struct.spring.annotation;

import org.struct.spring.support.StructConstant;
import org.struct.spring.support.StructStore;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TinyZ.
 * @version 2020.09.18
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented()
public @interface StructStoreOptions {

    /**
     * the struct store's workspace directory.
     *
     * @return struct store's workspace directory.
     */
    String workspace() default StructConstant.STRUCT_WORKSPACE;

    /**
     * Lazy load struct data. default: false.
     *
     * @return true if load struct data before user get it, otherwise false.
     */
    boolean lazyLoad() default false;

    /**
     * When the {@link #lazyLoad} is true, is user should sync wait for {@link StructStore} init done.
     *
     * @return true wait for {@link StructStore} initialize completed.
     */
    boolean waitForInit() default false;

}
