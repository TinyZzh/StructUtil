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
import org.struct.core.handler.StructHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 在<b>同一个 bean、同一批数据</b>上比较全部数据格式。
 *
 * <p>每个 (格式, 行数) 组合测两个口径：</p>
 * <dl>
 *   <dt>{@link #loadEndToEnd()}</dt>
 *   <dd>生产代码真实付出的成本：创建 {@code StructWorker} → 反射扫描 bean 结构 →
 *       解析文件 → 跑转换器 → 收集 bean。要引用"加载一张配置表要多久"就用这个数。</dd>
 *   <dt>{@link #parseOnly()}</dt>
 *   <dd>用预先建好、已完成 bean 结构扫描的 worker 解析同一个文件。它剔掉了一次性的
 *       反射扫描，把"格式本身的解码成本"单独剥出来 —— 这正是不在该列的
 *       决定性指标。</dd>
 * </dl>
 *
 * <p>{@code xlsx} / {@code xls} 在这里用的是各自的流式 handler
 * （{@code XlsxSaxStructHandler} / {@code XlsEventStructHandler}）；POI 用户模型
 * 另有一个 benchmark，因为它撑不到 5 万行那一档。</p>
 *
 * <p>配合 {@code -prof gc} 还能拿到 {@code gc.alloc.rate.norm}
 * （每次加载分配多少字节），这是衡量 GC 压力最干净的指标。</p>
 *
 * <p><b>关于 {@code jvmArgsAppend} 里的日志开关：</b>JMH 会 fork 出独立的 worker JVM，
 * Gradle {@code JavaExec} 的 {@code jvmArgs} 传不进去，所以静音只能写在这里。
 * 这不是为了输出好看 —— examples 默认的 log4j2.xml 把 {@code org.struct} 设成 DEBUG
 * 且逐行打日志，而 csv handler 每行打一条、protobuf 一条不打，日志 I/O 会被当成格式
 * 开销计入成绩，实测能把差距夸大 30% 以上。</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xmx3g", "-Dlog4j2.configurationFile=log4j2-benchmark.xml"})
@State(Scope.Benchmark)
public class FormatLoadBenchmark {

    @Param({"csv", "json", "xlsx", "xls", "xml", "protobuf"})
    public String format;

    @Param({"1000", "10000", "50000"})
    public int rows;

    private File file;
    private StructDescriptor descriptor;
    /**
     * 生产环境里 handler 是 SPI 单例，这里也只建一次 ——
     * 每次调用都重新 new 一个 {@code Gson}（json）或重新解析 protobuf parser
     * 的话，测到的就不是格式本身的成本了。
     */
    private StructHandler handler;
    /**
     * {@link #parseOnly()} 复用：bean 结构已扫描完毕。
     */
    private StructWorker<BenchItem> sharedWorker;

    @Setup
    public void setup() {
        DataFormat fmt = DataFormat.of(this.format);
        Path dataDir = BenchDatasets.defaultDataDir().toAbsolutePath();
        this.file = BenchDatasets.file(dataDir, fmt, this.rows).toFile();
        if (!this.file.isFile()) {
            throw new IllegalStateException("missing data file: " + this.file.getAbsolutePath()
                    + ". run `gradlew :struct-examples:generateBenchData` first.");
        }
        this.descriptor = BenchDatasets.descriptor(fmt, this.rows);
        this.handler = fmt.defaultHandler();

        this.sharedWorker = new StructWorker<>(dataDir.toString(), BenchItem.class,
                this.descriptor, new ConcurrentHashMap<>());
        this.sharedWorker.checkStructFactory();
    }

    /**
     * 端到端：一次完整的"加载一张配置表"。
     */
    @Benchmark
    public List<BenchItem> loadEndToEnd() {
        StructWorker<BenchItem> worker = new StructWorker<>(this.file.getParent(), BenchItem.class,
                this.descriptor, new ConcurrentHashMap<>());
        return worker.toList(ArrayList::new);
    }

    /**
     * 只测格式解析：worker 与 bean 结构扫描的成本被排除在外。
     */
    @Benchmark
    public void parseOnly(Blackhole bh) {
        CountingConsumer<BenchItem> sink = new CountingConsumer<>(bh);
        this.handler.handle(this.sharedWorker, BenchItem.class, sink, this.file);
        bh.consume(sink.count);
    }

    /**
     * 计数并把每个对象喂给 {@link Blackhole}：既防止 JIT 把整段解析消除掉，
     * 又不像 {@code ArrayList::add} 那样把 list 扩容的成本算进格式开销里。
     */
    private static final class CountingConsumer<T> implements Consumer<T> {

        private final Blackhole bh;
        private int count;

        CountingConsumer(Blackhole bh) {
            this.bh = bh;
        }

        @Override
        public void accept(T t) {
            this.bh.consume(t);
            this.count++;
        }
    }
}
