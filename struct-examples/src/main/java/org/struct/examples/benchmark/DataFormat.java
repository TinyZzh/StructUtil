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

import org.struct.core.handler.CsvStructHandler;
import org.struct.core.handler.ExcelUMStructHandler;
import org.struct.core.handler.JsonStructHandler;
import org.struct.core.handler.ProtobufStructHandler;
import org.struct.core.handler.StructHandler;
import org.struct.core.handler.XlsEventStructHandler;
import org.struct.core.handler.XlsxSaxStructHandler;
import org.struct.spring.handler.XmlStructHandler;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 基准覆盖的全部数据格式。
 *
 * <p>{@link #defaultHandler()} 是文件足够小时 {@code StructWorker} 自动匹配会选中的
 * handler；{@link #EXCEL_USER_MODEL} 单独一个 benchmark，因为 POI 用户模型会把整个
 * workbook 读进内存，撑不到 50k 行那一档。</p>
 */
public enum DataFormat {

    CSV("csv", CsvStructHandler::new),
    JSON("json", JsonStructHandler::new),
    XLSX("xlsx", XlsxSaxStructHandler::new),
    XLS("xls", XlsEventStructHandler::new),
    XML("xml", XmlStructHandler::new),
    PROTOBUF("bin", ProtobufStructHandler::new),
    /**
     * POI 用户模型（全量 DOM），读的是同一份 {@code .xlsx} / {@code .xls} 文件。
     */
    EXCEL_USER_MODEL("xlsx", ExcelUMStructHandler::new),
    ;

    /**
     * 生成的数据文件扩展名。protobuf 用 {@code .bin}，
     * 它是 {@code ProtobufStructHandler} 声明的三个扩展名之一（另两个是 .protobuf / .pbf）。
     */
    public final String extension;
    /**
     * 报告与 JMH {@code @Param} 里使用的名字。
     */
    public final String label;

    private final Supplier<StructHandler> handlerFactory;

    DataFormat(String extension, Supplier<StructHandler> handlerFactory) {
        this.extension = extension;
        this.label = name().toLowerCase();
        this.handlerFactory = handlerFactory;
    }

    /**
     * 一个新的 handler 实例。handler 除了缓存之外是无状态的，
     * 每次 trial 新建可以让各次测量互相独立。
     */
    public StructHandler defaultHandler() {
        return this.handlerFactory.get();
    }

    /**
     * 该格式的载荷在磁盘上是否已经是压缩的
     * （{@code .xlsx} / {@code .xls} 内部是 deflate 过的 xml / BIFF 记录）。
     */
    public boolean isCompressedOnDisk() {
        return this == XLSX || this == XLS;
    }

    public static DataFormat of(String name) {
        for (DataFormat format : values()) {
            if (format.label.equalsIgnoreCase(name) || format.name().equalsIgnoreCase(name)) {
                return format;
            }
        }
        throw new IllegalArgumentException("unknown data format: " + name
                + ", candidates: " + Arrays.toString(values()));
    }
}
