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

/**
 * 数据格式基准测试：csv / json / xlsx / xls / xml / protobuf 的横向对比。
 *
 * <p>所有格式都反序列化成同一个 {@link org.struct.examples.benchmark.BenchItem}，
 * 并且数据由固定种子生成，因此字段数量、字段类型、转换器链路、反射建实例的成本
 * 完全相同 —— 唯一的变量只有数据格式本身。</p>
 *
 * <h2>怎么跑</h2>
 * <pre>
 * # 1) 生成数据（并自动校验六种格式读回来完全一致）
 * gradlew.bat --no-daemon :struct-examples:generateBenchData
 *
 * # 2) 只看体积
 * gradlew.bat --no-daemon :struct-examples:fileSizeReport
 *
 * # 3) 跑 JMH（默认全量，带 -prof gc）
 * gradlew.bat --no-daemon :struct-examples:benchmark
 *
 * # 只跑某个基准 / 某个参数
 * gradlew.bat --% --no-daemon :struct-examples:benchmark -Pbench.jmh="FormatLoadBenchmark -p rows=10000"
 * </pre>
 *
 * <p>可用 {@code -Pbench.rows=1000,10000} 缩小数据档位，用 {@code -Pbench.jmh="..."}
 * 完全接管 JMH 参数。</p>
 *
 * <h2>三个基准各自回答什么</h2>
 * <dl>
 *   <dt>{@link org.struct.examples.benchmark.FormatLoadBenchmark}</dt>
 *   <dd>主力对比表。每个 (格式, 行数) 测两个口径：{@code loadEndToEnd} 是生产代码
 *       真实付出的成本；{@code parseOnly} 剔除一次性的 bean 结构扫描，只比格式解码。</dd>
 *   <dt>{@link org.struct.examples.benchmark.ProtobufDiagnosticBenchmark}</dt>
 *   <dd>回答"protobuf 为什么慢/快"。把 pb 的读取路径逐层拆开（裸读字节 → 扫描结构 →
 *       DynamicMessage → 生成类），并给出路径 A 的端到端成绩。</dd>
 *   <dt>{@link org.struct.examples.benchmark.ExcelUserModelBenchmark}</dt>
 *   <dd>POI 用户模型的对照。它把整个 workbook 读进内存，撑不到 5 万行，单独测。</dd>
 * </dl>
 *
 * <h2>结论（10k 行，本机实测；JDK 25 / JMH 1.37）</h2>
 * <ol>
 *   <li><b>体积上 pb 几乎没有优势。</b>本数据集里 {@code desc} 字段（约 40 字节随机文本）
 *       占了绝大部分，而字符串在 pb 里原样存 UTF-8，varint 只省下数字部分。
 *       10k 行裸文件：pb 79.5 B/row，csv 89.9 B/row（仅 1.13x 差距）；
 *       gzip 之后 csv 反而 33.5 B/row &lt; pb 34.9 B/row（0.96x）。
 *       <b>pb 的体积优势只在“数据以数值为主、且不做二次压缩”时才明显。</b>
 *       （1k / 50k 两档趋势一致。）</li>
 *   <li><b>Path B（普通 POJO）的 pb 只是略快于 csv，优势可忽略。</b>目标 bean 是普通
 *       POJO 时走路径 B：{@code DynamicMessage} 通用解码 + {@code SingleFieldDescriptor}
 *       直接从 {@code Message} 读字段（<b>没有</b> {@code StructImpl} 中间层）。
 *       10k 行端到端 protobuf 12.7ms vs csv 14.8ms，<b>pb 仅快 ~14%</b>；
 *       1k 行时两者几乎持平（1.43 vs 1.63）。<b>不要为了“pb 更快”而把普通 POJO
 *       改存 pb —— 收益太小，还牺牲了可读性与调试便利性。</b></li>
 *   <li><b>换成 protoc 生成的类（路径 A）后，pb 才显现真正的优势：端到端 3.0ms，
 *       比 csv 14.8ms 快 4.9 倍。</b>此时解析结果直接就是 bean，没有 DynamicMessage
 *       的通用解码与逐字段读取开销。{@code ProtobufDiagnosticBenchmark} 显示路径 A
 *       纯解码 2.5ms，与端到端 3.0ms 只差 0.5ms，便是明证。</li>
 *   <li><b>历史上最大的瓶颈其实是无缓冲 I/O，与用不用生成类完全正交。</b>
 *       {@code parseDelimitedFrom} 逐字节读 varint 长度、再分多次读消息体，在裸
 *       {@code FileInputStream} 上每条消息都要好几次 {@code read()} 系统调用。
 *       {@code ProtobufDiagnosticBenchmark}：无缓冲 30.4ms vs 缓冲 6.7ms（4.5 倍）；
 *       路径 A 无缓冲 25.4ms vs 缓冲 2.5ms（10 倍）。仅“包一层 BufferedInputStream”
 *       这一项，就把 10k 行从 31.0ms 降到 7.1ms（4.3 倍）。该修复已合入
 *       {@code ProtobufStructHandler}。</li>
 *   <li><b>Excel 用户模型只适合小文件。</b>{@code ExcelUserModelBenchmark}：xlsx 10k=172ms、
 *       xls 10k=59ms，且整本 workbook 读进内存、撑不到 5 万行就会 OOM。生产代码走流式
 *       handler（SAX/Event），{@code FormatLoadBenchmark} 里 xlsx 10k=170ms、xls 10k=95ms
 *       已是更优路径，但仍比 csv/json 慢一个数量级。</li>
 * </ol>
 *
 * <h2>读数字时的两个警告</h2>
 * <ul>
 *   <li><b>必须静音日志。</b>examples 默认的 log4j2.xml 把 {@code org.struct} 设为 DEBUG
 *       且逐行打日志，而 csv handler 每行打一条、protobuf 一条不打 —— 日志 I/O 会被当成
 *       格式开销计入成绩，实测能把差距夸大 30% 以上。因为 JMH 会 fork 独立 JVM，
 *       Gradle 的 {@code jvmArgs} 传不进去，静音开关写在各个 {@code @Fork(jvmArgsAppend)} 里。</li>
 *   <li><b>Path B（{@code DynamicMessage}）是 pb 的“慢路径”，不是真实能力。</b>
 *       普通 POJO 走它；要评估 pb 的真实上限请看
 *       {@code ProtobufDiagnosticBenchmark#loadEndToEndPathA} 与
 *       {@link org.struct.examples.benchmark.BenchItemMessage}（protoc 生成类）。</li>
 * </ul>
 */
package org.struct.examples.benchmark;
