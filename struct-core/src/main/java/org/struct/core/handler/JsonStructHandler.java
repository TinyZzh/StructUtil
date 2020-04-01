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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.ExcelTransformException;
import org.struct.spi.SPI;
import org.struct.util.AnnotationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Use Gson stream API to load data file.
 *
 * @see "https://sites.google.com/site/gson/streaming"
 */
@SPI(name = "json")
public class JsonStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonStructHandler.class);
    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_JSON);

    private final Gson gson = new Gson();

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructSheet annotation = AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct);
        int i = 0;
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                int line = ++i;
                if (annotation.startOrder() > 0 && line < annotation.startOrder()) {
                    reader.skipValue();
                } else if (annotation.endOrder() > 0 && line > annotation.endOrder()) {
                    //  end
                    return;
                } else {
                    T objInstance = (T) gson.fromJson(reader, clzOfStruct);
                    worker.afterObjectSetCompleted(objInstance);
                    cellHandler.accept(objInstance);
                }
            }
            reader.endArray();
        } catch (Exception e) {
            LOGGER.warn("json deserialize failure. struct:{}, file:{}, line:{}", clzOfStruct, file.getName(), i, e);
            throw new ExcelTransformException(e.getMessage(), e);
        }
    }
}
