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

package org.struct.store;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * The core {@link AbstractStructStore} is framework agnostic - this test is plain java and
 * doesn't touch any spring annotation / callback.
 *
 * @author TinyZ.
 */
class AbstractStructStoreTest {

    @Test
    void testBasics() {
        TestStore store = new TestStore(Bean.class);
        Assertions.assertEquals(Bean.class, store.clzOfBean());
        Assertions.assertEquals(Bean.class, store.getClzOfBean());
        Assertions.assertEquals(0, store.size());
        Assertions.assertEquals(Bean.class.getSimpleName() + StructStore.class.getSimpleName(), store.identify());
        Assertions.assertFalse(store.isInitialized());
        Assertions.assertEquals(AbstractStructStore.DEFAULT_WAIT_FOR_INIT_TIMEOUT_MS, store.getWaitForInitTimeoutMs());
    }

    @Test
    void testStatusCas() {
        TestStore store = new TestStore(Bean.class);
        Assertions.assertTrue(store.casStatusInit());
        Assertions.assertFalse(store.casStatusInit());
        Assertions.assertTrue(store.casStatusDone());
        Assertions.assertFalse(store.casStatusDone());
        Assertions.assertTrue(store.isInitialized());
        store.waitForDone();
        store.casStatusReset();
        Assertions.assertFalse(store.isInitialized());
    }

    @Test
    void testSetters() {
        TestStore store = new TestStore();
        Assertions.assertNull(store.clzOfBean());
        store.setClzOfBean(Bean.class);
        Assertions.assertEquals(Bean.class, store.clzOfBean());

        StoreOptions options = new StoreOptions();
        options.setWorkspace("xx");
        store.setOptions(options);
        Assertions.assertSame(options, store.getOptions());

        store.setWaitForInitTimeoutMs(1234L);
        Assertions.assertEquals(1234L, store.getWaitForInitTimeoutMs());
    }

    @Test
    void testReloadBeforeInitializedIsNoop() {
        TestStore store = new TestStore(Bean.class);
        //  not initialized yet -> reload does nothing.
        store.reload();
        Assertions.assertFalse(store.isInitialized());
        Assertions.assertEquals(0, store.size());
    }

    @Test
    void testReload() {
        TestStore store = new TestStore(Bean.class);
        store.casStatusInit();
        store.casStatusDone();
        Assertions.assertTrue(store.isInitialized());
        store.reload();
        Assertions.assertTrue(store.isInitialized());
    }

    @Test
    void testLookupHelpers() {
        TestStore store = new TestStore(Bean.class);
        Assertions.assertFalse(store.tryGet(1L).isPresent());
        Bean dv = new Bean();
        Assertions.assertSame(dv, store.getOrDefault(1L, dv));
        Assertions.assertTrue(store.lookup(1L, 2L).isEmpty());
    }

    @Test
    void testRequireOptionsFailFast() {
        TestStore store = new TestStore(Bean.class);
        Assertions.assertThrows(IllegalStateException.class, store::requireOptions);
    }

    @Test
    void testRequireOptions() {
        TestStore store = new TestStore(Bean.class);
        StoreOptions options = new StoreOptions();
        store.setOptions(options);
        Assertions.assertSame(options, store.requireOptions());
    }

    /**
     * {@link AbstractStructStore#waitForStatus(int)} must be bounded: it throws instead of hanging
     * forever when the store never reaches the expected status.
     */
    @Test
    void testWaitForStatusBounded() {
        TestStore store = new TestStore(Bean.class);
        store.setWaitForInitTimeoutMs(50L);
        Assertions.assertTrue(store.casStatusInit());
        Assertions.assertThrows(IllegalStateException.class, store::waitForDone);
    }

    /**
     * {@link AbstractStructStore#casStatusReset()} must be bounded too.
     */
    @Test
    void testCasStatusResetBounded() {
        TestStore store = new TestStore(Bean.class);
        store.setWaitForInitTimeoutMs(50L);
        store.casStatusInit();
        //  a tiny timeout must not hang the reset.
        long begin = System.nanoTime();
        store.casStatusReset();
        Assertions.assertTrue(System.nanoTime() - begin < 5_000_000_000L);
        Assertions.assertFalse(store.isInitialized());
    }

    /**
     * {@link AbstractStructStore#waitForStatus(int)} must report a usable identify even when the
     * {@code clzOfBean} hasn't been injected yet.
     */
    @Test
    void testWaitForStatusTimeoutWithoutClzOfBean() {
        TestStore store = new TestStore();
        store.setWaitForInitTimeoutMs(50L);
        Assertions.assertTrue(store.casStatusInit());
        Assertions.assertThrows(IllegalStateException.class, store::waitForDone);
    }

    /**
     * The {@code casStatusReset()} CAS fails as soon as another thread flips the status in
     * between - the reset must keep spinning (and eventually succeed) instead of giving up.
     */
    @Test
    void testCasStatusResetRetriesUnderContention() throws Exception {
        TestStore store = new TestStore(Bean.class);
        store.setWaitForInitTimeoutMs(10_000L);
        AtomicBoolean stop = new AtomicBoolean();
        //  no yielding here: the flippers have to be dense enough to actually lose the race.
        Thread flipperA = statusFlipper(store, stop, false);
        Thread flipperB = statusFlipper(store, stop, false);
        flipperA.start();
        flipperB.start();
        try {
            //  the flippers keep flipping the status, so the store's status is meaningless
            //  while they run - only assert once they are gone.
            for (int i = 0; i < 5_000; i++) {
                store.casStatusReset();
            }
        } finally {
            stop.set(true);
            flipperA.join(5_000L);
            flipperB.join(5_000L);
        }
        Assertions.assertFalse(store.isInitialized());
    }

    /**
     * Same race, but the reset runs out of time - it must fail fast instead of hanging.
     */
    @Test
    void testCasStatusResetTimeoutUnderContention() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> casStatusResetUntilTimeout(new TestStore(Bean.class)));
    }

    @Test
    void testCasStatusResetTimeoutWithoutClzOfBean() {
        Assertions.assertThrows(IllegalStateException.class, () -> casStatusResetUntilTimeout(new TestStore()));
    }

    /**
     * Drives the status through NORMAL -> INITIALIZING -> DONE -> NORMAL so that the tested
     * {@code casStatusReset()} sees a value that changed under its feet.
     */
    private static Thread statusFlipper(TestStore store, AtomicBoolean stop, boolean yield) {
        Thread t = new Thread(() -> {
            while (!stop.get()) {
                store.casStatusInit();
                store.casStatusDone();
                try {
                    store.casStatusReset();
                } catch (RuntimeException ignored) {
                    //  the reset may time out while racing, ignore.
                }
                if (yield) {
                    Thread.onSpinWait();
                }
            }
        });
        t.setDaemon(true);
        return t;
    }

    /**
     * Spins {@code casStatusReset()} with a zero timeout until the contention makes one attempt
     * fail its CAS and blow up on the deadline.
     */
    private void casStatusResetUntilTimeout(TestStore store) throws Exception {
        store.setWaitForInitTimeoutMs(0L);
        AtomicBoolean stop = new AtomicBoolean();
        Thread flipper = statusFlipper(store, stop, false);
        flipper.start();
        try {
            for (int i = 0; i < 20_000; i++) {
                store.casStatusReset();
            }
        } finally {
            stop.set(true);
            flipper.join(5_000L);
        }
    }

    static class Bean {
    }

    static class TestStore extends AbstractStructStore<Long, Bean> {

        TestStore() {
        }

        TestStore(Class<Bean> clzOfBean) {
            super(clzOfBean);
        }

        @Override
        public void initialize() {
            if (casStatusInit()) {
                casStatusDone();
            }
        }

        @Override
        public void dispose() {
            casStatusReset();
        }

        @Override
        public List<Bean> getAll() {
            return List.of();
        }

        @Override
        public Bean get(Long key) {
            return null;
        }

        @Override
        public List<Bean> lookup(Predicate<Bean> filter) {
            return List.of();
        }
    }
}
