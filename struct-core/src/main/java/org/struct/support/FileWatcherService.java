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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * <strong>Only {@link StandardWatchEventKinds#ENTRY_MODIFY} event</strong>
 */
public class FileWatcherService implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherService.class);

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Watch file changed and load template data when file modified.
     */
    private final WatchService ws;
    /**
     * Set the scheduled job's initial delay.
     */
    private final long scheduleInitialDelay;
    /**
     * Set the scheduled job's delay.
     */
    private final long scheduleDelay;
    /**
     * Set schedule job's {@link TimeUnit}
     */
    private final TimeUnit scheduleTimeUnit;

    private final ScheduledExecutorService executor;

    //  =============== Dynamic Fields ====================

    /**
     * {@link WatchKey} - {@link Path} map.
     */
    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    /**
     * the file change event hook map.
     */
    private final ConcurrentHashMap<Path, List<Runnable>> hooksMap = new ConcurrentHashMap<>();

    private volatile ScheduledFuture<?> future;

    FileWatcherService(WatchService ws, long scheduleInitialDelay, long scheduleDelay, TimeUnit scheduleTimeUnit, ScheduledExecutorService executor) {
        this.ws = ws;
        this.scheduleInitialDelay = scheduleInitialDelay;
        this.scheduleDelay = scheduleDelay;
        this.scheduleTimeUnit = scheduleTimeUnit;
        this.executor = executor;
    }

    /**
     * Bootstrap service.
     */
    public void bootstrap() {
        if (null != this.future)
            throw new IllegalStateException("File watcher service is running.");
        synchronized (this) {
            if (null == this.future) {
                this.future = this.executor.scheduleWithFixedDelay(this, this.scheduleInitialDelay, this.scheduleDelay, this.scheduleTimeUnit);
            }
        }
    }

    public FileWatcherService register(String dir) throws IOException {
        return this.register(Paths.get(Objects.requireNonNull(dir, "dir")));
    }

    public FileWatcherService register(Path dir) throws IOException {
        Objects.requireNonNull(dir, "dir");
        WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        Path p = keys.putIfAbsent(key, dir);
        if (null == p) {
            LOGGER.info("Register file watcher service. path: {}", dir.toAbsolutePath());
        }
        return this;
    }

    public FileWatcherService registerAll(String path) throws IOException {
        return this.registerAll(Paths.get(path));
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    public FileWatcherService registerAll(final Path start) throws IOException {
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
        List<Runnable> list = this.hooksMap.computeIfAbsent(path, p -> Collections.synchronizedList(new ArrayList<>()));
        list.add(hook);
        LOGGER.info("Register file hook. path: {}", path.toAbsolutePath());
        return this;
    }

    public FileWatcherService deregisterHook(String fileName) {
        return this.deregisterHook(Paths.get(fileName));
    }

    public FileWatcherService deregisterHook(Path path) {
        List<Runnable> l = this.hooksMap.remove(path);
        if (null != l) {
            LOGGER.info("Deregister file hook. path: {}", path.toAbsolutePath());
        }
        return this;
    }

    /**
     * Schedule process the file watch events.
     */
    private void process() {
        try {
            WatchKey key;
            Path dir;
            if ((key = ws.poll()) == null
                    || (dir = keys.get(key)) == null) {
                LOGGER.debug("watch key not registered. key:{}", key);
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
                List<Runnable> l = hooksMap.get(child);
                if (l != null) {
                    try {
                        l.forEach(Runnable::run);
                    } catch (Exception e) {
                        LOGGER.error("process data file failure. file:{}", child.toAbsolutePath(), e);
                        throw e;
                    }
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

    public final static class Builder {
        private WatchService ws;
        private long scheduleInitialDelay = 10L;
        private long scheduleDelay = 5L;
        private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;
        private ScheduledExecutorService executor;

        public Builder setWatchService(WatchService ws) {
            this.ws = ws;
            return this;
        }

        public Builder setScheduleInitialDelay(long scheduleInitialDelay) {
            if (scheduleInitialDelay <= 0)
                throw new IllegalArgumentException("the scheduleInitialDelay must be large than zero.");
            this.scheduleInitialDelay = scheduleInitialDelay;
            return this;
        }

        public Builder setScheduleDelay(long scheduleDelay) {
            if (scheduleDelay <= 0)
                throw new IllegalArgumentException("the scheduleDelay must be large than zero.");
            this.scheduleDelay = scheduleDelay;
            return this;
        }

        public Builder setScheduleTimeUnit(TimeUnit scheduleTimeUnit) {
            this.scheduleTimeUnit = Objects.requireNonNull(scheduleTimeUnit, "scheduleTimeUnit");
            return this;
        }

        public Builder setExecutor(ScheduledExecutorService executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        public FileWatcherService build() throws IOException {
            WatchService ws = this.ws == null ? FileSystems.getDefault().newWatchService() : this.ws;
            ScheduledExecutorService executor = this.executor == null
                    ? Executors.newScheduledThreadPool(1, r -> new Thread(r, "StructFileWatcherThread"))
                    : this.executor;
            return new FileWatcherService(ws, scheduleInitialDelay, scheduleDelay, scheduleTimeUnit, executor);
        }
    }
}
