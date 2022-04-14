package org.struct.core.factory;

import org.struct.core.StructWorker;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
public interface StructFactoryBean {

    <T> StructFactory newInstance(Class<T> clzOfStruct, StructWorker<T> worker);
}
