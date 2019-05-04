package org.excel.core;

import org.junit.Assert;
import org.junit.Test;

public class ConverterRegistryTest {

    @Test
    public void testRegister() {
        //  lambda
        ConverterRegistry.register(Integer.class, o -> 1);
        Assert.assertEquals(Integer.valueOf(1), ConverterRegistry.lookup(Integer.class).apply(1));
        //  anonymous class
        ConverterRegistry.register(Long.class, new Converter<Long>() {
            @Override
            public Long apply(Object o) {
                return 1L;
            }
        });
        Assert.assertEquals(Long.valueOf(1), ConverterRegistry.lookup(Long.class).apply(1));
        //  interface
        try {
            ConverterRegistry.register(A.class, IA.class);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  abstract class
        try {
            ConverterRegistry.register(A.class, AbstractA.class);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  anonymous class
        try {
            ConverterRegistry.register(A.class, new Converter<A>() {
                @Override
                public A apply(Object o) {
                    return null;
                }
            }.getClass());
        } catch (IllegalArgumentException e) {
            //  no-op
        } catch (Exception e) {
            Assert.fail();
        }
        ConverterRegistry.register(A.class, BeanAConverter.class);
        Assert.assertNull(ConverterRegistry.lookup(A.class).apply(null));
        //  Constructor throw exception
        try {
            ConverterRegistry.register(A.class, ExceptionA.class);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
    }

    @Test
    public void testLookupOrDefault() {
        ConverterRegistry.unregister(A.class);
        Converter<A> converter = ConverterRegistry.lookupOrDefault(A.class, BeanAConverter.class);
        Assert.assertNotNull(converter);
        A apply = converter.apply(new A());
        Assert.assertNull(apply);
        ConverterRegistry.lookupOrDefault(A.class, BeanAConverter.class);
    }

    @Test
    public void testLookupOrDefaultException() {
        ConverterRegistry.unregister(A.class);
        try {
            ConverterRegistry.lookupOrDefault(A.class, NestedConverter.class);
        } catch (Exception e) {

        }
    }

    public class A {

    }

    public abstract class AbstractA implements Converter<A> {

    }

    public interface IA extends Converter<A> {

    }

    public static class ExceptionA implements Converter<A> {
        public ExceptionA() {
            throw new IllegalArgumentException();
        }

        @Override
        public A apply(Object o) {
            return null;
        }
    }

    public static class BeanAConverter implements Converter<A> {

        @Override
        public A apply(Object o) {
            return null;
        }
    }

    /**
     * [wrong] nested class
     */
    public class NestedConverter implements Converter<A> {

        @Override
        public A apply(Object o) {
            return null;
        }
    }
}