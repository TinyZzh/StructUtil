package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author TinyZ
 * @since 2025.05.22
 */
public class StructInternalTest {

    @Test
    public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.setProperty("key", "data");
        {
            Consumer<String> consumer = Mockito.mock(Consumer.class);
            Method method = StructInternal.class.getDeclaredMethod("handleProperty", String.class, Consumer.class);
            method.invoke(StructInternal.class, "key", consumer);
            Mockito.verify(consumer, Mockito.times(1)).accept(Mockito.eq("data"));
        }
        {
            Consumer<String> consumer = Mockito.mock(Consumer.class);
            Method method = StructInternal.class.getDeclaredMethod("handleProperty", String.class, Consumer.class);
            method.invoke(StructInternal.class, "", consumer);
            Mockito.verify(consumer, Mockito.never()).accept(Mockito.anyString());
        }
        {
            Consumer<String> consumer = Mockito.mock(Consumer.class);
            Mockito.doThrow(new UnsupportedOperationException()).when(consumer).accept(Mockito.anyString());
            Method method = StructInternal.class.getDeclaredMethod("handleProperty", String.class, Consumer.class);
            method.invoke(StructInternal.class, "", consumer);
            Mockito.verify(consumer, Mockito.never()).accept(Mockito.anyString());
        }
    }

    @Test
    public void testArrayKey() {
        Assertions.assertEquals(new ArrayKey(new Object[]{1, 2}), new ArrayKey(new Object[]{1, 2}));
        new ArrayKey(new Object[]{1, 2}).toString();
        new ArrayKey(new Object[]{1, 2}).hashCode();
    }

    @Test
    public void testFieldDescriptor() {
        FieldDescriptor descriptor = new FieldDescriptor("fd") {
        };
        descriptor.toString();
        Assertions.assertEquals(descriptor, descriptor);
        Assertions.assertNotEquals(descriptor, null);
        Assertions.assertNotEquals(descriptor, new int[]{1});
    }

    @Test
    public void testOptionalDescriptor() {
        OptionalDescriptor descriptor = new OptionalDescriptor();
        descriptor.toString();
        Assertions.assertEquals(descriptor, descriptor);
        Assertions.assertNotEquals(descriptor, null);
        Assertions.assertNotEquals(descriptor, new int[]{1});
        Assertions.assertEquals(descriptor, new OptionalDescriptor());
    }
}