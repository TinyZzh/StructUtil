/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
        SingleFieldDescriptor sfd = new SingleFieldDescriptor(rc, null);
        Assertions.assertEquals(sfd, sfd);
        Assertions.assertNotEquals(sfd, null);
        Assertions.assertNotEquals(sfd, new Object());
        Assertions.assertEquals(new SingleFieldDescriptor(rc, null), sfd);
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