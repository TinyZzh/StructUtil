package org.struct.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileWatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherService.class);
    /**
     * the root path.
     */
    private static volatile String DEFAULT_WORKSPACE_DIR;
    /**
     * Watch file changed and load template data when file modified.
     */
    private WatchService ws;
    /**
     * the file change event hook map.
     */
    private final ConcurrentHashMap<String, Runnable> hookMap;

    private volatile boolean isInitialized = false;

    static {
        DEFAULT_WORKSPACE_DIR = System.getProperty("struct.workspace.dir", "./data/");
    }

    public FileWatcherService() throws IOException {
        this(Executors.newScheduledThreadPool(1, r -> new Thread("FileWatcherThread")));
    }

    public FileWatcherService(ScheduledExecutorService scheduledThreadPool) throws IOException {
        this.ws = FileSystems.getDefault().newWatchService();
        this.hookMap = new ConcurrentHashMap<>();
        if (scheduledThreadPool != null) {
            scheduledThreadPool.scheduleWithFixedDelay(this::process, 10, 5, TimeUnit.SECONDS);
        }
    }

    public void withWorkspace(String dir) {
        try {
            Path path = Paths.get(dir == null || dir.isEmpty() ? DEFAULT_WORKSPACE_DIR : dir);
            path.register(this.ws, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception e) {
            LOGGER.error("watch path failure. path:{}", dir, e);
        }
    }

    private void checkService() {
        if (!isInitialized) {
            synchronized (this) {
                withWorkspace(null);
                this.isInitialized = true;
            }
        }
    }

    public void registerHook(String fileName, Runnable hook) {
        this.checkService();
        this.hookMap.putIfAbsent(fileName, hook);
    }

    public void deregisterHook(String fileName) {
        this.checkService();
        this.hookMap.remove(fileName);
    }

    /**
     * Schedule process the file watch events.
     */
    public void process() {
        WatchKey key = ws.poll();
        if (key == null) {
            return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            String fileName = ev.context().toString();

            Runnable runnable = hookMap.get(fileName);
            if (runnable != null) {
                runnable.run();
            }
        }
        key.reset();
    }
}
