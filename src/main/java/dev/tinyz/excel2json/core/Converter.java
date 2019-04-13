package dev.tinyz.excel2json.core;

import java.util.function.Function;

public interface Converter<R> extends Function<Object, R> {
}
