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

import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.matcher.WorkerMatcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据生成器、体积报告和 JMH 三方共用的命名与描述符约定。
 */
public final class BenchDatasets {

    /**
     * 1k 行 = 一张小配置表，50k 行 = 一张大配置表。
     * 跨两个数量级足以把"每文件固定成本"（JAXB 建 context、打开 workbook……）
     * 和"每行成本"区分开。
     */
    public static final int[] DEFAULT_ROW_COUNTS = {1_000, 10_000, 50_000};

    public static final String FILE_PREFIX = "bench_item_";

    /**
     * excel 的 sheet 名；同时也就是 protobuf 的 message 名 ——
     * 因为 {@code ProtobufStructHandler} 是从 {@code StructDescriptor#getSheetName()} 解析 message 名的。
     */
    public static final String SHEET_NAME = BenchItemProto.MESSAGE_NAME;

    private BenchDatasets() {
        //  no-op
    }

    public static String fileName(DataFormat format, int rows) {
        return FILE_PREFIX + rows + "." + format.extension;
    }

    public static Path file(Path dir, DataFormat format, int rows) {
        return dir.resolve(fileName(format, rows));
    }

    /**
     * 手工构造出真实 {@code @StructSheet} 会生成的同一个 descriptor。
     *
     * <p>{@code startOrder = 1 / endOrder = -1} 正是注解的默认值，含义是
     * "第 0 行是表头，一直读到 EOF"，而且这个语义对<b>全部</b> handler 都一致：
     * csv / json / protobuf 不跳过任何行，xlsx-sax 与 xls-event 把第 0 行当表头，
     * excel 用户模型从 {@code startOrder - 1} 读表头。</p>
     *
     * <p>之所以手工构造而不是加注解：注解的 {@code fileName} 是编译期常量，
     * 一个 bean 只能绑一个文件名，无法让同一个类服务六种格式 × 三档行数。</p>
     */
    public static StructDescriptor descriptor(DataFormat format, int rows) {
        return new StructDescriptor(fileName(format, rows), SHEET_NAME, 1, -1,
                WorkerMatcher.class, StructBeanFilter.class);
    }

    /**
     * 按生产代码的方式加载一份生成好的数据文件。
     */
    public static List<BenchItem> load(Path dir, DataFormat format, int rows) {
        StructWorker<BenchItem> worker = new StructWorker<>(
                dir.toAbsolutePath().toString(), BenchItem.class, descriptor(format, rows),
                new ConcurrentHashMap<>());
        return worker.toList(ArrayList::new);
    }

    /**
     * @param csv 逗号分隔的行数，例如 {@code "1000,10000"}。
     */
    public static int[] parseRowCounts(String csv) {
        if (csv == null || csv.isBlank()) {
            return DEFAULT_ROW_COUNTS.clone();
        }
        String[] parts = csv.split("[,\\s]+");
        int[] rows = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            rows[i] = Integer.parseInt(parts[i].trim());
        }
        Arrays.sort(rows);
        return rows;
    }

    public static Path defaultDataDir() {
        String property = System.getProperty("bench.dataDir");
        return property != null && !property.isBlank()
                ? Paths.get(property)
                : Paths.get("build", "bench-data");
    }
}
