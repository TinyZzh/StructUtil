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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * 打印每种格式生成文件的磁盘体积。
 *
 * <p>只比裸字节数会让 {@code .xlsx} / {@code .xls} 占便宜 —— 它们本身就是 deflate
 * 压缩过的容器，拿它们和未压缩的 protobuf 裸文件比并不公平。所以额外给出
 * <b>gzip 之后</b>的体积：protobuf 也压一遍，两者才在同一条线上。
 * 两个 "vs pb" 列回答的是"这个格式是 protobuf 的多少倍"。</p>
 *
 * <pre>
 * gradlew.bat --no-daemon :struct-examples:fileSizeReport
 * </pre>
 */
public final class FileSizeReport {

    private FileSizeReport() {
        //  no-op
    }

    public static void main(String[] args) throws Exception {
        Path dir = args.length > 0 ? Path.of(args[0]) : BenchDatasets.defaultDataDir();
        int[] rowCounts = BenchDatasets.parseRowCounts(System.getProperty("bench.rows"));
        print(dir, rowCounts);
    }

    public static void print(Path dir, int[] rowCounts) throws IOException {
        System.out.println();
        System.out.println("=== data file size (smaller is better; 'vs pb' = x times protobuf) ===");
        for (int rows : rowCounts) {
            long pbBytes = sizeOf(dir, DataFormat.PROTOBUF, rows);
            long pbGzip = gzipSize(dir, DataFormat.PROTOBUF, rows);

            System.out.printf("%nrows = %d%n", rows);
            System.out.printf("%-11s %13s %9s %9s %13s %9s %10s%n",
                    "FORMAT", "bytes", "B/row", "vs pb", "gzip", "gz B/row", "vs pb.gz");
            for (DataFormat format : BenchDataGenerator.WRITTEN_FORMATS) {
                long bytes = sizeOf(dir, format, rows);
                //  已经压缩过的格式不再压第二遍，直接复用原始大小。
                long gzip = format.isCompressedOnDisk() ? bytes : gzipSize(dir, format, rows);
                System.out.printf("%-11s %13d %9.1f %8.2fx %13d %9.1f %9.2fx%n",
                        format.label + (format.isCompressedOnDisk() ? "*" : ""),
                        bytes,
                        bytes / (double) rows,
                        bytes / (double) pbBytes,
                        gzip,
                        gzip / (double) rows,
                        gzip / (double) pbGzip);
            }
            System.out.printf("%-11s %13d %9.1f %8.2fx%n",
                    "pb(gzip)", pbGzip, pbGzip / (double) rows, 1.0D);
        }
        System.out.println();
        System.out.println("* xlsx/xls 在磁盘上已经是 deflate 压缩的，其 gzip 列直接复用原始大小，不重复压缩。");
    }

    private static long sizeOf(Path dir, DataFormat format, int rows) throws IOException {
        Path file = BenchDatasets.file(dir, format, rows);
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("missing data file: " + file + ". run generateBenchData first.");
        }
        return Files.size(file);
    }

    /**
     * @return 默认压缩级别 gzip 之后的大小。
     */
    private static long gzipSize(Path dir, DataFormat format, int rows) throws IOException {
        Path file = BenchDatasets.file(dir, format, rows);
        Counter counter = new Counter();
        try (InputStream in = Files.newInputStream(file);
             GZIPOutputStream gzip = new GZIPOutputStream(counter)) {
            in.transferTo(gzip);
        }
        return counter.size;
    }

    private static final class Counter extends OutputStream {

        private long size;

        @Override
        public void write(int b) {
            this.size++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            this.size += len;
        }
    }
}
