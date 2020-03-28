package org.struct.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FileWatcherService implements Runnable {

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

    private final Map<WatchKey,Path> keys = new ConcurrentHashMap<>();

    private volatile boolean isInitialized = false;

    private ScheduledExecutorService executor;

    private ScheduledFuture<?> future;

    static {
        DEFAULT_WORKSPACE_DIR = System.getProperty("struct.workspace.dir", "./data/");
    }

    public FileWatcherService() throws IOException {
        this(null);
    }

    public FileWatcherService(ScheduledExecutorService executor) throws IOException {
        this.ws = FileSystems.getDefault().newWatchService();
        this.hookMap = new ConcurrentHashMap<>();
        if (executor != null) {
            this.executor = executor;
            this.future = executor.scheduleWithFixedDelay(this::process, 10, 5, TimeUnit.SECONDS);
        }
    }

    public void withWorkspace(String dir) {
        this.withWorkspace(dir, null);
    }

    public void withWorkspace(String dir, ScheduledExecutorService scheduledExecutorService) {
        try {
            Path path = Paths.get(dir == null || dir.isEmpty() ? DEFAULT_WORKSPACE_DIR : dir);
            WatchKey key = path.register(this.ws, StandardWatchEventKinds.ENTRY_MODIFY);
            keys.putIfAbsent(key, path);

            //  try initialize  schedule to monitor file change event.
            if (this.executor == null || this.future == null) {
                synchronized (this) {
                    if (this.executor == null) {
                        this.executor = scheduledExecutorService == null
                                ? Executors.newScheduledThreadPool(1, r -> new Thread(this, "FileWatcherThread"))
                                : scheduledExecutorService;
                    }
                    if (this.future == null) {
                        this.future = this.executor.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
                    }
                }
            }
            this.isInitialized = true;
        } catch (Exception e) {
            LOGGER.error("watch path failure. path:{}", dir, e);
        }
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
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

    @Override
    public void run() {
        this.process();
    }
}
