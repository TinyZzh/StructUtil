package org.excel.annotation;

import org.excel.core.Converter;

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
public @interface ExcelField {

    /**
     * @return the property's field name.
     */
    String name() default "";

    /**
     * reference {@link ExcelSheet} class.
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
    boolean required() default true;

    /**
     * @return use custom converter replace the default system converter to convert this field value.
     */
    Class<Converter> converter() default Converter.class;
}
