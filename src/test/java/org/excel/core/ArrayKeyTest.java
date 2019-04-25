package org.excel.core;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayKeyTest {

    @Test
    public void testEquals() {
        ArrayKey ak1 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak2 = new ArrayKey(new Object[]{1, 2, 3});
        ArrayKey ak3 = new ArrayKey(new Object[]{1, 2});
        Assert.assertEquals(ak1, ak2);
        Assert.assertNotEquals(ak2, ak3);
        Assert.assertNotEquals(ak2, 1L);
    }

    @Test
    public void testToString() {
        String string = new ArrayKey(new Object[]{1, 2, 3}).toString();
        Assert.assertEquals("[1, 2, 3]", string);
    }
}