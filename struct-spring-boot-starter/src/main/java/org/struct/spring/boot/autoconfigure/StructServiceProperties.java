package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author TinyZ.
 * @version 2020.07.09
 */
@ConfigurationProperties(prefix = StarterConstant.STRUCT_MAPPER_SERVICE)
public class StructServiceProperties {

    /**
     * Lazy load struct data before user use it.
     *
     * @see org.struct.spring.support.StructStore#initialize()
     */
    private boolean lazyLoad = false;


}
