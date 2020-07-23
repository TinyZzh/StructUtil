package org.struct.core.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;

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

}