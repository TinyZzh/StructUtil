package org.struct.spring.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.struct.annotation.StructSheet;
import org.struct.spring.support.ClassPathStructScanner;
import org.struct.spring.support.StructMapperService;
import org.struct.spring.support.StructConfig;
import org.struct.spring.support.StructStore;
import org.struct.support.FileWatcherService;
import org.struct.util.WorkerUtil;

import java.io.IOException;
import java.util.List;


/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConditionalOnProperty(prefix = StarterConstant.STRUCT_UTIL, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Configuration
@EnableConfigurationProperties({StructProperties.class, StructServiceProperties.class})
public class StructAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructAutoConfiguration.class);

    @ConditionalOnMissingBean()
    @Bean()
    public StructConfig structConfig(StructProperties properties) {
        StructConfig config = new StructConfig();
        config.setWorkspace(properties.getWorkspace());
        config.setLazyLoad(properties.isLazyLoad());
        config.setMonitorFileChange(properties.isMonitorFileChange());
        config.setScheduleInitialDelay(properties.getScheduleInitialDelay());
        config.setScheduleDelay(properties.getScheduleDelay());
        config.setScheduleTimeUnit(properties.getScheduleTimeUnit());
        return config;
    }

    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.STRUCT_MAPPER_SERVICE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
    @Bean()
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public StructMapperService structMapperService(StructConfig config) {
        StructMapperService service = new StructMapperService(config);
        return service;
    }

    @ConditionalOnMissingBean()
    @ConditionalOnProperty(prefix = StarterConstant.STRUCT_UTIL_MONITOR_FILE_CHANGE, name = StarterConstant.ENABLE, havingValue = "true", matchIfMissing = true)
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
                    StructSheet annotation = AnnotationUtils.findAnnotation(store.clzOfBean(), StructSheet.class);
                    fws.registerHook(WorkerUtil.resolveFilePath(config.getWorkspace(), annotation), store::reload);
                });
        fws.bootstrap();
        return fws;
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ApplicationContextAware {

        private BeanFactory beanFactory;
        private ApplicationContext applicationContext;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                LOGGER.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }
            LOGGER.debug("Searching for mappers annotated with @Mapper");
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);

            ClassPathStructScanner scanner = new ClassPathStructScanner(registry, false);
            scanner.registerFilters();
            scanner.setConfig(applicationContext.getBean(StructConfig.class));
            scanner.doScan(StringUtils.toStringArray(packages));
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({StructMapperService.class})
    public static class StructMapperServiceNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
            LOGGER.debug("Not found configuration for StructMapperService.");
        }
    }


}
