/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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

package org.excel.annotation;

import org.excel.core.ExcelWorker;

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
public @interface ExcelSheet {

    /**
     * @return the excel file's name.
     */
    String fileName() default "";

    /**
     * @return the excel sheet name which will be loaded.
     */
    String sheetName() default "Sheet1";

    /**
     * @return the first row's order of excel. {@link ExcelWorker} will load
     * from startOrder to endOrder. default 1. it is means from the second row
     */
    int startOrder() default 1;

    /**
     * @return the last row's order of excel. {@link ExcelWorker} will load
     * from startOrder to endOrder. default -1. it is means ths real excel's last row.
     */
    int endOrder() default -1;
}
