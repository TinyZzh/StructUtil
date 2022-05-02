package org.struct.spring.support;

import org.struct.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

/**
 * {@link MapStructStore} map key field's value resolver.
 *
 * @author TinyZ.
 * @date 2022-03-01.
 */
public class MapKeyFieldResolver implements StructKeyResolver<Object, Object> {

    /**
     * The {@link MapStructStore}'s key field's name.
     */
    public final String fieldName;

    /**
     * Constructor for mapped field.
     *
     * @param fieldName the {@link MapStructStore} key field's name.
     */
    public MapKeyFieldResolver(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Get mapped field's value from bean.
     *
     * @param bean the struct bean instance.
     * @return the mapped field's value.
     * @throws RuntimeException the unknown bean field.
     */
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
