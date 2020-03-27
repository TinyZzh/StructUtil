package org.struct.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SPI {

    int LOWEST = Integer.MAX_VALUE;

    int HIGHEST = Integer.MIN_VALUE;

    /**
     * @return Extension point name.
     */
    String name();

    /**
     * @return the extension's order
     */
    int order() default LOWEST;

}
