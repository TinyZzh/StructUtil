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

package org.struct.annotation;

import org.struct.core.StructImpl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Choice any one of {@link StructField} to resolve reference field's value.
 *
 * @author TinyZ.
 * @version 2020.08.16
 * @see StructField
 */
@Inherited
@Documented()
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StructOptional {

    /**
     * @return the property's field name.
     */
    String name() default "";

    /**
     * define reference field's probably {@link StructField} array.
     * <strong>the array's index is execute order</strong>.
     * foreach all {@link StructField[]} element until resolve reference field's and break this iteration.
     *
     * @return {@link StructField} array.
     * @see org.struct.core.StructWorker#createInstance(StructImpl)
     */
    StructField[] value() default {};

}
