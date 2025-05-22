package org.struct.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.factory.StructFactory;
import org.struct.core.factory.StructFactoryBean;
import org.struct.core.handler.StructHandler;
import org.struct.core.matcher.WorkerMatcher;

import java.io.File;
import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author TinyZ
 * @since 2025.05.22
 */
public class WorkerUtilTest {


    @Test
    public void testStructFactory() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            Field filed = WorkerUtil.class.getDeclaredField("FACTORY_BEAN_HOLDER");
            filed.setAccessible(true);
            Object holder = filed.get(WorkerUtil.class);
            if (holder instanceof WorkerUtil.Holder<?>) {
                ((WorkerUtil.Holder<List<StructFactoryBean>>) holder).value = Collections.singletonList(new StructFactoryBean() {
                    @Override
                    public <T> StructFactory newInstance(Class<T> clzOfStruct, StructWorker<T> worker) {
                        throw new UnsupportedOperationException();
                    }
                });
            }
            WorkerUtil.structFactory(String.class, null);
        });
    }

    static class SimpleWorkerMatcher implements WorkerMatcher {
        @Override
        public int order() {
            return 0;
        }

        @Override
        public boolean matchFile(File file) {
            return true;
        }
    }

    @Test
    public void testLookupStructHandler() throws IllegalAccessException, NoSuchFieldException {
        Field filed = WorkerUtil.class.getDeclaredField("HANDLERS_HOLDER");
        filed.setAccessible(true);
        Object holder = filed.get(WorkerUtil.class);
        if (holder instanceof WorkerUtil.Holder<?>) {
            ((WorkerUtil.Holder<List<StructHandler>>) holder).value = Arrays.<StructHandler>asList(new StructHandler() {

                @Override
                public WorkerMatcher matcher() {
                    return new SimpleWorkerMatcher();
                }

                @Override
                public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {

                }
            }, new StructHandler() {
                @Override
                public WorkerMatcher matcher() {
                    return new WorkerMatcher() {
                        @Override
                        public int order() {
                            return 0;
                        }

                        @Override
                        public boolean matchFile(File file) {
                            return true;
                        }
                    };
                }

                @Override
                public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {

                }
            });
        }
        StructDescriptor descriptor = Mockito.mock(StructDescriptor.class);
        //
        Mockito.doReturn(WorkerMatcher.class).when(descriptor).getMatcher();
        Assertions.assertEquals(2, WorkerUtil.lookupStructHandler(descriptor, null).size());
        //
        Mockito.doReturn(SimpleWorkerMatcher.class).when(descriptor).getMatcher();
        List<StructHandler> handlers = WorkerUtil.lookupStructHandler(descriptor, null);
        Assertions.assertEquals(1, handlers.size());
    }

    @Test
    public void testNewListOnlyException() throws Exception {
        //
        try {
            WorkerUtil.newListOnly(Object.class);
        } catch (Exception e) {
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testNewInterface() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(List.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(Set.class).getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(AbstractSet.class).getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(HashSet.class).getClass()));
    }

    @Test
    public void testNewAbstractClass() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(AbstractList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewArrayList() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(ArrayList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(HashSet.class).getClass()));
    }

    @Test
    public void testNewMap() throws Exception {
        Assertions.assertTrue(ConcurrentHashMap.class.isAssignableFrom(WorkerUtil.newMap(ConcurrentHashMap.class).getClass()));
        Assertions.assertTrue(HashMap.class.isAssignableFrom(WorkerUtil.newMap(HashMap.class).getClass()));
        Assertions.assertTrue(HashMap.class.isAssignableFrom(WorkerUtil.newMap(AbstractMap.class).getClass()));
        Assertions.assertTrue(HashMap.class.isAssignableFrom(WorkerUtil.newMap(Map.class).getClass()));
        Assertions.assertTrue(HashMap.class.isAssignableFrom(WorkerUtil.newMap(null).getClass()));
    }

    @Test
    public void testResolveFilePath() {
        //  文件存在时
        WorkerUtil.resolveFilePath("classpath:/org/struct/core/", "bean.xls");
        Assertions.assertEquals("classpath:./data/key.data", WorkerUtil.resolveFilePath("classpath:./data/", "key.data"));
        Assertions.assertEquals("classpath:./data/key.data", WorkerUtil.resolveFilePath("classpath:./data", "key.data"));
        Assertions.assertEquals("./data/key.data", WorkerUtil.resolveFilePath("file:./data", "key.data"));
        Assertions.assertEquals("./data/key.data", WorkerUtil.resolveFilePath("./data", "key.data"));
    }
}