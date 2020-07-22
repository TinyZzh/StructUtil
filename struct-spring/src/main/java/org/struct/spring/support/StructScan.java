package org.struct.spring.support;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(StructScannerRegistrar.class)
public @interface StructScan {

    /**
     * Alias for {@link #basePackages}.
     * <p>Allows for more concise annotation declarations if no other attributes
     * are needed &mdash; for example, {@code @ComponentScan("org.my.pkg")}
     * instead of {@code @ComponentScan(basePackages = "org.my.pkg")}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components.
     * <p>{@link #value} is an alias for (and mutually exclusive with) this
     * attribute.
     * <p>Use {@link #basePackageClasses} for a type-safe alternative to
     * String-based package names.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected components
     * within the Spring container.
     * <p>The default value of the {@link BeanNameGenerator} interface itself indicates
     * that the scanner used to process this {@code @ComponentScan} annotation should
     * use its inherited bean name generator, e.g. the default
     * {@link AnnotationBeanNameGenerator} or any custom instance supplied to the
     * application context at bootstrap time.
     *
     * @see AnnotationConfigApplicationContext#setBeanNameGenerator(BeanNameGenerator)
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * Struct worker's workspace.
     */
    String workspace() default "";

    String keyResolverBeanName() default "";


}

