package org.struct.core;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayKeyTest {

    @Test
    public void testEquals() {
        ArrayKey ak1 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak2 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak3 = new ArrayKey(new Object[]{1, 2});
        Assertions.assertEquals(ak1, ak2);
        Assertions.assertNotEquals(ak2, ak3);
        Assertions.assertNotEquals(ak2, 1L);
    }

    @Test
    public void testToString() {
        String string = new ArrayKey(new Object[]{1, 2, 3}).toString();
        Assertions.assertEquals("[1, 2, 3]", string);
    }
}