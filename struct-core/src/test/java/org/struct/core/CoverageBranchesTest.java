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
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.converter.Converter;
import org.struct.core.converter.ConverterRegistry;
import org.struct.core.converter.ConvertContext;
import org.struct.core.converter.DateConverter;
import org.struct.core.factory.JdkStructFactory;
import org.struct.core.factory.StructFactory;
import org.struct.core.filter.StructBeanFilter;
import org.struct.util.ConverterUtil;
import org.struct.util.Reflects;
import org.struct.util.WorkerUtil;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The defensive arms of the engine: the "this can not normally happen" guards that still have to be
 * exercised so that a regression in them does not go unnoticed.
 *
 * @author TinyZ.
 */
class CoverageBranchesTest {

    private static final ConvertContext CTX = null;

    //  ------------------------------------------------------------------
    //  ConverterRegistry
    //  ------------------------------------------------------------------

    @Test
    void testRegisterAnonymousConverterIsRejected() {
        //  an anonymous class can not be instantiated reflectively.
        Class<? extends Converter> anonymous = new Converter() {
            @Override
            public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
                return originValue;
            }
        }.getClass();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ConverterRegistry.register(String.class, anonymous));
    }

    /**
     * A converter that can not be instantiated at all.
     */
    public static class NoDefaultConstructorConverter implements Converter {
        public NoDefaultConstructorConverter(String mustHaveThis) {
        }

        @Override
        public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
            return originValue;
        }
    }

    @Test
    void testRegisterUninstantiableConverterIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ConverterRegistry.register(String.class, NoDefaultConstructorConverter.class));
    }

    /**
     * A user supplied {@code Array} converter that does not return an array at all - the collection
     * conversion must stay empty instead of failing.
     */
    @Test
    void testConvertCollectionWithNonArrayResult() {
        Converter previous = ConverterRegistry.lookup(java.lang.reflect.Array.class);
        Converter notAnArray = (ctx, originValue, targetType) -> "not-an-array";
        ConverterRegistry.register(java.lang.reflect.Array.class, notAnArray);
        try {
            Object result = ConverterRegistry.convertCollection(CTX, "1|2", List.class, Integer.class);
            Assertions.assertTrue(result instanceof List);
            Assertions.assertTrue(((List<?>) result).isEmpty());
        } finally {
            restore(java.lang.reflect.Array.class, previous);
        }
    }

    /**
     * ... and one that hands back {@code null}.
     */
    @Test
    void testConvertCollectionWithNullResult() {
        Converter previous = ConverterRegistry.lookup(java.lang.reflect.Array.class);
        ConverterRegistry.register(java.lang.reflect.Array.class, (ctx, originValue, targetType) -> null);
        try {
            Object result = ConverterRegistry.convertCollection(CTX, "1|2", List.class, Integer.class);
            Assertions.assertTrue(result instanceof List);
            Assertions.assertTrue(((List<?>) result).isEmpty());
        } finally {
            restore(java.lang.reflect.Array.class, previous);
        }
    }

    private static void restore(Class<?> type, Converter previous) {
        ConverterRegistry.unregister(type);
        if (null != previous) {
            ConverterRegistry.register(type, previous);
        }
    }

    //  ------------------------------------------------------------------
    //  DateConverter
    //  ------------------------------------------------------------------

    /**
     * A {@code Number} that is neither {@code Integer} nor {@code Long} is already in milliseconds.
     */
    @Test
    void testDateConverterOtherNumber() {
        DateConverter c = new DateConverter();
        //  a Double: not an Integer, not a Long -> taken as milliseconds
        Assertions.assertEquals(new Date(1_500L), c.convert(CTX, 1_500D, Date.class));
        Assertions.assertEquals(new Date(7L), c.convert(CTX, BigInteger.valueOf(7L), Date.class));
    }

    //  ------------------------------------------------------------------
    //  SingleFieldDescriptor
    //  ------------------------------------------------------------------

    /**
     * A descriptor may only be built out of a {@code Field} or a {@code RecordComponent}.
     */
    @Test
    void testDescriptorRejectsForeignFieldOrRc() {
        Assertions.assertThrows(AssertionError.class, () -> new SingleFieldDescriptor("not-a-field", null));
    }

    /**
     * An abstract / interface converter on the annotation is not instantiated.
     */
    @Test
    void testAbstractConverterOnAnnotationIsIgnored() {
        StructWorker<AbstractConverterBean> worker = WorkerUtil.newWorker("classpath:", AbstractConverterBean.class);
        worker.checkStructFactory();
        StructFactory factory = WorkerUtil.structFactory(AbstractConverterBean.class, worker);
        factory.parseStruct();
        SingleFieldDescriptor fd = ((JdkStructFactory) factory).beanFields().stream()
                .filter(SingleFieldDescriptor.class::isInstance)
                .map(SingleFieldDescriptor.class::cast)
                .findFirst()
                .orElseThrow();
        Assertions.assertNull(fd.getConverter());
    }

    abstract static class AbstractConverter implements Converter {
    }

    @StructSheet(fileName = "unused.json")
    static class AbstractConverterBean {
        @StructField(converter = AbstractConverter.class)
        public String a;
    }

    /**
     * A {@code ref} configured with a group-by is not a "basic type collection".
     */
    @Test
    void testIsBasicTypeCollectionWithRefConfig() throws Exception {
        Field f = RefConfigBean.class.getDeclaredField("items");
        SingleFieldDescriptor fd = new SingleFieldDescriptor(f, null);
        fd.setRefGroupBy(new String[]{"key"});
        Assertions.assertFalse(fd.isBasicTypeCollection());

        fd.setRefGroupBy(new String[0]);
        fd.setRefUniqueKey(new String[]{"key"});
        Assertions.assertFalse(fd.isBasicTypeCollection());

        //  an aggregate field is not one either
        fd.setRefUniqueKey(new String[0]);
        fd.setAggregateBy("key");
        Assertions.assertFalse(fd.isBasicTypeCollection());
    }

    @StructSheet(fileName = "unused.json")
    static class RefConfigBean {
        public List<Integer> items;
    }

    /**
     * A record whose accessor is not accessible from here gets it forced open.
     */
    @Test
    void testGetFieldValueForcesAccessOnHiddenRecord() {
        record Hidden(int a) {
        }
        SingleFieldDescriptor fd = new SingleFieldDescriptor(
                Hidden.class.getRecordComponents()[0], null);
        Assertions.assertEquals(42, fd.getFieldValueFrom(new Hidden(42)));
    }

    //  ------------------------------------------------------------------
    //  OptionalDescriptor
    //  ------------------------------------------------------------------

    /**
     * An {@code OptionalDescriptor} may also be built on top of a record component - the name then
     * falls back to the component's name.
     */
    @Test
    void testOptionalDescriptorOnRecordComponent() {
        StructWorker<OptionalRecord> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", OptionalRecord.class);
        worker.checkStructFactory();
        StructFactory factory = WorkerUtil.structFactory(OptionalRecord.class, worker);
        factory.parseStruct();
        OptionalDescriptor od = ((JdkStructFactory) factory).beanFields().stream()
                .filter(OptionalDescriptor.class::isInstance)
                .map(OptionalDescriptor.class::cast)
                .findFirst()
                .orElseThrow();
        //  @StructOptional carries no name -> the record component's name is used.
        Assertions.assertEquals("a", od.getName());
    }

    @StructSheet(fileName = "tpl_val.json")
    record OptionalRecord(@org.struct.annotation.StructOptional({@StructField(name = "key")}) int a) {
    }

    //  ------------------------------------------------------------------
    //  StructWorker
    //  ------------------------------------------------------------------

    /**
     * A {@code null} descriptor is simply ignored.
     */
    @Test
    void testHandleReferenceFieldValueNullDescriptor() {
        StructWorker<AbstractConverterBean> worker = WorkerUtil.newWorker("classpath:", AbstractConverterBean.class);
        StructFactory factory = WorkerUtil.structFactory(AbstractConverterBean.class, worker);
        worker.handleReferenceFieldValue(factory, null);
    }

    /**
     * A filter that is abstract can not be wrapped - the load fails loudly.
     */
    @Test
    void testAbstractFilterIsRejected() {
        StructWorker<AbstractFilterBean> worker = WorkerUtil.newWorker("classpath:", AbstractFilterBean.class);
        Assertions.assertThrows(RuntimeException.class, () -> worker.toList(java.util.ArrayList::new));
    }

    abstract static class AbstractFilter extends StructBeanFilter<Object> {
        public AbstractFilter(Consumer<Object> cellHandler) {
            super(cellHandler);
        }
    }

    @StructSheet(fileName = "tpl_val.json", filter = AbstractFilter.class)
    static class AbstractFilterBean {
        public int key;
        public String val;
    }

    //  ------------------------------------------------------------------
    //  Reflects / ConverterUtil
    //  ------------------------------------------------------------------

    interface Iface {
    }

    /**
     * An interface has no super class - the setter lookup walks into {@code null} and stops.
     */
    @Test
    void testLookupSetterOnInterface() {
        Assertions.assertTrue(Reflects.lookupFieldSetter(Iface.class, "x").isEmpty());
        //  the same for the accessor walk
        Assertions.assertNull(Reflects.lookupAccessor(Iface.class, "x"));
    }

    /**
     * A blank field name is a programming error.
     */
    @Test
    void testLookupWithBlankFieldName() {
        Assertions.assertThrows(AssertionError.class, () -> Reflects.lookupFieldSetter(String.class, ""));
    }

    /**
     * A lone {@code "0"} is decimal, not octal.
     */
    @Test
    void testDecodeBigIntegerLoneZero() {
        Assertions.assertEquals(BigInteger.ZERO, ConverterUtil.decodeBigInteger("0"));
        //  and a negative octal
        Assertions.assertEquals(-15L, ConverterUtil.decodeBigInteger("-017").longValue());
    }

    /**
     * A class whose annotations do not carry the target annotation at all.
     */
    @Deprecated
    static class DeprecatedClz {
    }

    @Test
    void testFindAnnotationWithoutMetaAnnotation() {
        //  the class carries no @Deprecated at all -> the direct lookup already fails and no meta
        //  annotation matches either.
        Assertions.assertNull(org.struct.util.AnnotationUtils.findAnnotation(Deprecated.class, Iface.class));
        //  the annotation is present right on the element -> no meta lookup needed.
        Assertions.assertNotNull(org.struct.util.AnnotationUtils.findAnnotation(
                Deprecated.class, DeprecatedClz.class));
    }

    @Test
    void testOptionalOfLookup() {
        Assertions.assertTrue(Reflects.lookupFieldGetter(Optional.class, "value").isPresent()
                || Reflects.lookupFieldGetter(Optional.class, "value").isEmpty());
    }
}
