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

package org.struct.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructImpl;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * basic separator file struct handle.
 *
 * @author TinyZ.
 * @date 2020-08-19.
 * @see CsvStructHandler
 */
public abstract class RowWithSeparatorStructHandler extends MultipleRowsStructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowWithSeparatorStructHandler.class);

    public final String separator;

    public RowWithSeparatorStructHandler(String separator) {
        this.separator = separator;
    }

    @Override
    protected <T> String[] resolveColumnNameArray(Class<T> clzOfStruct, String line) {
        return Stream.of(line.split(separator)).map(String::trim).toArray(String[]::new);
    }

    @Override
    protected <T> Object processRow(Class<T> clzOfStruct, String[] columns, String line) {
        String[] split = line.split(separator);
        StructImpl impl = new StructImpl();
        IntStream.range(0, split.length).forEach(i -> {
            String d = split[i];
            impl.add(columns[i], d.isEmpty() ? d : d.trim());
        });
        LOGGER.debug("class :{}, struct:{}", clzOfStruct, impl);
        return impl;
    }
}
