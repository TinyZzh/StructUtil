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
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

        FileWatcherService service = new FileWatcherService(mockMs);
        service.setScheduleInitialDelay(10L);
        service.setScheduleTimeUnit(TimeUnit.DAYS);
        service.setScheduleDelay(999L);
        service.bootstrap();

        Assertions.assertThrows(ProviderMismatchException.class, () -> service.register("./"));
        Assertions.assertThrows(ProviderMismatchException.class, () -> service.registerAll("./"));
        service.registerHook("./", () -> {});
        service.deregisterHook("./");
        service.run();
    }

}