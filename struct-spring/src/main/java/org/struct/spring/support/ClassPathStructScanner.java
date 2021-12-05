/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

package org.struct.spring.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.ClassMetadata;
import org.springframework.util.ClassUtils;
import org.struct.annotation.StructSheet;
import org.struct.spring.annotation.AutoStruct;
import org.struct.spring.annotation.StructStoreOptions;

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
        this.addExcludeFilter((mr, mrf) ->
                !mr.getClassMetadata().isConcrete()
                        || mr.getClassMetadata().getClassName().equals(MapStructStore.class.getName())
                        || mr.getClassMetadata().getClassName().equals(ListStructStore.class.getName())
        );
        //  1. AutoStruct annotation.
        this.addIncludeFilter((mr, mrf) ->
                mr.getAnnotationMetadata().hasAnnotation(AutoStruct.class.getName())
                        && mr.getAnnotationMetadata().hasAnnotation(StructSheet.class.getName())
        );
        //  2. this class implement StructStore interface.
        this.addIncludeFilter((mr, mrf) -> {
            ClassMetadata cm = mr.getClassMetadata();
            if (cm.isConcrete()) {
                if (cm.hasSuperClass()) {
                    if (AbstractStructStore.class.getName().equals(cm.getSuperClassName())) {
                        return true;
                    }
                }
                ClassLoader classLoader = ClassPathStructScanner.class.getClassLoader();
                try {
                    Class<?> clzOfBean = ClassUtils.forName(cm.getClassName(), classLoader);
                    if (StructStore.class.isAssignableFrom(clzOfBean)) {
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
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
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
        if (StructStore.class.isAssignableFrom(gbd.getBeanClass())) {
            int modifiers = gbd.getBeanClass().getModifiers();
            if (Modifier.isAbstract(modifiers)
                    || Modifier.isInterface(modifiers)) {
                //  ignore abstract class and interface.
                return;
            }
            //  register custom struct store.
            Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(gbd.getBeanClass(), StructStore.class);
            gbd.getPropertyValues().add(StructConstant.CLZ_OF_BEAN, typeArguments[1]);
            this.handleStructStoreProperty(gbd,
                    AnnotationUtils.findAnnotation(gbd.getBeanClass(), AutoStruct.class),
                    AnnotationUtils.findAnnotation(gbd.getBeanClass(), StructStoreOptions.class)
            );
            super.registerBeanDefinition(definitionHolder, registry);
        } else {
            //  Generate struct store by struct's bean definition
            String mapperBeanName = definitionHolder.getBeanName() + StructStore.class.getSimpleName();
            AnnotatedGenericBeanDefinition mbd = this.generateStructStoreBeanDefinition(gbd);
            AnnotationConfigUtils.processCommonDefinitionAnnotations(mbd);
            BeanDefinitionHolder mapperDefinitionHolder = new BeanDefinitionHolder(mbd, mapperBeanName);
            super.registerBeanDefinition(mapperDefinitionHolder, registry);
        }
    }

    AnnotatedGenericBeanDefinition generateStructStoreBeanDefinition(GenericBeanDefinition gbd) {
        Class<?> clzOfStore = null;
        AutoStruct anno = AnnotationUtils.findAnnotation(gbd.getBeanClass(), AutoStruct.class);
        if (anno != null) {
            int modifiers;
            if (anno.clzOfStore() != StructStore.class) {
                //  custom struct store
                modifiers = anno.clzOfStore().getModifiers();
                if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                    throw new IllegalArgumentException("the struct store class:{" + anno.clzOfStore() + "} is illegal. holder:"
                            + gbd.getBeanClassName());
                }
                clzOfStore = anno.clzOfStore();
            } else {
                //  nested MapStructStore or ListStructStore
                if (!anno.keyResolverBeanName().isEmpty()) {
                    clzOfStore = MapStructStore.class;
                }
                if (StructKeyResolver.class != anno.keyResolverBeanClass()) {
                    modifiers = anno.keyResolverBeanClass().getModifiers();
                    if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                        throw new IllegalArgumentException("the key resolver class:{" + anno.clzOfStore() + "} is illegal. holder:"
                                + gbd.getBeanClassName());
                    }
                    clzOfStore = MapStructStore.class;
                }
            }
        }
        if (clzOfStore == null) {
            clzOfStore = ListStructStore.class;
        }
        AnnotatedGenericBeanDefinition mbd = new AnnotatedGenericBeanDefinition(clzOfStore);
        mbd.setRole(BeanDefinition.ROLE_APPLICATION);
        mbd.setScope(BeanDefinition.SCOPE_SINGLETON);
        mbd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        //  notice: the one param clzOfBean's constructor must be existed.
        ConstructorArgumentValues cavs = new ConstructorArgumentValues();
        cavs.addIndexedArgumentValue(0, gbd.getBeanClass());
        mbd.getConstructorArgumentValues().addArgumentValues(cavs);
        this.handleStructStoreProperty(mbd, anno, AnnotationUtils.findAnnotation(gbd.getBeanClass(), StructStoreOptions.class));
        return mbd;
    }

    void handleStructStoreProperty(GenericBeanDefinition gbd, AutoStruct anno, StructStoreOptions annoOptions) {
        MutablePropertyValues propertyValues = gbd.getPropertyValues();
        if (null != anno && MapStructStore.class.isAssignableFrom(gbd.getBeanClass())) {
            if (!anno.keyResolverBeanName().isEmpty()) {
                propertyValues.add(StructConstant.KEY_RESOLVER_BEAN_NAME, anno.keyResolverBeanName());
            }
            Class<? extends StructKeyResolver> clzOfKrb = anno.keyResolverBeanClass();
            if ((StructKeyResolver.class != clzOfKrb
                    && !Modifier.isAbstract(clzOfKrb.getModifiers())
                    && !Modifier.isInterface(clzOfKrb.getModifiers()))) {
                propertyValues.add(StructConstant.KEY_RESOLVER_BEAN_CLASS, clzOfKrb);
            }
        }
        if (annoOptions != null) {
            propertyValues.add(StructConstant.KEY_OPTIONS, Options.generate(annoOptions));
        }
    }

}