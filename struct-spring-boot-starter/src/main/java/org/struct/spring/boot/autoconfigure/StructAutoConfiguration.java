package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConditionalOnProperty(prefix = StructConstant.STRUCT_UTIL, name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(StructProperties.class)
public class StructAutoConfiguration {



}
