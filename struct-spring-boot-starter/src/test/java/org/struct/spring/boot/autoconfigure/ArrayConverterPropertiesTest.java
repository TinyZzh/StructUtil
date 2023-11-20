package org.struct.spring.boot.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ
 * @date 2023-05-22
 */
class ArrayConverterPropertiesTest {

    @Test
    public void test() {
        ArrayConverterProperties prop1 = new ArrayConverterProperties();
        prop1.setStringSeparator(":");
        prop1.setStringTrim(false);
        prop1.setIgnoreBlank(false);
        Assertions.assertEquals(prop1, prop1);
        Assertions.assertNotEquals(prop1, null);
        Assertions.assertNotEquals(prop1, new Object());

        ArrayConverterProperties prop2 = new ArrayConverterProperties();
        prop2.setStringSeparator(":");
        prop2.setStringTrim(false);
        prop2.setIgnoreBlank(false);

        Assertions.assertEquals(prop1, prop2);
        {
            prop2.setStringSeparator(",");
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setStringSeparator(":");
        }
        {
            prop2.setStringTrim(true);
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setStringTrim(false);
        }
        {
            prop2.setIgnoreBlank(true);
            Assertions.assertNotEquals(prop1, prop2);
            prop2.setIgnoreBlank(false);
        }

        Assertions.assertEquals(prop1.toString(), prop2.toString());
        Assertions.assertEquals(prop1.hashCode(), prop2.hashCode());
    }

}