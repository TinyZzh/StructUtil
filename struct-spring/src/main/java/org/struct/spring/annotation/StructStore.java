package org.struct.spring.annotation;

import org.struct.spring.support.StructKeyResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TinyZ.
 * @version 2020.07.12
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented()
public @interface StructStore {

    String value();

    String keyResolverBeanName() default "";

    Class<? extends StructKeyResolver> keyResolver() default StructKeyResolver.class;


}
