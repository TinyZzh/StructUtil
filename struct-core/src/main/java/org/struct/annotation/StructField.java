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

package org.struct.annotation;

import org.struct.core.converter.Converter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TinyZ.
 * @version 2019.03.23
 */
@Inherited
@Documented()
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StructField {

    /**
     * @return the property's field name.
     */
    String name() default "";

    /**
     * reference {@link StructSheet} class.
     */
    Class<?> ref() default Object.class;

    /**
     * @return define the target bean's field which collection result will be group by.
     */
    String[] refGroupBy() default {};

    /**
     * the ref unique key.
     */
    String[] refUniqueKey() default {};

    /**
     * is this field required.
     */
    boolean required() default false;

    /**
     * @return use custom converter replace the default system converter to convert this field value.
     */
    Class<? extends Converter> converter() default Converter.class;
}
