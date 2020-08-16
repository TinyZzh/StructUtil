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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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

    /**
     * The Google json deserializer.
     */
    private final Gson gson;

    public JsonStructHandler() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(StructImpl.class, new StructJsonDeserializer());
        this.gson = builder.create();
    }

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        int i = 0;
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                int line = ++i;
                if (descriptor.getStartOrder() > 0 && line < descriptor.getStartOrder()) {
                    reader.skipValue();
                } else if (descriptor.getEndOrder() > 0 && line > descriptor.getEndOrder()) {
                    //  end
                    return;
                } else {
                    Object rowStruct = gson.fromJson(reader, clzOfStruct);
                    worker.createInstance(rowStruct).ifPresent(cellHandler);
                }
            }
            reader.endArray();
        } catch (Exception e) {
            LOGGER.warn("json deserialize failure. struct:{}, file:{}, line:{}", clzOfStruct, file.getName(), i, e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    public static class StructJsonDeserializer implements JsonDeserializer<StructImpl> {

        @Override
        public StructImpl deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            StructImpl struct = new StructImpl();
            jsonElement.getAsJsonObject().entrySet().forEach(e -> {
                struct.add(e.getKey(), e.getValue().getAsString());
            });
            return struct;
        }
    }
}
