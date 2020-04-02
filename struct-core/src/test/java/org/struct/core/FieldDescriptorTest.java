package org.struct.core;

import org.junit.Assert;
import org.junit.Test;

public class FieldDescriptorTest {

    @Test
    public void constructor() {
        FieldDescriptor descriptor = new FieldDescriptor();
        new FieldDescriptor("name", null, null, null, null, false, null);

        {
            Assert.assertEquals(
                    new FieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null),
                    new FieldDescriptor("name", null, FieldDescriptorTest.class, new String[]{"1"}, new String[]{"1"}, false, null)
            );
        }
        Assert.assertNotNull(descriptor.toString());
    }


}