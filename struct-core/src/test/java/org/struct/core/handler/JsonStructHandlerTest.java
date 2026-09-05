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

package org.struct.core.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonStructHandlerTest {

    @Test
    public void test() {
        StructWorker<KeyValueBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", KeyValueBean.class);
        ArrayList<KeyValueBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
    }

    @Test
    public void testWithOrder() {
        StructWorker<KeyValueWithOrder> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", KeyValueWithOrder.class);
        ArrayList<KeyValueWithOrder> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(1, beans.size());
    }

    /**
     * Every json type must be preserved and handed over to the converter pipeline.
     * <p>
     * Before the fix the row was deserialized straight into the target bean, which
     * bypassed every {@link org.struct.core.converter.Converter}: a {@link java.time.LocalDate}
     * field could not be filled at all, and numbers/booleans/arrays were rejected by
     * {@code JsonElement#getAsString()}.
     */
    @Test
    public void testAllValueTypes() {
        StructWorker<TypedBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", TypedBean.class);
        ArrayList<TypedBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());

        TypedBean first = beans.get(0);
        Assertions.assertEquals(1, first.id);
        Assertions.assertEquals("first", first.name);
        Assertions.assertEquals(1.5D, first.ratio);
        Assertions.assertTrue(first.enabled);
        Assertions.assertEquals(99, first.score);
        //  json array -> String[]
        Assertions.assertNotNull(first.tags);
        Assertions.assertEquals(2, first.tags.length);
        Assertions.assertEquals("alpha", first.tags[0]);
        Assertions.assertEquals("beta", first.tags[1]);
        //  LocalDate goes through LocalDateConverter
        Assertions.assertEquals(LocalDate.of(2020, 9, 15), first.birth);

        //  the "missing" field is null in the first row -> must stay null
        Assertions.assertNull(first.missing);

        TypedBean second = beans.get(1);
        Assertions.assertFalse(second.enabled);
        //  numeric string is still converted
        Assertions.assertEquals(88, second.score);
        Assertions.assertEquals(1, second.tags.length);
        Assertions.assertEquals("ignored", second.missing);
    }

    /**
     * A json array must be turned into a real java array.
     */
    @Test
    public void testJsonArrayToStringArray() {
        StructWorker<ArrayBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", ArrayBean.class);
        ArrayList<ArrayBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());
        Assertions.assertArrayEquals(new String[]{"alpha", "beta"}, beans.get(0).tags);
        Assertions.assertArrayEquals(new String[]{"gamma"}, beans.get(1).tags);
    }

    /**
     * A json array must also fill a {@link java.util.List} field.
     */
    @Test
    public void testJsonArrayToList() {
        StructWorker<ListBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", ListBean.class);
        ArrayList<ListBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());
        Assertions.assertEquals(Arrays.asList("alpha", "beta"), beans.get(0).tags);
        Assertions.assertEquals(Collections.singletonList("gamma"), beans.get(1).tags);
    }

    @StructSheet(fileName = "tpl_types.json")
    public static class TypedBean {
        public int id;
        public String name;
        public double ratio;
        public boolean enabled;
        public int score;
        public String[] tags;
        public String missing;
        public LocalDate birth;
    }

    @StructSheet(fileName = "tpl_types.json")
    public static class ArrayBean {
        public int id;
        public String[] tags;
    }

    @StructSheet(fileName = "tpl_types.json")
    public static class ListBean {
        public int id;
        public List<String> tags;
    }

    /**
     * {@code startOrder = 0} disables the "skip leading rows" guard, so EVERY row of the
     * array is parsed. (with the default {@code startOrder = 1} the guard is always
     * entered, so the {@code startOrder > 0 == false} branch was never exercised)
     */
    @Test
    public void testStartOrderZero() {
        StructWorker<KeyValueNoStartOrder> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", KeyValueNoStartOrder.class);
        ArrayList<KeyValueNoStartOrder> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
        Assertions.assertEquals(1, beans.get(0).key);
        Assertions.assertEquals(3, beans.get(2).key);
    }

    /**
     * A {@code null} element inside the json array must be skipped instead of blowing up.
     */
    @Test
    public void testNullRowIsSkipped() {
        StructWorker<KeyValueNullRowBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", KeyValueNullRowBean.class);
        ArrayList<KeyValueNullRowBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(2, beans.size());
        Assertions.assertEquals(1, beans.get(0).key);
        Assertions.assertEquals(2, beans.get(1).key);
    }

    /**
     * The deserializer must reject anything that is not a json object.
     */
    @Test
    public void testDeserializeNonObject() {
        JsonStructHandler.StructJsonDeserializer deserializer = new JsonStructHandler.StructJsonDeserializer();
        Assertions.assertNull(deserializer.deserialize(null, StructImpl.class, null));
        Assertions.assertNull(deserializer.deserialize(new JsonPrimitive("x"), StructImpl.class, null));
        Assertions.assertNull(deserializer.deserialize(new JsonArray(), StructImpl.class, null));
    }

    /**
     * A nested object is kept as a raw {@link JsonElement} (a user defined converter may
     * resolve it), while a nested array is flattened into a {@link List} element-wise -
     * including the recursive handling of a {@code null} item.
     */
    @Test
    public void testDeserializeNestedObjectAndArray() {
        JsonObject nested = new JsonObject();
        nested.addProperty("inner", 1);

        JsonArray innerArray = new JsonArray();
        innerArray.add(new JsonPrimitive("x"));
        innerArray.add(JsonNull.INSTANCE);
        innerArray.add(new JsonArray());

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(10));
        array.add(innerArray);

        JsonObject root = new JsonObject();
        root.addProperty("name", "n");
        root.add("nested", nested);
        root.add("array", array);
        root.add("nothing", JsonNull.INSTANCE);

        StructImpl impl = new JsonStructHandler.StructJsonDeserializer().deserialize(root, StructImpl.class, null);
        Assertions.assertEquals("n", impl.get("name"));
        //  a JsonNull value resolves to null and is dropped by StructImpl#add
        Assertions.assertNull(impl.get("nothing"));
        //  a nested object is kept as is
        Assertions.assertSame(nested, impl.get("nested"));

        List<?> list = (List<?>) impl.get("array");
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(10, ((Number) list.get(0)).intValue());

        List<?> inner = (List<?>) list.get(1);
        Assertions.assertEquals(3, inner.size());
        Assertions.assertEquals("x", inner.get(0));
        Assertions.assertNull(inner.get(1));
        Assertions.assertEquals(Collections.emptyList(), inner.get(2));
    }

    @StructSheet(fileName = "tpl_val.json")
    public static class KeyValueBean {
        @StructField(name = "key")
        public int key;
        public int val;
    }

    @StructSheet(fileName = "tpl_val.json", startOrder = 2, endOrder = 2)
    public static class KeyValueWithOrder {
        @StructField(name = "key")
        public int key;
        public int val;
    }

    @StructSheet(fileName = "tpl_val.json", startOrder = 0)
    public static class KeyValueNoStartOrder {
        @StructField(name = "key")
        public int key;
        public int val;
    }

    @StructSheet(fileName = "tpl_null_row.json")
    public static class KeyValueNullRowBean {
        @StructField(name = "key")
        public int key;
        public int val;
    }

}