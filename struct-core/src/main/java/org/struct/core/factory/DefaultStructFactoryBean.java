package org.struct.core.factory;

import org.struct.core.StructWorker;
import org.struct.spi.SPI;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
@SPI(name = "default", order = 0)
public class DefaultStructFactoryBean implements StructFactoryBean {

    @Override
    public <T> StructFactory newInstance(Class<T> clzOfStruct, StructWorker<T> worker) {
        return new JdkStructFactory(clzOfStruct, worker);
    }
}
