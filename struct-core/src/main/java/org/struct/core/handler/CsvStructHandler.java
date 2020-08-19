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
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * *.Csv file start handle.
 *
 * @author TinyZ.
 * @date 2020-08-19.
 */
@SPI(name = "csv", order = 0)
public class CsvStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvStructHandler.class);
    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_CSV);

    public static final String COMMA_SEPARATOR = ",";

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            //  column name. read the first line.
            String[] columns = Stream.of(reader.readLine().split(COMMA_SEPARATOR)).map(String::trim).toArray(String[]::new);
            //  start load data
            int startLine = Math.max(descriptor.getStartOrder() - 1, 0);
            int endOrder = descriptor.getEndOrder() - 1;
            //  end order must be large than start order.
            int limit = endOrder < startLine ? Integer.MAX_VALUE : Math.max(endOrder - startLine, 0);
            reader.lines().skip(startLine)
                    .map(line -> {
                        String[] split = line.split(COMMA_SEPARATOR);
                        StructImpl impl = new StructImpl();
                        IntStream.range(0, split.length).forEach(i -> impl.add(columns[i], split[i]));
                        return impl;
                    })
                    .limit(limit)
                    .forEach(rowStruct -> worker.createInstance(rowStruct).ifPresent(cellHandler));
        } catch (Exception e) {
            LOGGER.error("load csv file failure. ");
            throw new StructTransformException(e.getMessage(), e);
        }
    }
}
