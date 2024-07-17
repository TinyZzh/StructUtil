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

package org.struct.spring.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author TinyZ
 * @date 2022-03-01
 */
class MapKeyFieldResolverTest {

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.a = "a";
        bean.b = 2;
        bean.c = "c";
        bean.key = new Key();
        bean.key.k = 1;
        Assertions.assertEquals(bean.a, new MapKeyFieldResolver("a").resolve(bean));
        Assertions.assertEquals(bean.b, new MapKeyFieldResolver("b").resolve(bean));
        Assertions.assertEquals(bean.c, new MapKeyFieldResolver("c").resolve(bean));
        Assertions.assertEquals(bean.d, new MapKeyFieldResolver("d").resolve(bean));
        Assertions.assertEquals(bean.key, new MapKeyFieldResolver("key").resolve(bean));
    }

    public static class Bean {

        private String a;
        private int b;
        private String c;
        public String d;
        private Key key;

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }
    }

    public static class Key {
        private int k;

        public Key() {
        }

        public Key(int k) {
            this.k = k;
        }
    }

    @Test
    public void testRecord() {
        RecordData rc = new RecordData(1, "b", new Key(2));
        Assertions.assertEquals(rc.a, new MapKeyFieldResolver("a").resolve(rc));
        Assertions.assertEquals(rc.b, new MapKeyFieldResolver("b").resolve(rc));
        Assertions.assertEquals(rc.c, new MapKeyFieldResolver("c").resolve(rc));
        Assertions.assertNull(new MapKeyFieldResolver("d").resolve(rc));
    }

    record RecordData(int a, String b, Key c) {
    }

}