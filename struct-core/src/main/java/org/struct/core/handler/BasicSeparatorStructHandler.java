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
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.exception.StructTransformException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * basic separator file struct handle.
 *
 * @author TinyZ.
 * @date 2020-08-19.
 * @see CsvStructHandler
 */
public abstract class BasicSeparatorStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSeparatorStructHandler.class);

    public final String separator;

    public BasicSeparatorStructHandler(String separator) {
        this.separator = separator;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            //  column name. read the first line.
            String[] columns = Stream.of(reader.readLine().split(separator)).map(String::trim).toArray(String[]::new);
            //  start load data
            int startOrder = descriptor.getStartOrder();
            int endOrder = descriptor.getEndOrder();
            int skip = Math.max(startOrder - 1, 0);
            //  end order must be large than start order.
            int limit = endOrder < startOrder ? Integer.MAX_VALUE : (endOrder - skip);
            reader.lines().skip(skip)
                    .map(line -> {
                        String[] split = line.split(separator);
                        StructImpl impl = new StructImpl();
                        IntStream.range(0, split.length).forEach(i -> impl.add(columns[i], split[i]));
                        return impl;
                    })
                    .limit(limit)
                    .forEach(rowStruct -> worker.createInstance(rowStruct).ifPresent(cellHandler));
        } catch (Exception e) {
            LOGGER.info("load file with separator:{} failure. this:{}", this.separator, getClass().getName(), e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }
}
