package org.struct.spring.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author TinyZ.
 * @version 2020.07.17
 */
public class StructScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(StructScan.class.getName()));
        Objects.requireNonNull(annoAttrs, "annoAttrs");
        ClassPathStructScanner scanner = new ClassPathStructScanner(registry, false);
        // set ResourceLoader
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(BeanUtils.instantiateClass(generatorClass));
        }
        //  scan base package
        List<String> basePackages = new ArrayList<>();
        Stream.of(annoAttrs.getStringArray("value"),
                annoAttrs.getStringArray("basePackages"),
                Arrays.stream(annoAttrs.getClassArray("basePackageClasses"))
                        .map(ClassUtils::getPackageName)
                        .distinct()
                        .toArray(String[]::new)
        )
                .flatMap((Function<String[], Stream<String>>) Stream::of)
                .filter(StringUtils::hasText)
                .forEach(basePackages::add);
        //  scan
        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
