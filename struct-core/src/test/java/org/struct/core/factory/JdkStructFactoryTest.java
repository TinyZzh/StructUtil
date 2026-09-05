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

package org.struct.core.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.exception.NoSuchFieldReferenceException;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;

public class JdkStructFactoryTest {

    private static final String WS = "classpath:/org/struct/core/";

    private static <T> StructFactory parse(Class<T> clz) {
        StructWorker<T> worker = WorkerUtil.newWorker(WS, clz);
        StructFactory factory = WorkerUtil.structFactory(clz, worker);
        factory.parseStruct();
        return factory;
    }

    /**
     * {@code parseStruct()} must be idempotent - calling it again is a no-op.
     */
    @Test
    public void testParseStructIsIdempotent() {
        StructFactory factory = parse(SimpleBean.class);
        factory.parseStruct();
        factory.parseStruct();
        Assertions.assertEquals(7, factory.getFieldValuesArray(new SimpleBean(7, "x"), new String[]{"key"}));
    }

    /**
     * {@code static} fields are not struct fields.
     */
    @Test
    public void testStaticFieldIsSkipped() {
        StructFactory factory = parse(WithStaticBean.class);
        Assertions.assertThrows(NoSuchFieldReferenceException.class,
                () -> factory.getFieldValuesArray(new WithStaticBean(), new String[]{"CONST"}));
        //  the regular field is still resolved
        Assertions.assertEquals(3, factory.getFieldValuesArray(new WithStaticBean(), new String[]{"key"}));
    }

    /**
     * Two fields mapped onto the same name are a conflict: the first one wins and a
     * warning is logged.
     */
    @Test
    public void testDuplicateFieldNameConflict() {
        StructFactory factory = parse(ConflictBean.class);
        ConflictBean bean = new ConflictBean();
        bean.a = 11;
        bean.b = 22;
        //  putIfAbsent keeps the FIRST descriptor
        Object v = factory.getFieldValuesArray(bean, new String[]{"key"});
        Assertions.assertEquals(11, v);
    }

    /**
     * A record with two components mapped onto the same name behaves the same way.
     */
    @Test
    public void testDuplicateRecordComponentNameConflict() {
        StructFactory factory = parse(ConflictRecord.class);
        Object v = factory.getFieldValuesArray(new ConflictRecord(1, 2), new String[]{"key"});
        Assertions.assertEquals(1, v);
    }

    /**
     * A {@code null} intermediate row yields an empty {@link java.util.Optional}.
     */
    @Test
    public void testNewStructInstanceWithNull() {
        StructFactory factory = parse(SimpleBean.class);
        Assertions.assertFalse(((java.util.Optional<?>) factory.newStructInstance(null)).isPresent());
    }

    /**
     * A missing value for a {@code required} field must be reported.
     */
    @Test
    public void testRequiredFieldMissing() {
        StructFactory factory = parse(RequiredBean.class);
        //  the data file has no "nope" column at all
        Assertions.assertThrows(RuntimeException.class, () -> factory.newStructInstance(new StructImpl()));
    }

    /**
     * A required field that resolves to an empty string is invalid too.
     */
    @Test
    public void testRequiredFieldBlank() {
        StructFactory factory = parse(RequiredBean.class);
        StructImpl si = new StructImpl();
        si.add("nope", "");
        Assertions.assertThrows(RuntimeException.class, () -> factory.newStructInstance(si));
    }

    /**
     * {@code @StructField(cached = true)} interns the resolved string.
     */
    @Test
    public void testCachedFieldIsInterned() {
        StructWorker<CachedBean> worker = WorkerUtil.newWorker(WS, CachedBean.class);
        ArrayList<CachedBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
        //  "11" is a compile time constant and therefore already interned
        Assertions.assertSame("11", beans.get(0).val);
    }

    /**
     * A reference field whose target table is empty resolves to {@code null}.
     */
    @Test
    public void testReferenceFieldWithEmptyTable() {
        StructWorker<RefEmptyBean> worker = WorkerUtil.newWorker(WS, RefEmptyBean.class);
        ArrayList<RefEmptyBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
        for (RefEmptyBean bean : beans) {
            Assertions.assertNull(bean.ref);
        }
    }

    /**
     * A REQUIRED reference field whose target table is empty must be reported.
     */
    @Test
    public void testRequiredReferenceFieldWithEmptyTable() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            StructWorker<RefRequiredBean> worker = WorkerUtil.newWorker(WS, RefRequiredBean.class);
            worker.toList(ArrayList::new);
        });
    }

    /**
     * A {@code record} can be used as a struct bean; its components are mapped
     * positionally onto the canonical constructor.
     */
    @Test
    public void testRecordBean() {
        StructWorker<ValRecord> worker = WorkerUtil.newWorker(WS, ValRecord.class);
        ArrayList<ValRecord> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
        Assertions.assertEquals(1, beans.get(0).key());
        Assertions.assertEquals("11", beans.get(0).val());
        Assertions.assertEquals(3, beans.get(2).key());
    }

    /**
     * A field level converter is used instead of the registry lookup.
     */
    @Test
    public void testFieldLevelConverter() {
        StructWorker<ConverterBean> worker = WorkerUtil.newWorker(WS, ConverterBean.class);
        ArrayList<ConverterBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
        Assertions.assertEquals("11!", beans.get(0).val);
    }

    /**
     * {@code aggregateBy} with an ARRAY key: one parent row collects several
     * children in a single lookup.
     */
    @Test
    public void testAggregateByArrayKey() {
        StructWorker<AggregateArrayBean> worker = WorkerUtil.newWorker(WS, AggregateArrayBean.class);
        ArrayList<AggregateArrayBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());

        AggregateArrayBean first = beans.get(0);
        Assertions.assertEquals(1, first.id);
        Assertions.assertNotNull(first.children);
        Assertions.assertEquals(2, first.children.size(), "childIds=1|2 must resolve to 2 children");
        Assertions.assertEquals(1, first.children.get(0).key);
        Assertions.assertEquals(2, first.children.get(1).key);
    }

    /**
     * {@code aggregateBy} with a COLLECTION key.
     */
    @Test
    public void testAggregateByCollectionKey() {
        StructWorker<AggregateCollectionBean> worker = WorkerUtil.newWorker(WS, AggregateCollectionBean.class);
        ArrayList<AggregateCollectionBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());

        AggregateCollectionBean first = beans.get(0);
        Assertions.assertNotNull(first.children);
        Assertions.assertEquals(2, first.children.size());
    }

    /**
     * {@code aggregateBy} with a SCALAR key falls back to a plain map lookup.
     */
    @Test
    public void testAggregateByScalarKey() {
        StructWorker<AggregateScalarBean> worker = WorkerUtil.newWorker(WS, AggregateScalarBean.class);
        ArrayList<AggregateScalarBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());

        AggregateScalarBean first = beans.get(0);
        Assertions.assertNotNull(first.child);
        Assertions.assertEquals(1, first.child.key);
    }

    /**
     * A REQUIRED reference field whose key matches nothing must be reported
     * instead of silently staying {@code null}.
     * <p>
     * The exception surfaces as an {@link IllegalArgumentException}: the real
     * {@link NoSuchFieldReferenceException} is thrown by the handler and is then
     * wrapped by {@code StructWorker#handleDataFile}, which reports
     * "unknown data file extension" once every handler has been tried.
     */
    @Test
    public void testRequiredReferenceFieldWithNoMatch() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructWorker<NoMatchBean> worker = WorkerUtil.newWorker(WS, NoMatchBean.class);
            worker.toList(ArrayList::new);
        });
    }

    //  ------------------------------------------------------------------
    //  beans
    //  ------------------------------------------------------------------

    @StructSheet(fileName = "tpl_val.json")
    public static class SimpleBean {
        public int key;
        public String val;

        public SimpleBean() {
        }

        public SimpleBean(int key, String val) {
            this.key = key;
            this.val = val;
        }
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class WithStaticBean {
        public static final String CONST = "constant";

        public int key = 3;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class ConflictBean {
        @StructField(name = "key")
        public int a;
        @StructField(name = "key")
        public int b;
    }

    @StructSheet(fileName = "tpl_val.json")
    public record ConflictRecord(@StructField(name = "key") int first,
                                 @StructField(name = "key") int second) {
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class RequiredBean {
        @StructField(name = "nope", required = true)
        public String missing;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class CachedBean {
        @StructField(name = "val", cached = true)
        public String val;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class RefEmptyBean {
        public int key;
        @StructField(ref = EmptyBean.class, refUniqueKey = "key")
        public EmptyBean ref;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class RefRequiredBean {
        public int key;
        @StructField(ref = EmptyBean.class, refUniqueKey = "key", required = true)
        public EmptyBean ref;
    }

    @StructSheet(fileName = "tpl_empty.json")
    public static class EmptyBean {
        public int key;
    }

    @StructSheet(fileName = "tpl_val.json")
    public record ValRecord(int key, String val) {
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class ConverterBean {
        @StructField(name = "val", converter = BangConverter.class)
        public String val;
    }

    /**
     * A field level converter that appends a marker, so it is easy to tell whether
     * it was used instead of the registry lookup.
     */
    public static class BangConverter implements org.struct.core.converter.Converter {

        @Override
        public Object convert(org.struct.core.converter.ConvertContext ctx, Object originValue, Class<?> targetType) {
            return originValue == null ? null : String.valueOf(originValue) + "!";
        }
    }

    @StructSheet(fileName = "tpl_aggregate.json")
    public static class AggregateArrayBean {
        public int id;
        @StructField(name = "childIds")
        public int[] childIds;
        //  the key comes from the parent's array field
        @StructField(ref = ChildBean.class, refUniqueKey = "key", aggregateBy = "childIds")
        public java.util.List<ChildBean> children;
    }

    //  NOTE: a plain (non reference) List field is filled directly, so the json
    //  value must already be an array - the separator splitting only applies to
    //  array target types.
    @StructSheet(fileName = "tpl_aggregate_coll.json")
    public static class AggregateCollectionBean {
        public int id;
        @StructField(name = "childIds")
        public java.util.List<Integer> childIds;
        @StructField(ref = ChildBean.class, refUniqueKey = "key", aggregateBy = "childIds")
        public java.util.List<ChildBean> children;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class AggregateScalarBean {
        public int key;
        @StructField(ref = ChildBean.class, refUniqueKey = "key", aggregateBy = "key")
        public ChildBean child;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class ChildBean {
        public int key;
        public String val;
    }

    /**
     * The reference key never matches a row of the (non empty) target table.
     */
    @StructSheet(fileName = "tpl_agg_nomatch.json")
    public static class NoMatchBean {
        public int key;
        @StructField(ref = ChildBean.class, refUniqueKey = "key", required = true)
        public ChildBean child;
    }
}
