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

package org.struct.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.exception.StructTransformException;
import org.struct.util.BomInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * basic separator file struct handle.
 *
 * @author TinyZ.
 * @date 2020-08-19.
 * @see CsvStructHandler
 */
public abstract class MultipleRowsStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleRowsStructHandler.class);

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        try (FileInputStream fis = new FileInputStream(file);
             BomInputStream bis = new BomInputStream(fis);
             BufferedReader reader = new BufferedReader(new InputStreamReader(bis, bis.getBomCharset()))) {
            //  column name. read the first line.
            String[] columns = this.resolveColumnNameArray(clzOfStruct, reader.readLine());
            //  start load data
            int startOrder = descriptor.getStartOrder();
            int endOrder = descriptor.getEndOrder();
            int skip = Math.max(startOrder - 1, 0);
            //  end order must be larger than start order.
            int limit = endOrder < startOrder ? Integer.MAX_VALUE : (endOrder - skip);
            reader.lines().skip(skip)
                    .map(line -> this.processRow(clzOfStruct, columns, line))
                    .limit(limit)
                    .forEach(rowStruct -> worker.createInstance(rowStruct).ifPresent(cellHandler));
        } catch (Exception e) {
            LOGGER.info("load multiple rows file failure. struct:{}", getClass().getName(), e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    /**
     * Process first line to resolve column name array.
     *
     * @param clzOfStruct reference struct class.
     * @param line        the file's first line data.
     * @param <T>         reference struct type.
     * @return column name array.
     */
    protected abstract <T> String[] resolveColumnNameArray(Class<T> clzOfStruct, String line);

    /**
     * Process row data to struct instance.
     *
     * @param clzOfStruct reference struct class.
     * @param columns     column name array.
     * @param line        the file's line data.
     * @param <T>         reference struct type.
     * @return the real struct instance.
     * @throws RuntimeException throw exception if struct convert failure.
     */
    protected abstract <T> Object processRow(Class<T> clzOfStruct, String[] columns, String line) throws RuntimeException;
}
