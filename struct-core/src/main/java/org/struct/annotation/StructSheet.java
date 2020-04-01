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

package org.struct.annotation;

import org.struct.core.StructWorker;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.matcher.WorkerMatcher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TinyZ.
 * @version 2019.04.02
 */
@Inherited
@Documented()
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StructSheet {

    /**
     * @return the struct file's name.
     */
    String fileName() default "";

    /**
     * @return the struct sheet name which will be loaded.
     */
    String sheetName() default "Sheet1";

    /**
     * @return the first row's order of struct. {@link StructWorker} will load
     * from startOrder to endOrder. default 1.
     * it is means from the second row in excel(0-based) or first line in other data file.
     */
    int startOrder() default 1;

    /**
     * @return last last row or line order of struct. {@link StructWorker} will load
     * from startOrder to endOrder. default -1. it's means load data until the EOF.
     * if user set the other large than zero's numeric means the data file's last row or last line.
     */
    int endOrder() default -1;

    /**
     * the sheet worker matcher.
     * try auto choice the worker to convert the data file.
     *
     * @return the sheet's worker matcher. default is auto. {@link WorkerMatcher}
     */
    Class<? extends WorkerMatcher> matcher() default WorkerMatcher.class;

    /**
     * the bean instance filter after struct data convert completed.
     */
    Class<? extends StructBeanFilter> filter() default StructBeanFilter.class;

}
