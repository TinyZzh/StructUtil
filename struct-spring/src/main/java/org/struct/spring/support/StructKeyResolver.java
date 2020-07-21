package org.struct.spring.support;

/**
 * @author TinyZ.
 * @version 2020.07.17
 */
@FunctionalInterface
public interface StructKeyResolver<K, B> {

    K resolve(B bean);
}
