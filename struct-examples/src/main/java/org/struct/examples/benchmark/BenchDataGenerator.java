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

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 把<b>同一批</b>数据写进每一种格式，让基准比的是格式而不是比的是内容。
 *
 * <pre>
 * gradlew.bat --no-daemon :struct-examples:generateBenchData
 * </pre>
 *
 * <p>生成结束会自动做一次完整性校验：把每个文件用 {@code StructWorker} 读回来，
 * 与源数据逐行比对。handler 若静默丢行或解析错值，会在这里立刻暴露，
 * 而不是变成一个"protobuf 快 20 倍"的假数字。</p>
 */
public final class BenchDataGenerator {

    /**
     * 固定种子 —— 每种格式、每次运行看到的数据完全一致。
     */
    private static final long SEED = 20240917L;

    private static final String[] WORDS = {
            "sword", "shield", "potion", "ring", "armor", "gem", "scroll", "bow",
            "staff", "cloak", "boots", "helm", "rune", "orb", "tome", "amulet"
    };

    /**
     * 会真正写出文件的格式。{@code EXCEL_USER_MODEL} 不单独产出文件 ——
     * 它通过 POI 用户模型读同一份 {@code .xlsx} / {@code .xls}。
     */
    static final DataFormat[] WRITTEN_FORMATS = {
            DataFormat.CSV,
            DataFormat.JSON,
            DataFormat.XLSX,
            DataFormat.XLS,
            DataFormat.XML,
            DataFormat.PROTOBUF,
    };

    private BenchDataGenerator() {
        //  no-op
    }

    public static void main(String[] args) throws Exception {
        Path dir = args.length > 0 ? Path.of(args[0]) : BenchDatasets.defaultDataDir();
        int[] rowCounts = BenchDatasets.parseRowCounts(System.getProperty("bench.rows"));
        generate(dir, rowCounts);
        verify(dir, rowCounts);
    }

    //  ==================================================================
    //  数据
    //  ==================================================================

    /**
     * 确定性生成。没有任何字段取 java / proto3 的默认值。
     */
    public static List<BenchItem> newRows(int rows) {
        Random rnd = new Random(SEED);
        List<BenchItem> list = new ArrayList<>(rows);
        for (int i = 1; i <= rows; i++) {
            BenchItem item = new BenchItem();
            item.id = i;
            item.name = "item_" + String.format("%06d", i);
            item.quality = 1 + rnd.nextInt(5);
            item.price = Math.round((1D + rnd.nextDouble() * 9998D) * 100D) / 100D;
            //  控制在 10 位以内：POI 会把更大的数字渲染成科学计数法，无法原样往返。
            item.expireAt = 1_000_000_000L + Math.floorMod(rnd.nextLong(), 8_999_999_999L);
            item.stackLimit = 1 + rnd.nextInt(999);
            item.rewards = new int[]{1 + rnd.nextInt(9999), 1 + rnd.nextInt(9999), 1 + rnd.nextInt(9999)};
            item.desc = randomDesc(rnd);
            list.add(item);
        }
        return list;
    }

    private static String randomDesc(Random rnd) {
        StringBuilder sb = new StringBuilder(56);
        for (int i = 0; i < 6; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(WORDS[rnd.nextInt(WORDS.length)]);
        }
        return sb.toString();
    }

    /**
     * 文本格式里数组字段的编码方式：{@code 123|456|789}。
     * 与 {@code ArrayConverter} 的默认分隔符（正则 {@code \|}）一致。
     */
    static String joinRewards(int[] rewards) {
        StringBuilder sb = new StringBuilder(rewards.length * 5);
        for (int i = 0; i < rewards.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(rewards[i]);
        }
        return sb.toString();
    }

    //  ==================================================================
    //  生成
    //  ==================================================================

    public static void generate(Path dir, int[] rowCounts) throws Exception {
        Files.createDirectories(dir);
        for (int rows : rowCounts) {
            List<BenchItem> data = newRows(rows);
            for (DataFormat format : WRITTEN_FORMATS) {
                Path file = BenchDatasets.file(dir, format, rows);
                long begin = System.nanoTime();
                write(format, file, data);
                long ms = (System.nanoTime() - begin) / 1_000_000L;
                System.out.printf("generated %-14s rows=%-6d %9d bytes (%d ms)%n",
                        format.label, rows, Files.size(file), ms);
            }
        }
    }

    private static void write(DataFormat format, Path file, List<BenchItem> rows) throws Exception {
        switch (format) {
            case CSV -> writeCsv(file, rows);
            case JSON -> writeJson(file, rows);
            case XLSX -> writeXlsx(file, rows);
            case XLS -> writeXls(file, rows);
            case XML -> writeXml(file, rows);
            case PROTOBUF -> writeProtobuf(file, rows);
            default -> throw new IllegalArgumentException("not a writable format: " + format);
        }
    }

    private static void writeCsv(Path file, List<BenchItem> rows) throws IOException {
        StringBuilder sb = new StringBuilder(rows.size() * 96 + 64);
        sb.append(String.join(",", BenchItem.FIELDS));
        for (BenchItem it : rows) {
            sb.append('\n')
                    .append(it.id).append(',')
                    .append(it.name).append(',')
                    .append(it.quality).append(',')
                    .append(it.price).append(',')
                    .append(it.expireAt).append(',')
                    .append(it.stackLimit).append(',')
                    .append(joinRewards(it.rewards)).append(',')
                    .append(it.desc);
        }
        Files.writeString(file, sb, StandardCharsets.UTF_8);
    }

    private static void writeJson(Path file, List<BenchItem> rows) throws IOException {
        StringBuilder sb = new StringBuilder(rows.size() * 160 + 8);
        sb.append('[');
        for (int i = 0; i < rows.size(); i++) {
            BenchItem it = rows.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(it.id)
                    .append(",\"name\":\"").append(it.name).append('"')
                    .append(",\"quality\":").append(it.quality)
                    .append(",\"price\":").append(it.price)
                    .append(",\"expireAt\":").append(it.expireAt)
                    .append(",\"stackLimit\":").append(it.stackLimit)
                    .append(",\"rewards\":[");
            for (int j = 0; j < it.rewards.length; j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(it.rewards[j]);
            }
            sb.append("],\"desc\":\"").append(it.desc).append("\"}");
        }
        sb.append(']');
        Files.writeString(file, sb, StandardCharsets.UTF_8);
    }

    private static void writeXml(Path file, List<BenchItem> rows) throws IOException {
        StringBuilder sb = new StringBuilder(rows.size() * 220 + 64);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<items>\n");
        for (BenchItem it : rows) {
            sb.append("<item>")
                    .append("<id>").append(it.id).append("</id>")
                    .append("<name>").append(it.name).append("</name>")
                    .append("<quality>").append(it.quality).append("</quality>")
                    .append("<price>").append(it.price).append("</price>")
                    .append("<expireAt>").append(it.expireAt).append("</expireAt>")
                    .append("<stackLimit>").append(it.stackLimit).append("</stackLimit>");
            for (int reward : it.rewards) {
                sb.append("<rewards>").append(reward).append("</rewards>");
            }
            sb.append("<desc>").append(it.desc).append("</desc>")
                    .append("</item>\n");
        }
        sb.append("</items>\n");
        Files.writeString(file, sb, StandardCharsets.UTF_8);
    }

    private static void writeXlsx(Path file, List<BenchItem> rows) throws IOException {
        //  SXSSF 只把窗口内的行留在内存里；否则光是"写"一个 5 万行的 workbook
        //  就要吃掉数 GB 堆。
        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            writeSheet(wb, rows);
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file))) {
                wb.write(os);
            }
            wb.dispose();
        }
    }

    private static void writeXls(Path file, List<BenchItem> rows) throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            writeSheet(wb, rows);
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file))) {
                wb.write(os);
            }
        }
    }

    private static void writeSheet(Workbook wb, List<BenchItem> rows) throws IOException {
        Sheet sheet = wb.createSheet(BenchDatasets.SHEET_NAME);
        Row head = sheet.createRow(0);
        for (int c = 0; c < BenchItem.FIELDS.length; c++) {
            head.createCell(c).setCellValue(BenchItem.FIELDS[c]);
        }
        for (int r = 0; r < rows.size(); r++) {
            BenchItem it = rows.get(r);
            Row row = sheet.createRow(r + 1);
            row.createCell(0).setCellValue(it.id);
            row.createCell(1).setCellValue(it.name);
            row.createCell(2).setCellValue(it.quality);
            row.createCell(3).setCellValue(it.price);
            row.createCell(4).setCellValue((double) it.expireAt);
            row.createCell(5).setCellValue(it.stackLimit);
            row.createCell(6).setCellValue(joinRewards(it.rewards));
            row.createCell(7).setCellValue(it.desc);
        }
    }

    private static void writeProtobuf(Path file, List<BenchItem> rows) throws IOException {
        Descriptors.Descriptor descriptor = BenchItemProto.DESCRIPTOR;
        Descriptors.FieldDescriptor fdId = descriptor.findFieldByName("id");
        Descriptors.FieldDescriptor fdName = descriptor.findFieldByName("name");
        Descriptors.FieldDescriptor fdQuality = descriptor.findFieldByName("quality");
        Descriptors.FieldDescriptor fdPrice = descriptor.findFieldByName("price");
        Descriptors.FieldDescriptor fdExpireAt = descriptor.findFieldByName("expireAt");
        Descriptors.FieldDescriptor fdStack = descriptor.findFieldByName("stackLimit");
        Descriptors.FieldDescriptor fdRewards = descriptor.findFieldByName("rewards");
        Descriptors.FieldDescriptor fdDesc = descriptor.findFieldByName("desc");

        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file))) {
            for (BenchItem it : rows) {
                DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor)
                        .setField(fdId, it.id)
                        .setField(fdName, it.name)
                        .setField(fdQuality, it.quality)
                        .setField(fdPrice, it.price)
                        .setField(fdExpireAt, it.expireAt)
                        .setField(fdStack, it.stackLimit)
                        .setField(fdDesc, it.desc);
                for (int reward : it.rewards) {
                    builder.addRepeatedField(fdRewards, reward);
                }
                //  length-delimited：每行 = 一个 varint 长度前缀 + 一条 message。
                builder.build().writeDelimitedTo(os);
            }
        }
    }

    //  ==================================================================
    //  完整性校验
    //  ==================================================================

    /**
     * 把每个生成的文件读回来与源数据逐行比对。
     * handler 静默丢行 / 解析错值会在这里报错，而不是变成假的性能数字。
     */
    public static void verify(Path dir, int[] rowCounts) {
        System.out.println();
        System.out.printf("%-10s %-8s %-10s %s%n", "FORMAT", "ROWS", "RESULT", "DETAIL");
        boolean allOk = true;
        for (int rows : rowCounts) {
            List<BenchItem> expected = newRows(rows);
            for (DataFormat format : WRITTEN_FORMATS) {
                String result;
                String detail = "";
                try {
                    List<BenchItem> actual = BenchDatasets.load(dir, format, rows);
                    if (actual.size() != rows) {
                        result = "MISMATCH";
                        detail = "expected " + rows + " rows, loaded " + actual.size();
                    } else {
                        String diff = firstDifference(expected, actual);
                        if (diff == null) {
                            result = "OK";
                        } else {
                            result = "MISMATCH";
                            detail = diff;
                        }
                    }
                } catch (Exception e) {
                    result = "ERROR";
                    detail = e.getClass().getSimpleName() + ": " + e.getMessage();
                }
                allOk &= "OK".equals(result);
                System.out.printf("%-10s %-8d %-10s %s%n", format.label, rows, result, detail);
            }
        }
        if (!allOk) {
            throw new IllegalStateException("benchmark data integrity check failed. see the table above.");
        }
        System.out.println("integrity check passed: every format loaded back identical rows.");
    }

    private static String firstDifference(List<BenchItem> expected, List<BenchItem> actual) {
        int size = Math.min(expected.size(), actual.size());
        for (int i = 0; i < size; i++) {
            BenchItem a = expected.get(i);
            BenchItem b = actual.get(i);
            if (a.id != b.id) {
                return "row " + i + " id: expected " + a.id + ", actual " + b.id;
            }
            if (!a.name.equals(b.name)) {
                return "row " + i + " name: expected " + a.name + ", actual " + b.name;
            }
            if (a.quality != b.quality) {
                return "row " + i + " quality: expected " + a.quality + ", actual " + b.quality;
            }
            if (Double.compare(a.price, b.price) != 0) {
                return "row " + i + " price: expected " + a.price + ", actual " + b.price;
            }
            if (a.expireAt != b.expireAt) {
                return "row " + i + " expireAt: expected " + a.expireAt + ", actual " + b.expireAt;
            }
            if (a.stackLimit != b.stackLimit) {
                return "row " + i + " stackLimit: expected " + a.stackLimit + ", actual " + b.stackLimit;
            }
            if (!Arrays.equals(a.rewards, b.rewards)) {
                return "row " + i + " rewards: expected " + Arrays.toString(a.rewards)
                        + ", actual " + Arrays.toString(b.rewards);
            }
            if (!a.desc.equals(b.desc)) {
                return "row " + i + " desc: expected '" + a.desc + "', actual '" + b.desc + "'";
            }
        }
        return null;
    }
}
