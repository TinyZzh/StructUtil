/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.struct.annotation.StructSheet;
import org.struct.spring.annotation.AutoStruct;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class ClassPathStructScannerTest {

    @Test
    public void testConstructors() {
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class)));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false, mock(Environment.class)));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false, mock(Environment.class), mock(ResourceLoader.class)));
    }

    @Test
    public void testGenerateStructStoreBeanDefinition() {
        ClassPathStructScanner scanner = new ClassPathStructScanner(mock(BeanDefinitionRegistry.class));
        GenericBeanDefinition gbd = mock(GenericBeanDefinition.class);
        doReturn(Clz.class).when(gbd).getBeanClass();
        doReturn("Clz").when(gbd).getBeanClassName();
        AnnotatedGenericBeanDefinition definition = scanner.generateStructStoreBeanDefinition(gbd);
        Assertions.assertNotNull(definition);
    }

    @AutoStruct(mapKey = "id", keyResolverBeanClass = MapKeyFieldResolver.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class Clz {

    }

    @Test
    public void testGenerateStructStoreBeanDefinitionWithStore() {
        ClassPathStructScanner scanner = new ClassPathStructScanner(mock(BeanDefinitionRegistry.class));
        GenericBeanDefinition gbd = mock(GenericBeanDefinition.class);
        doReturn(ClzA.class).when(gbd).getBeanClass();
        doReturn("ClzA").when(gbd).getBeanClassName();
        AnnotatedGenericBeanDefinition definition = scanner.generateStructStoreBeanDefinition(gbd);
        Assertions.assertNotNull(definition);
    }

    @AutoStruct(clzOfStore = ListStructStore.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class ClzA {

    }
}