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

package org.struct.examples.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.handler.ExcelUMStructHandler;
import org.struct.core.handler.StructHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * POI 用户模型（{@code ExcelUMStructHandler}）的对照基准。
 *
 * <p>它把整个 workbook 读进内存再逐行取值，支持的 excel 特性最全，
 * 代价是内存与耗时都随行数线性（甚至超线性）增长，因此这里只跑到 1 万行。</p>
 *
 * <p><b>两个生产环境必须知道的事实</b>（都能从这个 benchmark 的边界条件看出来）：</p>
 * <ol>
 *   <li>{@code ExcelUMStructHandler} 的 matcher 带 1.5MB 文件大小上限
 *       （{@code StructInternal.HANDLER_XLSX_UM_LENGTH_THRESHOLD}），
 *       超过就<b>不会</b>被自动匹配上，{@code StructWorker} 会降级到流式 handler。
 *       只有直接调用 {@code handler.handle(...)} 才会绕过这个阈值 —— 本 benchmark 正是这样。</li>
 *   <li>它的 order 是 {@code HIGHEST}，即在阈值内会被<b>优先</b>选中。
 *       换句话说：小 excel 走的是这个最慢的实现，大 excel 反而走快的流式实现。</li>
 * </ol>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xmx3g"})
@State(Scope.Benchmark)
public class ExcelUserModelBenchmark {

    /**
     * {@code .xls}（HSSF 用户模型）比 {@code .xlsx} 更慢更吃内存，两个都测。
     */
    @Param({"xlsx", "xls"})
    public String excelType;

    @Param({"1000", "10000"})
    public int rows;

    private File file;
    private StructHandler handler;
    private StructWorker<BenchItem> sharedWorker;

    @Setup
    public void setup() {
        DataFormat fmt = DataFormat.of(this.excelType);
        Path dataDir = BenchDatasets.defaultDataDir().toAbsolutePath();
        this.file = BenchDatasets.file(dataDir, fmt, this.rows).toFile();
        if (!this.file.isFile()) {
            throw new IllegalStateException("missing data file: " + this.file.getAbsolutePath()
                    + ". run `gradlew :struct-examples:generateBenchData` first.");
        }
        StructDescriptor descriptor = BenchDatasets.descriptor(fmt, this.rows);
        this.handler = new ExcelUMStructHandler();
        this.sharedWorker = new StructWorker<>(dataDir.toString(), BenchItem.class,
                descriptor, new ConcurrentHashMap<>());
        this.sharedWorker.checkStructFactory();
    }

    @Benchmark
    public void parseOnly(Blackhole bh) {
        Consumer<BenchItem> sink = bh::consume;
        this.handler.handle(this.sharedWorker, BenchItem.class, sink, this.file);
    }
}
