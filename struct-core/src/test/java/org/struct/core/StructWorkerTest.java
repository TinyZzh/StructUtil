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
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.handler.StructHandler;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author TinyZ.
 * @date 2020-10-09.
 */
class StructWorkerTest {


    @Test
    public void testMapWithGroup() {
        StructWorker<MapWithGroup0> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MapWithGroup0.class);
        ArrayList<MapWithGroup0> list = worker.load(ArrayList::new);
        System.out.println();
    }

    /**
     * A reference field declared as a {@link Collection}.
     */
    @Test
    public void testCollectionReferenceField() {
        StructWorker<CollRefBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", CollRefBean.class);
        ArrayList<CollRefBean> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, list.size());

        CollRefBean first = list.get(0);
        Assertions.assertNotNull(first.items);
        Assertions.assertEquals(1, first.items.size());
        Assertions.assertEquals(1, first.items.get(0).key);
    }

    /**
     * A collection type that cannot be instantiated must fail loudly.
     */
    @Test
    public void testToListWithGroupIllegalCollectionType() {
        StructWorker<CollRefBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", CollRefBean.class);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> worker.toListWithGroup(Integer.class, new String[]{"key"}));
    }

    /**
     * A map type that cannot be instantiated must fail loudly.
     */
    @Test
    public void testToMapWithGroupIllegalMapType() {
        StructWorker<MapWithGroup0> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MapWithGroup0.class);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> worker.toMapWithGroup(Integer.class, new String[]{"id"}, new String[]{"group"}));
    }

    /**
     * A self reference must be detected instead of recursing forever.
     * <p>
     * Before the fix the placeholder was put into the map only AFTER the recursion
     * returned, so {@code containsKey} never matched and a circular reference
     * ended in a {@link StackOverflowError}.
     */
    @Test
    public void testSelfReferenceIsDetected() {
        withCircularReferencesAllowed(() -> {
            StructWorker<SelfRefBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", SelfRefBean.class);
            ArrayList<SelfRefBean> list = worker.toList(ArrayList::new);
            Assertions.assertEquals(3, list.size());
            //  the first level resolves normally, the cycle is cut at the second level
            //  instead of recursing forever.
            SelfRefBean first = list.get(0);
            Assertions.assertNotNull(first.parent);
            Assertions.assertNull(first.parent.parent, "the cycle must be cut");
        });
    }

    /**
     * Mutually referencing beans must be detected as well.
     */
    @Test
    public void testMutualReferenceIsDetected() {
        withCircularReferencesAllowed(() -> {
            StructWorker<MutualA> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MutualA.class);
            ArrayList<MutualA> list = worker.toList(ArrayList::new);
            Assertions.assertEquals(3, list.size());
            for (MutualA a : list) {
                //  a -> b -> a -> b : the second round trip is cut
                if (a.b != null && a.b.a != null) {
                    Assertions.assertNull(a.b.a.b, "the cycle must be cut");
                }
            }
        });
    }

    /**
     * With circular references disallowed the loop must be reported explicitly.
     */
    @Test
    public void testCircularReferenceForbidden() {
        boolean prev = StructConfig.INSTANCE.isAllowCircularReferences();
        StructConfig.INSTANCE.setAllowCircularReferences(false);
        try {
            StructWorker<SelfRefBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", SelfRefBean.class);
            Assertions.assertThrows(RuntimeException.class, () -> worker.toList(ArrayList::new));
        } finally {
            StructConfig.INSTANCE.setAllowCircularReferences(prev);
        }
    }

    /**
     * An empty or {@code null} intermediate row yields an empty {@link java.util.Optional}.
     */
    @Test
    public void testCreateInstanceWithEmptyOrNull() {
        StructWorker<MapWithGroup0> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MapWithGroup0.class);
        Assertions.assertFalse(worker.createInstance(new StructImpl()).isPresent());
        Assertions.assertFalse(worker.createInstance((Object) null).isPresent());
    }

    /**
     * A non existent data file must be reported.
     */
    @Test
    public void testDataFileNotFound() {
        StructWorker<NoSuchFileBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", NoSuchFileBean.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> worker.toList(ArrayList::new));
    }

    /**
     * {@code @StructSheet#filter} wraps the cell handler: beans rejected by the
     * filter never reach the result collection.
     */
    @Test
    public void testStructBeanFilter() {
        StructWorker<FilteredBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", FilteredBean.class);
        ArrayList<FilteredBean> list = worker.toList(ArrayList::new);
        //  tpl_val.json holds key 1..3, the filter keeps key > 1
        Assertions.assertEquals(2, list.size());
        for (FilteredBean bean : list) {
            Assertions.assertTrue(bean.key > 1);
        }
    }

    /**
     * A filter without a {@code (Consumer)} constructor cannot be wired up and
     * must fail loudly.
     */
    @Test
    public void testStructBeanFilterWithoutConsumerConstructor() {
        StructWorker<BadFilterBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", BadFilterBean.class);
        Assertions.assertThrows(RuntimeException.class, () -> worker.toList(ArrayList::new));
    }

    //  ------------------------------------------------------------------
    //  helpers
    //  ------------------------------------------------------------------

    /**
     * Runs {@code task} with circular references explicitly allowed.
     * <p>
     * {@link StructConfig} is a process wide singleton, so a test must not rely on
     * whatever value a previously executed test happened to leave behind.
     */
    private static void withCircularReferencesAllowed(ThrowingRunnable task) {
        boolean prev = StructConfig.INSTANCE.isAllowCircularReferences();
        StructConfig.INSTANCE.setAllowCircularReferences(true);
        try {
            task.run();
        } finally {
            StructConfig.INSTANCE.setAllowCircularReferences(prev);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "MapWithGroup0")
    public static class MapWithGroup0 {
        public int id;
        public String str;
        public int group;
        @StructField(ref = MapWithGroup1.class, refGroupBy = "group", refUniqueKey = "vg")
        public Map<Integer, MapWithGroup1> data;
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "MapWithGroup1")
    public static class MapWithGroup1 {
        public String vg;
        public int group;
        public int v;
    }

    //  ------------------------------------------------------------------
    //  beans used by the tests above
    //  ------------------------------------------------------------------

    //  NOTE: `refUniqueKey` / `refGroupBy` are resolved against the field's
    //  *mapped* name (StructField#name when set, otherwise the java field name),
    //  so these beans simply use the json property name as the field name.

    @StructSheet(fileName = "tpl_val.json")
    public static class CollRefBean {
        public int key;
        @StructField(ref = SubItem.class, refGroupBy = "key")
        public List<SubItem> items;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class SubItem {
        public int key;
        public String val;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class SelfRefBean {
        public int key;
        @StructField(ref = SelfRefBean.class, refUniqueKey = "key")
        public SelfRefBean parent;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class MutualA {
        public int key;
        @StructField(ref = MutualB.class, refUniqueKey = "key")
        public MutualB b;
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class MutualB {
        public int key;
        @StructField(ref = MutualA.class, refUniqueKey = "key")
        public MutualA a;
    }

    @StructSheet(fileName = "no_such_data_file.json")
    public static class NoSuchFileBean {
        public int key;
    }

    @StructSheet(fileName = "tpl_val.json", filter = IdGt1Filter.class)
    public static class FilteredBean {
        public int key;
    }

    @StructSheet(fileName = "tpl_val.json", filter = NoConsumerCtorFilter.class)
    public static class BadFilterBean {
        public int key;
    }

    /**
     * Keeps beans whose id is greater than 1.
     */
    public static class IdGt1Filter extends StructBeanFilter<FilteredBean> {

        public IdGt1Filter(Consumer<FilteredBean> cellHandler) {
            super(cellHandler);
        }

        @Override
        public boolean test(FilteredBean bean) {
            return bean.key > 1;
        }
    }

    /**
     * A filter that lacks the required {@code (Consumer)} constructor.
     */
    public static class NoConsumerCtorFilter extends StructBeanFilter<BadFilterBean> {

        public NoConsumerCtorFilter() {
            super(null);
        }

        @Override
        public boolean test(BadFilterBean bean) {
            return true;
        }
    }
}