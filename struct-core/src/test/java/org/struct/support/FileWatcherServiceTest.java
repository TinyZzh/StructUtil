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