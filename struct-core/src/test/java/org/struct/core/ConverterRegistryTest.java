package org.struct.core;

import org.junit.Assert;
import org.junit.Test;
import org.struct.core.converter.Converter;

public class ConverterRegistryTest {

    @Test
    public void lookupOrDefault() {
        final int num = 9999;
        Object obj = ConverterRegistry.lookupOrDefault(A.class, MyConverter.class, num)
                .convert("x", int.class);
        Assert.assertEquals(num, obj);
    }

    @Test
    public void registerConverterClz() {
        final int num = 9999;
        ConverterRegistry.register(A.class, MyConverter.class, num);
        Object obj = ConverterRegistry.lookup(A.class)
                .convert("x", int.class);
        Assert.assertEquals(num, obj);
    }

    public static class A {
        public String content;

        public A(String content) {
            this.content = content;
        }
    }

    public static class MyConverter implements Converter {

        private final int val;

        public MyConverter(int val) {
            this.val = val;
        }

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            //  do nothing and return val.
            return this.val;
        }
    }

    @Test
    public void registerConverter() {
        String content = "x";
        ConverterRegistry.register(B.class, new Converter() {
            @Override
            public Object convert(Object originValue, Class<?> targetType) {
                return new B(String.valueOf(originValue));
            }
        });
        B obj = (B) ConverterRegistry.lookup(B.class).convert(content, String.class);
        Assert.assertEquals(content, obj.content);
    }

    public static class B {

        public String content;

        public B(String content) {
            this.content = content;
        }
    }
}