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

package org.struct.spring.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.struct.core.StructConfig;
import org.struct.core.StructDescriptor;
import org.struct.core.converter.ArrayConverter;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.spring.support.ClassPathStructScanner;
import org.struct.spring.support.StructScannerRegistrar;
import org.struct.spring.support.StructStore;
import org.struct.spring.support.StructStoreConfig;
import org.struct.spring.support.StructStoreService;
import org.struct.support.FileWatcherService;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;


/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConditionalOnProperty(prefix = StarterConstant.STRUCT_STORE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1000)
@Configuration
@EnableConfigurationProperties({StructProperties.class, StructServiceProperties.class})
public class StructAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public StructConfig structConfig(StructProperties properties) {
        StructConfig config = new StructConfig();
        config.setStructRequiredDefault(properties.isStructRequiredDefault());
        config.setIgnoreEmptyRow(properties.isIgnoreEmptyRow());

        //  set array converter's properties
        ArrayConverterProperties acp = properties.getArrayConverter();
        if (null != acp) {
            config.setArrayConverterStringSeparator(acp.getStringSeparator());
            config.setArrayConverterStringTrim(acp.isStringTrim());
            config.setArrayConverterIgnoreBlank(acp.isIgnoreBlank());

            Converter converter = ConverterRegistry.lookup(Array.class);
            if (converter instanceof ArrayConverter) {
                ((ArrayConverter) converter).setSeparator(acp.getStringSeparator());
                ((ArrayConverter) converter).setStrTrim(acp.isStringTrim());
                ((ArrayConverter) converter).setIgnoreBlank(acp.isIgnoreBlank());
            }
        }

        return config;
    }

    @ConditionalOnMissingBean()
    @Bean()
    public StructStoreConfig structStoreConfig(StructServiceProperties properties) {
        StructStoreConfig config = new StructStoreConfig();
        config.setWorkspace(properties.getWorkspace());
        config.setLazyLoad(properties.isLazyLoad());
        config.setWatchFile(properties.isWatchFile());
        config.setScheduleInitialDelay(properties.getScheduleInitialDelay());
        config.setScheduleDelay(properties.getScheduleDelay());
        config.setScheduleTimeUnit(properties.getScheduleTimeUnit());
        config.setBanner(properties.isBanner());
        return config;
    }

    /**
     * {@link StructStoreService} implement the {@link org.springframework.beans.factory.config.BeanPostProcessor} interface.
     * the method must be <strong>static</strong> method to ignore the exception warning in {@link org.springframework.context.support.PostProcessorRegistrationDelegate.BeanPostProcessorChecker}
     */
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.SERVICE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
    @Bean()
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public static StructStoreService structStoreService() {
        return new StructStoreService();
    }

    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.WATCH_FILE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
    @Bean()
    public FileWatcherService fileWatcherService(StructStoreConfig config, List<StructStore> storeList) throws IOException {
        File file = new File(config.getWorkspace());
        if (!file.exists() && !file.mkdir()) {
            throw new IllegalArgumentException("the file watcher service mkdir failure. path:" + config.getWorkspace());
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("the workspace must be directory. workspace:" + config.getWorkspace());
        }
        FileWatcherService fws = FileWatcherService.newBuilder()
                .setScheduleInitialDelay(config.getScheduleInitialDelay())
                .setScheduleDelay(config.getScheduleDelay())
                .setScheduleTimeUnit(config.getScheduleTimeUnit())
                .build();
        fws.registerAll(file.toPath());
        storeList.parallelStream()
                .forEach(store -> {
                    //  register reload hook
                    StructDescriptor descriptor = new StructDescriptor(store.clzOfBean());
                    fws.registerHook(WorkerUtil.resolveFilePath(config.getWorkspace(), descriptor.getFileName()), store::reload);
                });
        fws.bootstrap();
        return fws;
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                LOGGER.info("Could not determine auto-configuration package, automatic struct store scanning disabled.");
                return;
            }
            LOGGER.debug("Searching for struct store");
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);

            ClassPathStructScanner scanner = new ClassPathStructScanner(registry, false);
            if (this.resourceLoader != null) {
                scanner.setResourceLoader(this.resourceLoader);
            }
            scanner.registerFilters();
            scanner.doScan(StringUtils.toStringArray(packages));
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({StructScannerRegistrar.class})
    public static class StructMapperServiceNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
            LOGGER.debug("Not found configuration for {}.", StructStoreService.class.getName());
        }
    }


}
