/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

package org.struct.core.converter;

import org.junit.Assert;
import org.junit.Test;

public class EnumConverterTest {

    @Test
    public void test() {
        EnumConverter converter = new EnumConverter();
        Assert.assertEquals(MyEnum.One, converter.convert(0, MyEnum.class));
        Assert.assertEquals(MyEnum.Two, converter.convert(1.0D, MyEnum.class));
        Assert.assertEquals(MyEnum.Three, converter.convert(2L, MyEnum.class));

        Assert.assertEquals(MyEnum.One, converter.convert("0", MyEnum.class));
        Assert.assertEquals(MyEnum.Two, converter.convert("1", MyEnum.class));
        Assert.assertEquals(MyEnum.Three, converter.convert("2", MyEnum.class));

        Assert.assertEquals(MyEnum.One, converter.convert("One", MyEnum.class));
        Assert.assertEquals(MyEnum.Two, converter.convert("Two", MyEnum.class));
        Assert.assertEquals(MyEnum.Three, converter.convert("Three", MyEnum.class));

        Assert.assertEquals(MyEnum.One, converter.convert("one", MyEnum.class));
        Assert.assertEquals(MyEnum.Two, converter.convert("two", MyEnum.class));
        Assert.assertEquals(MyEnum.Three, converter.convert("three", MyEnum.class));

        Assert.assertEquals("three", converter.convert("three", Integer.class));

        Assert.assertEquals(MyEnum.One, converter.convert(MyEnum.One, MyEnum.class));
        Assert.assertEquals(MyEnum.Two, converter.convert(MyEnum.Two, MyEnum.class));
        Assert.assertEquals(MyEnum.Three, converter.convert(MyEnum.Three, MyEnum.class));
        try {
            converter.convert("four", MyEnum.class);
        } catch (IllegalStateException e) {
            //  no-op
        } catch (Exception e) {
            Assert.fail("four");
        }
    }

    public enum MyEnum {
        One,
        Two,
        Three;

    }
}