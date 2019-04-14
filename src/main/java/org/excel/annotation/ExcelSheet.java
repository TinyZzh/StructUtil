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
