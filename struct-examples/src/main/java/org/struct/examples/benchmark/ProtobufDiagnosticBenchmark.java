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

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
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
import org.struct.core.StructWorker;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 回答"protobuf 为什么慢" —— 把 {@code ProtobufStructHandler} 的读取路径逐层拆开。
 *
 * <p>{@code FormatLoadBenchmark} 显示 protobuf 在六种格式里只排第 4，比 csv 还慢近 3 倍，
 * 这与"二进制格式更快"的直觉相悖。本基准把 I/O 层与解码层分开测量，定位瓶颈到底在哪：</p>
 *
 * <dl>
 *   <dt>{@link #readRawBytes()}</dt>
 *   <dd>下界：只读字节，完全不解析。任何格式的解析都不可能比它更快。</dd>
 *   <dt>{@link #skipMessages()}</dt>
 *   <dd>解码下界：走 {@code CodedInputStream} 逐条读长度前缀并跳过内容，
 *       不构造任何 {@code DynamicMessage}。与上一项的差值 = 扫描 protobuf 结构本身的成本。</dd>
 *   <dt>{@link #parseUnbuffered()}</dt>
 *   <dd><b>当前生产实现</b>：{@code new FileInputStream(file)} 直接喂给
 *       {@code parser.parseDelimitedFrom(fis)}。{@code FileInputStream} 是无缓冲的，
 *       而 {@code parseDelimitedFrom} 会先逐字节读 varint 长度、再分多次读消息体 ——
 *       每一条消息对应多次 {@code read()} 系统调用。</dd>
 *   <dt>{@link #parseBuffered()}</dt>
 *   <dd>把上一条的流包上 {@code BufferedInputStream}，其余完全不变。
 *       与上一项的差值 = 无缓冲 I/O 造成的系统调用开销。</dd>
 *   <dt>{@link #parseInMemory()}</dt>
 *   <dd>文件一次性读进 {@code byte[]} 再解码，是最理想的 I/O 形态。
 *       与上一项的差值 = 剩余的缓冲复制成本。</dd>
 * </dl>
 *
 * <p>注意：这几项都<b>不包含</b> bean 字段绑定（那是 {@code FormatLoadBenchmark}
 * 的口径），只测"从文件到 Message"这一段。</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xmx3g", "-Dlog4j2.configurationFile=log4j2-benchmark.xml"})
@State(Scope.Benchmark)
public class ProtobufDiagnosticBenchmark {

    @Param({"1000", "10000"})
    public int rows;

    private File file;
    private byte[] fileBytes;
    private Parser<Message> parser;

    @Setup
    public void setup() throws Exception {
        Path dataDir = BenchDatasets.defaultDataDir().toAbsolutePath();
        this.file = BenchDatasets.file(dataDir, DataFormat.PROTOBUF, this.rows).toFile();
        if (!this.file.isFile()) {
            throw new IllegalStateException("missing data file: " + this.file.getAbsolutePath()
                    + ". run `gradlew :struct-examples:generateBenchData` first.");
        }
        Descriptors.Descriptor descriptor = BenchItemProto.DESCRIPTOR;
        @SuppressWarnings("unchecked")
        Parser<Message> p = (Parser<Message>) (Parser<?>)
                DynamicMessage.getDefaultInstance(descriptor).getParserForType();
        this.parser = p;
        this.fileBytes = Files.readAllBytes(this.file.toPath());
    }

    /**
     * 下界：只把字节读出来，不解析。
     */
    @Benchmark
    public void readRawBytes(Blackhole bh) throws IOException {
        byte[] bytes = Files.readAllBytes(this.file.toPath());
        bh.consume(bytes);
    }

    /**
     * 解码下界：扫描每条消息的长度前缀并跳过内容，不构造 DynamicMessage。
     */
    @Benchmark
    public void skipMessages(Blackhole bh) throws IOException {
        CodedInputStream cis = CodedInputStream.newInstance(this.fileBytes);
        int count = 0;
        while (!cis.isAtEnd()) {
            int length = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(length);
            cis.skipRawBytes(length);
            cis.popLimit(oldLimit);
            count++;
        }
        bh.consume(count);
    }

    /**
     * 当前生产实现：无缓冲的 FileInputStream。
     */
    @Benchmark
    public void parseUnbuffered(Blackhole bh) throws IOException {
        bh.consume(parse(new FileInputStream(this.file)));
    }

    /**
     * 加了缓冲，其余与生产实现一致。
     */
    @Benchmark
    public void parseBuffered(Blackhole bh) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(this.file))) {
            bh.consume(parse(in));
        }
    }

    /**
     * 最理想的 I/O 形态：全量驻留内存再解码。
     */
    @Benchmark
    public void parseInMemory(Blackhole bh) throws IOException {
        bh.consume(parse(new ByteArrayInputStream(this.fileBytes)));
    }

    private int parse(InputStream in) throws IOException {
        int count = 0;
        try (in) {
            while (true) {
                Message msg = this.parser.parseDelimitedFrom(in);
                if (msg == null) {
                    break;
                }
                count++;
            }
        }
        return count;
    }

    //  ==================================================================
    //  DynamicMessage  vs  protoc 生成的 Message 类
    //  ==================================================================

    /**
     * 格式上限：用等价于 protoc 生成的手写 Message 类解码，走
     * {@code ProtobufStructHandler} 的路径 A —— 解析结果<b>就是</b> bean，
     * 既不经 {@code DynamicMessage}，也不经 {@code StructImpl} 中间层与反射填字段。
     *
     * <p>它衡量的是"protobuf 这个二进制格式本身能有多快"，与 {@link #parseInMemory()}
     * 的差值就是通用 descriptor 解码器付出的代价。</p>
     */
    @Benchmark
    public void parseGeneratedMessage(Blackhole bh) throws IOException {
        Parser<BenchItemMessage> p = BenchItemMessage.parser();
        int count = 0;
        try (InputStream in = new ByteArrayInputStream(this.fileBytes)) {
            while (true) {
                BenchItemMessage msg = p.parseDelimitedFrom(in);
                if (msg == null) {
                    break;
                }
                bh.consume(msg.getId());
                count++;
            }
        }
        bh.consume(count);
    }

    /**
     * 同 {@link #parseGeneratedMessage()}，但换成无缓冲流。
     * 把"I/O 缓冲"与"解码器选择"两个因素彻底分开：
     * 即便解码器换成最快的生成类，无缓冲 I/O 依然会拖垮它。
     */
    @Benchmark
    public void parseGeneratedUnbuffered(Blackhole bh) throws IOException {
        Parser<BenchItemMessage> p = BenchItemMessage.parser();
        int count = 0;
        try (InputStream in = new FileInputStream(this.file)) {
            while (true) {
                BenchItemMessage msg = p.parseDelimitedFrom(in);
                if (msg == null) {
                    break;
                }
                bh.consume(msg.getId());
                count++;
            }
        }
        bh.consume(count);
    }

    //  ==================================================================
    //  路径 A 端到端：这才是 pb 生成类在框架里的真实成绩
    //  ==================================================================

    /**
     * <b>路径 A 端到端</b>：用 {@code StructWorker} 加载 pb 文件，目标类
     * {@link BenchItemMessage} 本身就是 {@code Message} 子类，因此
     * {@code ProtobufStructHandler} 走路径 A ——
     * 解析结果<b>直接就是 bean</b>，不经过 {@code DynamicMessage} 通用解码，
     * 也没有 {@code StructImpl} 中间层与反射填字段。
     *
     * <p>这是唯一能和 {@code FormatLoadBenchmark#loadEndToEnd} 的 csv / json 数字
     * 直接对比的口径，也就回答了"pb 生成类到底能有多快"。</p>
     */
    @Benchmark
    public List<BenchItemMessage> loadEndToEndPathA() {
        StructWorker<BenchItemMessage> worker = new StructWorker<>(
                this.file.getParent(), BenchItemMessage.class,
                BenchDatasets.fileName(DataFormat.PROTOBUF, this.rows));
        return worker.toList(ArrayList::new);
    }
}
