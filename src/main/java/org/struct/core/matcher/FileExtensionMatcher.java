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

package org.struct.core.matcher;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class FileExtensionMatcher implements WorkerMatcher {
    private static final long serialVersionUID = 4620761370006132284L;

    public static final String FILE_XLSX = ".xlsx";
    public static final String FILE_XLS = ".xls";
    public static final String FILE_JSON = ".json";
    public static final String FILE_XML = ".xml";
    public static final String FILE_BINARY = ".binary";

    /**
     * 可处理的文件大小的阀值
     */
    private final long fileLengthThreshold;
    /**
     * 可匹配的文件扩展名列表
     */
    private final String[] fileExtensionAry;
    /**
     * The matcher's order.
     */
    private final int order;

    public FileExtensionMatcher(String... fileExtensionAry) {
        this(-1, LOWEST, fileExtensionAry);
    }

    public FileExtensionMatcher(long fileLengthThreshold, String... fileExtensionAry) {
        this(fileLengthThreshold, LOWEST, fileExtensionAry);
    }

    public FileExtensionMatcher(int order, String... fileExtensionAry) {
        this(-1, order, fileExtensionAry);
    }

    public FileExtensionMatcher(long fileLengthThreshold, int order, String... fileExtensionAry) {
        super();
        this.fileLengthThreshold = fileLengthThreshold;
        this.order = order;
        if (fileExtensionAry == null || fileExtensionAry.length < 1)
            throw new IllegalArgumentException("fileExtensionAry must have one element at least");
        this.fileExtensionAry = fileExtensionAry;
    }

    @Override
    public int order() {
        return this.order;
    }

    @Override
    public boolean matchFile(File file) {
        if (null == file
                || !file.exists() || !file.canRead())
            return false;
        if (this.fileLengthThreshold > 0 && file.length() >= this.fileLengthThreshold)
            return false;
        for (String fileExtension : fileExtensionAry) {
            if (file.getName().endsWith(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "FileExtensionMatcher{" +
                "fileLengthThreshold=" + fileLengthThreshold +
                ", fileExtensionAry=" + Arrays.toString(fileExtensionAry) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileExtensionMatcher that = (FileExtensionMatcher) o;
        return fileLengthThreshold == that.fileLengthThreshold &&
                Arrays.equals(fileExtensionAry, that.fileExtensionAry);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), fileLengthThreshold);
        result = 31 * result + Arrays.hashCode(fileExtensionAry);
        return result;
    }
}
