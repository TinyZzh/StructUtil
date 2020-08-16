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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StructOptional {

    /**
     * @return the property's field name.
     */
    String name() default "";

    /**
     * define reference field's probably {@link StructField} array.
     * **the array's index is execute order**.
     * foreach all {@link StructField[]} element until resolve reference field's and break this iteration.
     *
     * @return {@link StructField} array.
     * @see org.struct.core.StructWorker#createInstance(StructImpl)
     */
    StructField[] value() default {};

}
