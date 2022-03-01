package org.struct.spring.support;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

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
        Field field = ReflectionUtils.findField(bean.getClass(), fieldName);
        if (field != null) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return ReflectionUtils.getField(field, bean);
        }
        throw new RuntimeException("unknown bean field:" + fieldName + ".");
    }
}
