/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

package org.struct.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TinyZ.
 * @version 2019.04.27
 */
public class AnnotationUtilsTest {

    @Test
    public void test() {
        Assertions.assertNull(AnnotationUtils.findAnnotation(Test.class, ClassA.class));
        Assertions.assertNotNull(AnnotationUtils.findAnnotation(Parent.class, ClassA.class));
        Assertions.assertNotNull(AnnotationUtils.findAnnotation(Parent.class, ClassB.class));

    }

    @Children
    public static class ClassB {
    }

    @Parent
    public static class ClassA {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface Parent {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Parent
    public @interface Children {

    }

}