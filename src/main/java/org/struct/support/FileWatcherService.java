/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Simple file watcher service implement.
 * <p>
 * Monitor registered dir or file change event and invoke custom hook to handle it.
 * <strong>Only {@link StandardWatchEventKinds#ENTRY_MODIFY} event<strong/>
 */
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
     * {@link WatchKey} - {@link Path} map.
     */
    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    /**
     * the file change event hook map.
     */
    private final ConcurrentHashMap<Path, Runnable> hookMap;
    /**
     * Set the scheduled job's initial delay.
     */
    private long scheduleInitialDelay = 10L;
    /**
     * Set the scheduled job's delay.
     */
    private long scheduleDelay = 5L;
    /**
     * Set schedule job's {@link TimeUnit}
     */
    private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;

    private volatile boolean isInitialized = false;

    private ScheduledExecutorService executor;

    private ScheduledFuture<?> future;

    static {
        DEFAULT_WORKSPACE_DIR = System.getProperty("struct.workspace.dir", "./data/");
    }

    public FileWatcherService() throws IOException {
        this.ws = FileSystems.getDefault().newWatchService();
        this.hookMap = new ConcurrentHashMap<>();
    }

    /**
     * Bootstrap service.
     */
    public void bootstrap() {
        synchronized (this) {
            if (!isInitialized) {
                this.startScheduledThread();
            }
        }
    }

    public FileWatcherService startScheduledThread() {
        return this.startScheduledThread(Executors.newScheduledThreadPool(1, r -> new Thread(r, "FileWatcherThread")));
    }

    public FileWatcherService startScheduledThread(ScheduledExecutorService scheduledExecutorService) {
        assert null != scheduledExecutorService;
        synchronized (this) {
            if (this.executor == null) {
                this.executor = scheduledExecutorService;
                this.future = this.executor.scheduleWithFixedDelay(this, this.scheduleInitialDelay, this.scheduleDelay, this.scheduleTimeUnit);
                this.isInitialized = true;
            }
        }
        return this;
    }

    public FileWatcherService register(String dir) throws IOException {
        Objects.requireNonNull(dir, "dir");
        return this.register(Paths.get(dir.isEmpty() ? DEFAULT_WORKSPACE_DIR : dir));
    }

    public FileWatcherService register(Path dir) throws IOException {
        Objects.requireNonNull(dir, "dir");
        WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.putIfAbsent(key, dir);
        return this;
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private FileWatcherService registerAll(final Path start) throws IOException {
        Objects.requireNonNull(start, "start");
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return this;
    }

    public FileWatcherService registerHook(String fileName, Runnable hook) {
        return this.registerHook(Paths.get(fileName), hook);
    }

    public FileWatcherService registerHook(Path path, Runnable hook) {
        this.hookMap.putIfAbsent(path, hook);
        return this;
    }

    public FileWatcherService deregisterHook(String fileName) {
        return this.deregisterHook(Paths.get(fileName));
    }

    public FileWatcherService deregisterHook(Path path) {
        this.hookMap.remove(path);
        return this;
    }

    public FileWatcherService setScheduleInitialDelay(long scheduleInitialDelay) {
        if (scheduleInitialDelay <= 0)
            throw new IllegalArgumentException("the scheduleInitialDelay must be large than zero.");
        this.scheduleInitialDelay = scheduleInitialDelay;
        return this;
    }

    public FileWatcherService setScheduleDelay(long scheduleDelay) {
        if (scheduleDelay <= 0)
            throw new IllegalArgumentException("the scheduleDelay must be large than zero.");
        this.scheduleDelay = scheduleDelay;
        return this;
    }

    public FileWatcherService setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
        Objects.requireNonNull(scheduleTimeUnit, "scheduleTimeUnit");
        this.scheduleTimeUnit = scheduleTimeUnit;
        return this;
    }

    /**
     * Schedule process the file watch events.
     */
    private void process() {
        try {
            WatchKey key = ws.poll();
            if (key == null) {
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                LOGGER.info("watch key not registered. key:{}", key);
                return;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);
                try {
                    Runnable runnable = hookMap.get(child);
                    if (runnable != null) {
                        runnable.run();
                    }
                } catch (Exception e) {
                    LOGGER.error("process data file failure. file:{}", child.toAbsolutePath(), e);
                    throw e;
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                this.keys.remove(key);
            }
        } catch (Throwable e) {
            LOGGER.error("file watcher service throw an unknown exception.", e);
        }
    }

    @Override
    public void run() {
        this.process();
    }
}
