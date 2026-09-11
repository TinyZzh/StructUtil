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

package org.struct.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The utility edges: the places where a lookup simply finds nothing, or a numeric literal comes in
 * a notation nobody uses.
 *
 * @author TinyZ.
 */
class UtilBranchesTest {

    //  ------------------------------------------------------------------
    //  Reflects
    //  ------------------------------------------------------------------

    interface NoSuperType {
        String getX();
    }

    /**
     * An interface has no super class - the accessor walk has to stop on {@code null}.
     */
    @Test
    void testLookupAccessorOnInterface() {
        Assertions.assertNull(Reflects.lookupAccessor(NoSuperType.class, "x"));
        Assertions.assertTrue(Reflects.lookupFieldGetter(NoSuperType.class, "x").isEmpty());
        Assertions.assertTrue(Reflects.lookupFieldSetter(NoSuperType.class, "x").isEmpty());
    }

    /**
     * A getter / setter for a field that does not exist resolves to empty.
     */
    @Test
    void testLookupMissingField() {
        Assertions.assertTrue(Reflects.lookupFieldGetter(Bean.class, "nope").isEmpty());
        Assertions.assertTrue(Reflects.lookupFieldSetter(Bean.class, "nope").isEmpty());
        //  the accessors that do exist
        Assertions.assertTrue(Reflects.lookupFieldGetter(Bean.class, "a").isPresent());
        Assertions.assertTrue(Reflects.lookupFieldSetter(Bean.class, "a").isPresent());
        //  a record's accessor
        Assertions.assertTrue(Reflects.lookupFieldGetter(RecordBean.class, "a").isPresent());
    }

    /**
     * A record has no field setter at all.
     */
    @Test
    void testLookupSetterOnRecord() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> Reflects.lookupFieldSetter(RecordBean.class, "a"));
    }

    static class Bean {
        public String a;
    }

    record RecordBean(String a) {
    }

    /**
     * The type compatibility check has to fall through to the primitive wrapper mapping.
     */
    @Test
    void testIsAssignable() {
        //  direct
        Assertions.assertTrue(Reflects.isAssignable(CharSequence.class, String.class));
        //  primitive lhs vs wrapper rhs
        Assertions.assertTrue(Reflects.isAssignable(int.class, Integer.class));
        Assertions.assertFalse(Reflects.isAssignable(int.class, Long.class));
        //  wrapper lhs vs primitive rhs -> resolved through the primitive -> wrapper mapping
        Assertions.assertTrue(Reflects.isAssignable(Integer.class, int.class));
        //  ... but the resolved wrapper still has to be assignable
        Assertions.assertFalse(Reflects.isAssignable(Long.class, int.class));
        //  neither side is primitive -> nothing to resolve
        Assertions.assertFalse(Reflects.isAssignable(String.class, Integer.class));
    }

    /**
     * A {@code ref} that points at a class without {@code @StructSheet} contributes no file name.
     */
    @Test
    void testResolveStructRelatedFileName() {
        //  no reference fields at all
        Assertions.assertTrue(Reflects.resolveStructRelatedFileName(Bean.class).isEmpty());

        //  a reference field pointing at a class without @StructSheet
        Assertions.assertTrue(Reflects.resolveStructRelatedFileName(RefNoSheet.class).isEmpty());

        //  ... and one pointing at a class whose @StructSheet has an empty file name
        Assertions.assertTrue(Reflects.resolveStructRelatedFileName(RefEmptyFileName.class).isEmpty());

        //  a resolvable one
        List<String> names = Reflects.resolveStructRelatedFileName(RefOk.class);
        Assertions.assertEquals(List.of("ok.json"), names);
    }

    static class RefNoSheet {
        @StructField(ref = Bean.class)
        public String ref;
    }

    @StructSheet(fileName = "")
    static class EmptyFileName {
    }

    static class RefEmptyFileName {
        @StructField(ref = EmptyFileName.class)
        public String ref;
    }

    @StructSheet(fileName = "ok.json")
    static class Ok {
    }

    static class RefOk {
        @StructField(ref = Ok.class)
        public String ref;
    }

    //  ------------------------------------------------------------------
    //  ConverterUtil
    //  ------------------------------------------------------------------

    /**
     * The radix specifiers: uppercase {@code 0X}, the {@code #} shorthand and octal.
     */
    @Test
    void testDecodeBigInteger() {
        Assertions.assertEquals(31L, ConverterUtil.decodeBigInteger("0x1F").longValue());
        Assertions.assertEquals(31L, ConverterUtil.decodeBigInteger("0X1F").longValue());
        Assertions.assertEquals(31L, ConverterUtil.decodeBigInteger("#1F").longValue());
        //  a leading zero is octal: 017 == 15
        Assertions.assertEquals(15L, ConverterUtil.decodeBigInteger("017").longValue());
        //  negative, and a plain decimal
        Assertions.assertEquals(-31L, ConverterUtil.decodeBigInteger("-0x1F").longValue());
        Assertions.assertEquals(31L, ConverterUtil.decodeBigInteger("31").longValue());
    }

    //  ------------------------------------------------------------------
    //  AnnotationUtils
    //  ------------------------------------------------------------------

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @StructSheet(fileName = "meta.json")
    @interface MetaAnnotated {
    }

    @MetaAnnotated
    static class MetaCarrier {
    }

    /**
     * The annotation may be present as a meta annotation on another annotation.
     */
    @Test
    void testFindAnnotationViaMetaAnnotation() {
        StructSheet anno = AnnotationUtils.findAnnotation(StructSheet.class, MetaCarrier.class);
        Assertions.assertNotNull(anno);
        Assertions.assertEquals("meta.json", anno.fileName());

        //  a class that simply has none
        Assertions.assertNull(AnnotationUtils.findAnnotation(StructSheet.class, Bean.class));
    }

    //  ------------------------------------------------------------------
    //  WorkerUtil
    //  ------------------------------------------------------------------

    /**
     * Concrete collection / map types are instantiated, the abstract ones fall back.
     */
    @Test
    void testNewListOnlyAndNewMap() throws Exception {
        //  a concrete list type
        Assertions.assertTrue(WorkerUtil.newListOnly(java.util.ArrayList.class) instanceof java.util.ArrayList);
        //  Set -> HashSet, anything else abstract -> ArrayList
        Assertions.assertTrue(WorkerUtil.newListOnly(Set.class) instanceof java.util.HashSet);
        Assertions.assertTrue(WorkerUtil.newListOnly(List.class) instanceof java.util.ArrayList);
        //  not a collection at all
        Assertions.assertThrows(IllegalArgumentException.class, () -> WorkerUtil.newListOnly(String.class));

        //  null and abstract maps fall back to HashMap
        Assertions.assertTrue(WorkerUtil.newMap(null) instanceof java.util.HashMap);
        Assertions.assertTrue(WorkerUtil.newMap(Map.class) instanceof java.util.HashMap);
        //  a concrete map type
        Assertions.assertTrue(WorkerUtil.newMap(java.util.TreeMap.class) instanceof java.util.TreeMap);
    }

    /**
     * Many threads racing on a fresh {@code Holder} - the supplier must run exactly once.
     */
    @Test
    void testHolderConcurrentGet() throws Exception {
        for (int round = 0; round < 20; round++) {
            java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
            WorkerUtil.Holder<String> holder = new WorkerUtil.Holder<>(() -> {
                calls.incrementAndGet();
                return "v";
            });
            int n = 4;
            java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
            java.util.Set<String> seen = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
            Thread[] threads = new Thread[n];
            for (int i = 0; i < n; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException ignored) {
                    }
                    seen.add(holder.get());
                });
                threads[i].start();
            }
            start.countDown();
            for (Thread t : threads) {
                t.join(5_000L);
            }
            Assertions.assertEquals(1, calls.get(), "the supplier must run exactly once");
            Assertions.assertEquals(1, seen.size());
        }
    }

    @Test
    void testHolderLazyValue() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        //  the value is created once, on first access
        Object holder = newHolder(calls);
        Assertions.assertNotNull(holder);
    }

    /**
     * {@code Holder#get()} is package private, so it is exercised through a tiny subclass in the
     * same package.
     */
    private static Object newHolder(java.util.concurrent.atomic.AtomicInteger calls) {
        WorkerUtil.Holder<String> holder = new WorkerUtil.Holder<>(() -> {
            calls.incrementAndGet();
            return "v";
        });
        String a = holder.get();
        String b = holder.get();
        Assertions.assertSame(a, b);
        Assertions.assertEquals(1, calls.get());
        return holder;
    }

    @Test
    void testLookupFieldGetterOptional() {
        Optional<?> empty = Reflects.lookupFieldGetter(Bean.class, "missing");
        Assertions.assertTrue(empty.isEmpty());
    }
}
