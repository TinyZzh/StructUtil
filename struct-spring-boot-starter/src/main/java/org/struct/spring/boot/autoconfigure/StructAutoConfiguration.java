/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.struct.core.StructDescriptor;
import org.struct.spring.support.ClassPathStructScanner;
import org.struct.spring.support.StructConfig;
import org.struct.spring.support.StructScannerRegistrar;
import org.struct.spring.support.StructStore;
import org.struct.spring.support.StructStoreService;
import org.struct.support.FileWatcherService;
import org.struct.util.WorkerUtil;

import java.io.IOException;
import java.util.List;


/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConditionalOnProperty(prefix = StarterConstant.STRUCT_UTIL, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1000)
@Configuration
@EnableConfigurationProperties({StructProperties.class, StructServiceProperties.class})
public class StructAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructAutoConfiguration.class);

    @ConditionalOnMissingBean()
    @Bean()
    public StructConfig structConfig(StructProperties properties, StructServiceProperties serviceProperties) {
        StructConfig config = new StructConfig();
        config.setWorkspace(properties.getWorkspace());
        config.setLazyLoad(serviceProperties.isLazyLoad());
        config.setMonitorFileChange(serviceProperties.isMonitorFileChange());
        config.setScheduleInitialDelay(serviceProperties.getScheduleInitialDelay());
        config.setScheduleDelay(serviceProperties.getScheduleDelay());
        config.setScheduleTimeUnit(serviceProperties.getScheduleTimeUnit());
        return config;
    }

    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.SERVICE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
    @Bean()
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public StructStoreService structMapperService(StructConfig config) {
        StructStoreService service = new StructStoreService(config);
        return service;
    }

    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.MONITOR_FILE_CHANGE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
    @Bean()
    public FileWatcherService fileWatcherService(StructConfig config, List<StructStore> storeList) throws IOException {
        FileWatcherService fws = new FileWatcherService();
        fws.registerAll(config.getWorkspace())
                .setScheduleInitialDelay(config.getScheduleInitialDelay())
                .setScheduleDelay(config.getScheduleDelay())
                .setScheduleTimeUnit(config.getScheduleTimeUnit());
        storeList.parallelStream()
                .forEach(store -> {
                    //  register reload hook
                    StructDescriptor descriptor = new StructDescriptor(store.clzOfBean());
                    fws.registerHook(WorkerUtil.resolveFilePath(config.getWorkspace(), descriptor.getFileName()), store::reload);
                });
        fws.bootstrap();
        return fws;
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

        private BeanFactory beanFactory;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                LOGGER.debug("Could not determine auto-configuration package, automatic struct store scanning disabled.");
                return;
            }
            LOGGER.debug("Searching for struct store");
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);

            ClassPathStructScanner scanner = new ClassPathStructScanner(registry, false);
            scanner.registerFilters();
            scanner.doScan(StringUtils.toStringArray(packages));
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
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
