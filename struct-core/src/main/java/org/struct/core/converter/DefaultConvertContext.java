package org.struct.core.converter;

import org.struct.core.SingleFieldDescriptor;

/**
 * @author TinyZ
 * @since 2025.05.21
 * @since 4.1
 */
public record DefaultConvertContext(
        Object structImpl,
        SingleFieldDescriptor descriptor
) implements ConvertContext {

}
