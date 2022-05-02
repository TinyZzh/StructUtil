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
 * Define non-primitive type field struct and handle special converter behavior.
 * <p>
 * e.g.
 * <pre> {@code
 *     class B {
 *
 *          //  Convert A struct file to Map&lt;id, A>
 *          @StructField(ref = A.class, refUniqueKey = "A.id")
 *          private A obj;
 *
 *          //  Convert A struct file to Map&lt;Tag, List&lt;A>>
 *          @StructField(ref = A.class, refGroupBy = "A.tag") //
 *          private List&lt;A> list;
 *
 *          //  Convert A struct file to Map&lt;Tag, Map&lt;id, A>>>
 *          @StructField(ref = A.class, refGroupBy = "A.tag", refUniqueKey = "A.id")
 *          private Map&lt;Integer, A> map;
 *     }
 * </pre>
 *
 * @author TinyZ.
 * @version 2019.03.23
 */
@Inherited
@Documented()
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StructField {

    /**
     * the property's field name.
     *
     * @return the property's field name.
     */
    String name() default "";

    /**
     * the reference {@link StructSheet} class.
     */
    Class<?> ref() default Object.class;

    /**
     * @return define the target bean's field which collection result will be grouping by.
     */
    String[] refGroupBy() default {};

    /**
     * @return the ref unique key.
     */
    String[] refUniqueKey() default {};

    /**
     * Is this field required.
     *
     * @return true if the field is requirement, otherwise false.
     */
    boolean required() default false;

    /**
     * Use the custom converter replace the default system converter to convert this field value.
     *
     * @return the converter class.
     */
    Class<? extends Converter> converter() default Converter.class;
}
