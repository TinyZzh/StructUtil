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

import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.struct.spring.support.StructStoreService;

import java.util.Map;

/**
 * @author TinyZ.
 * @date 2021-06-30.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StructStoreService.class)
@ConditionalOnBean({StructStoreService.class, HealthEndpoint.class})
@ConditionalOnEnabledHealthIndicator("struct")
public class StructStoreServiceHealthContributorAutoConfiguration extends
        CompositeHealthContributorConfiguration<StructStoreServiceHealthIndicator, StructStoreService> {

    @Bean
    @ConditionalOnMissingBean(name = {"structStoreServiceHealthIndicator", "structHealthContributor"})
    public HealthContributor structHealthContributor(Map<String, StructStoreService> services) {
        return createContributor(services);
    }
}
