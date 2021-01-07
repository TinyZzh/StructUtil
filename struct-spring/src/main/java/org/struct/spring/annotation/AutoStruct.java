/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

import org.struct.spring.support.StructKeyResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Either {@link #keyResolverBeanName()} or {@link #keyResolverBeanClass()} must be set.
 *
 * @author TinyZ.
 * @version 2020.07.12
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented()
public @interface AutoStruct {

    /**
     * @return {@link org.struct.spring.support.MapStructStore}'s {@link StructKeyResolver}'s bean name.
     */
    String keyResolverBeanName() default "";

    /**
     * @return {@link org.struct.spring.support.MapStructStore}'s {@link StructKeyResolver}'s bean class.
     */
    Class<? extends StructKeyResolver> keyResolverBeanClass() default StructKeyResolver.class;

}
