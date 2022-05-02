package org.struct.spring.support;

import org.struct.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

/**
 * bean's{#id} field's key resolver.
 *
 * @author TinyZ.
 * @date 2022-03-01.
 */
public class MapKeyFieldResolver implements StructKeyResolver<Object, Object> {

    public final String fieldName;

    public MapKeyFieldResolver(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Object resolve(Object bean) throws RuntimeException {
        Optional<MethodHandle> mh = Reflects.lookupFieldGetter(bean.getClass(), this.fieldName);
        return mh.map(m -> {
            try {
                return m.invoke(bean);
            } catch (Throwable e) {
                throw new RuntimeException("unknown bean field:" + fieldName + ".");
            }
        }).orElse(null);
    }
}
