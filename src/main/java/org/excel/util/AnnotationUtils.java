/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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

package org.excel.util;

import java.lang.annotation.Annotation;

public abstract class AnnotationUtils {

    private AnnotationUtils() {
        //  no-op
    }

    public static <A extends Annotation> A findAnnotation(final Class<A> targetAnnotation, final Class<?> annotatedType) {
        A foundAnnotation = annotatedType.getAnnotation(targetAnnotation);
        if (foundAnnotation == null) {
            for (Annotation annotation : annotatedType.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(targetAnnotation)) {
                    foundAnnotation = annotationType.getAnnotation(targetAnnotation);
                    break;
                }
            }
        }
        return foundAnnotation;
    }
}
