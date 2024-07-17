/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

package org.struct.spring.boot.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.struct.core.StructConfig;
import org.struct.spring.boot.autoconfigure.StructAutoConfiguration.AutoConfiguredMapperScannerRegistrar;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
@SpringBootTest(classes = {StructAutoConfiguration.class})
class StructAutoConfigurationTest {

    @Test
    public void testStructConfig() {
        StructAutoConfiguration configuration = new StructAutoConfiguration();
        StructProperties properties = new StructProperties();
        ArrayConverterProperties arrayConverterProperties = new ArrayConverterProperties();
        properties.setArrayConverter(arrayConverterProperties);
        StructConfig config = configuration.structConfig(properties);

        Assertions.assertEquals(config.isStructRequiredDefault(), properties.isStructRequiredDefault());
        Assertions.assertEquals(config.isIgnoreEmptyRow(), properties.isIgnoreEmptyRow());

        Assertions.assertEquals(config.getArrayConverterStringSeparator(), arrayConverterProperties.getStringSeparator());
        Assertions.assertEquals(config.isArrayConverterIgnoreBlank(), arrayConverterProperties.isIgnoreBlank());
        Assertions.assertEquals(config.isArrayConverterStringTrim(), arrayConverterProperties.isStringTrim());
    }

    @Test
    public void testAutoConfiguredMapperScannerRegistrar() {
        AutoConfiguredMapperScannerRegistrar registrar = new AutoConfiguredMapperScannerRegistrar();
        BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
        Mockito.doReturn(true).when(beanFactory).containsBean(anyString());
        Mockito.doReturn(Collections.singleton("xx")).when(beanFactory).getBean(anyString());
        registrar.setBeanFactory(beanFactory);
        registrar.setResourceLoader(Mockito.mock(ResourceLoader.class));

        registrar.registerBeanDefinitions(null, null);

    }

}