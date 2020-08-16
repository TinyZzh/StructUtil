package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldDescriptorTest {

    @Test
    public void constructor() {
        SingleFieldDescriptor descriptor = new SingleFieldDescriptor();
        new SingleFieldDescriptor("name", null, null, null, null, false, null);

        {
            Assertions.assertEquals(
                    new SingleFieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null),
                    new SingleFieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null)
            );
        }
        Assertions.assertNotNull(descriptor.toString());
    }


}