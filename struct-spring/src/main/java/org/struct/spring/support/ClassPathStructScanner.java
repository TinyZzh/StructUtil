package org.struct.spring.support;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.ClassMetadata;
import org.springframework.util.ClassUtils;
import org.struct.annotation.StructSheet;
import org.struct.spring.annotation.StructStore;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author TinyZ.
 * @version 2020.07.17
 */
public class ClassPathStructScanner extends ClassPathBeanDefinitionScanner {

    public ClassPathStructScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public ClassPathStructScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public ClassPathStructScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public ClassPathStructScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    public void registerFilters() {
        this.addExcludeFilter((mr, mrf) -> mr.getClassMetadata().getClassName().equals(StructMapper.class.getName()));
        //  1. StructMapper annotation.
        this.addIncludeFilter((mr, mrf) ->
                mr.getAnnotationMetadata().hasAnnotation(StructStore.class.getName())
                        && mr.getAnnotationMetadata().hasAnnotation(StructSheet.class.getName())
        );
        //  2. this class implement StructMapper interface.
        this.addIncludeFilter((mr, mrf) -> {
            ClassMetadata cm = mr.getClassMetadata();
            if (cm.isConcrete()) {
                if (cm.hasSuperClass()) {
                    if (StructMapper.class.getName().equals(cm.getSuperClassName())) {
                        return true;
                    }
                }
                ClassLoader classLoader = ClassPathStructScanner.class.getClassLoader();
                try {
                    Class<?> clzOfBean = ClassUtils.forName(cm.getClassName(), classLoader);
                    if (StructMapper.class.isAssignableFrom(clzOfBean)) {
                        return true;
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> holders = super.doScan(basePackages);
        return holders;
    }

    @Override
    public boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }

    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        GenericBeanDefinition gbd = (GenericBeanDefinition) definitionHolder.getBeanDefinition();
        if (!gbd.hasBeanClass()) {
            try {
                gbd.resolveBeanClass(ClassPathStructScanner.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        if (org.struct.spring.support.StructStore.class.isAssignableFrom(gbd.getBeanClass())) {
            int modifiers = gbd.getBeanClass().getModifiers();
            if (Modifier.isAbstract(modifiers)
                    || Modifier.isInterface(modifiers)) {
                return;
            }
            //  register user custom's struct mapper.
            super.registerBeanDefinition(definitionHolder, registry);
        } else {
            //  Generate mapper's bean definition
            String mapperBeanName = definitionHolder.getBeanName() + org.struct.spring.support.StructStore.class.getSimpleName();

            AnnotatedGenericBeanDefinition mbd = new AnnotatedGenericBeanDefinition(StructMapper.class);
            mbd.setRole(BeanDefinition.ROLE_APPLICATION);
            mbd.setScope(BeanDefinition.SCOPE_SINGLETON);
            mbd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
            ConstructorArgumentValues cavs = new ConstructorArgumentValues();
            cavs.addIndexedArgumentValue(0, gbd.getBeanClass());
            mbd.getConstructorArgumentValues().addArgumentValues(cavs);

            AnnotationConfigUtils.processCommonDefinitionAnnotations(mbd);
            BeanDefinitionHolder mapperDefinitionHolder = new BeanDefinitionHolder(mbd, mapperBeanName);
            super.registerBeanDefinition(mapperDefinitionHolder, registry);
        }
    }


}