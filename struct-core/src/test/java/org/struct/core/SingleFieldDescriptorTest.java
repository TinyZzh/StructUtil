package org.struct.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

/**
 * @author TinyZ
 * @date 2022-05-11
 */
class SingleFieldDescriptorTest {

    @Test
    public void test() {
        RecordComponent rc = RecordBean.class.getRecordComponents()[0];
        SingleFieldDescriptor sfd = new SingleFieldDescriptor(rc, null, false);
        Assertions.assertEquals(new SingleFieldDescriptor(rc, null, false), sfd);
        sfd.setReference(float.class);
        //  getAggregateType
        sfd.setAggregateType(Object.class);
        Assertions.assertEquals(Object.class, sfd.getAggregateType());
        //  resolveAggregateWorkerType
        Assertions.assertEquals(float.class, sfd.resolveAggregateWorkerType());
        sfd.setAggregateType(int.class);
        Assertions.assertEquals(int.class, sfd.resolveAggregateWorkerType());
        //  isReferenceField
        Assertions.assertTrue(sfd.isReferenceField());
        //  isAggregateField
        Assertions.assertFalse(sfd.isAggregateField());
        sfd.setAggregateBy("1");
        Assertions.assertTrue(sfd.isAggregateField());
        //  setFieldValue
        Assertions.assertThrows(UnsupportedOperationException.class, () -> sfd.setFieldValue(1, 1));
    }

    record RecordBean(int id) {

    }

}