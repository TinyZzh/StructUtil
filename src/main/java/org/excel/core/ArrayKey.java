package org.excel.core;

import java.util.Arrays;

/**
 * @author TinyZ.
 * @version 2019.04.06
 */
public final class ArrayKey {

    private final Object[] ary;

    public ArrayKey(Object[] ary) {
        this.ary = ary;
    }

    public Object[] getAry() {
        return ary;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ary);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayKey))
            return false;
        return Arrays.equals(ary, ((ArrayKey) obj).getAry());
    }

    @Override
    public String toString() {
        return Arrays.toString(ary);
    }
}
