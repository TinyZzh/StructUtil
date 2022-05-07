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

package org.struct.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class FileWatcherServiceTest {

    @Test
    public void test() throws IOException {
        WatchService mockMs = mock(WatchService.class);
        WatchKey mockWk = mock(WatchKey.class);
        doReturn(mockWk, null, null).when(mockMs).poll();
        WatchEvent mockWe0 = mock(WatchEvent.class);
        doReturn(StandardWatchEventKinds.OVERFLOW).when(mockWe0).kind();
        WatchEvent mockWe1 = mock(WatchEvent.class);
        doReturn(StandardWatchEventKinds.ENTRY_MODIFY).when(mockWe1).kind();
        when(mockWk.pollEvents()).thenReturn(Arrays.asList(mockWe0, mockWe1));

        FileWatcherService service = FileWatcherService.newBuilder().setWatchService(mockMs)
                .setScheduleInitialDelay(10L)
                .setScheduleTimeUnit(TimeUnit.DAYS)
                .setScheduleDelay(999L)
                .setExecutor(Executors.newScheduledThreadPool(1, r -> new Thread(r, "test")))
                .build();
        service.bootstrap();

        Assertions.assertThrows(ProviderMismatchException.class, () -> service.register("./"));
        Assertions.assertThrows(ProviderMismatchException.class, () -> service.registerAll("./"));
        service.registerHook("./", () -> {
        });
        service.deregisterHook("./");
        service.run();

        Assertions.assertThrows(IllegalStateException.class, () -> service.bootstrap());
    }

    @Test
    public void process() throws IOException, NoSuchFieldException, IllegalAccessException {
        FileWatcherService fws = FileWatcherService.newBuilder().build();

        WatchService ws = mock(WatchService.class);
        {
            Field field = FileWatcherService.class.getDeclaredField("ws");
            field.setAccessible(true);
            field.set(fws, ws);
        }
        Map<WatchKey, Path> keys = spy(new ConcurrentHashMap<>());
        {
            Field field = FileWatcherService.class.getDeclaredField("keys");
            field.setAccessible(true);
            field.set(fws, keys);
        }
        WatchKey wk = mock(WatchKey.class);
        Path thePath = mock(Path.class, RETURNS_DEEP_STUBS);
        WatchEvent<Path> we = mock(WatchEvent.class, RETURNS_DEEP_STUBS);
        doReturn(Collections.singletonList(we)).when(wk).pollEvents();
        doReturn(wk).when(ws).poll();
        doReturn(thePath).when(keys).get(any(WatchKey.class));
        fws.run();
    }

    @Test()
    public void testProcess() throws IOException, IllegalAccessException, NoSuchFieldException {
        WatchService ws = mock(WatchService.class);
        Path thePath = mock(Path.class, RETURNS_DEEP_STUBS);
        {
            WatchKey wk = mock(WatchKey.class);
            doReturn(wk).when(ws).poll();

            WatchEvent<Path> we = mock(WatchEvent.class, RETURNS_DEEP_STUBS);
            doReturn(List.of(we, we, we)).when(wk).pollEvents();
            doReturn(StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_MODIFY).when(we).kind();

            doReturn(thePath).when(we).context();
            doReturn(thePath).when(thePath).resolve(any(Path.class));
        }
        FileWatcherService fws = FileWatcherService.newBuilder().setWatchService(ws).build();

        Map<WatchKey, Path> keys = spy(new ConcurrentHashMap<>());
        {
            Field field = FileWatcherService.class.getDeclaredField("keys");
            field.setAccessible(true);
            field.set(fws, keys);
            doReturn(thePath).when(keys).get(any(WatchKey.class));
        }
        AtomicBoolean bool = new AtomicBoolean(false);
        //  抛出异常
        fws.registerHook(thePath, () -> {
            if (bool.get()) {
                throw new IllegalStateException();
            }
            bool.set(true);
        });
        fws.run();
    }

    @Test
    public void testRegisterAll() throws IOException {
        FileWatcherService fws = FileWatcherService.newBuilder().build();
        String path = "./";
        fws.registerAll(path);
        fws.registerAll(Paths.get(path));
    }

    @Test
    public void testRegisterHook() throws IOException {
        FileWatcherService fws = FileWatcherService.newBuilder().build();
        String path = "./";
        fws.registerHook(Paths.get(path), () -> {
        });
        fws.registerHook(path, () -> {
        });
        fws.deregisterHook(Paths.get(path));
        fws.deregisterHook(path);
    }

    @Test()
    public void testSetScheduleInitialDelay() throws IOException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FileWatcherService.newBuilder().setScheduleInitialDelay(-1);
        });
    }

    @Test()
    public void testSetScheduleDelay() throws IOException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FileWatcherService.newBuilder().setScheduleDelay(-1);
        });
    }

    @Test()
    public void testSetScheduleTimeUnit() throws IOException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            FileWatcherService.newBuilder().setScheduleTimeUnit(null);
        });
    }
}