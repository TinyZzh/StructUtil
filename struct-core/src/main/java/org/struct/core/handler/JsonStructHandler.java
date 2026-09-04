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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                    //  NOTE: deserialize into StructImpl (the intermediate row representation)
                    //  instead of clzOfStruct. Deserializing straight into the target bean
                    //  bypassed every Converter registered in ConverterRegistry
                    //  (LocalDate, Enum, array separator, user converters...) and forced a
                    //  second reflective conversion inside createInstance.
                    StructImpl rowStruct = gson.fromJson(reader, StructImpl.class);
                    if (rowStruct != null) {
                        worker.createInstance(rowStruct).ifPresent(cellHandler);
                    }
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
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                return null;
            }
            StructImpl struct = new StructImpl();
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                Object value = this.resolveValue(entry.getValue());
                //  StructImpl#add already ignores null/empty values.
                struct.add(entry.getKey(), value);
            }
            return struct;
        }

        /**
         * Resolve a json element to a plain java value.
         * <p>
         * NOTE: the previous implementation called {@code getAsString()} on every element,
         * which throws {@link UnsupportedOperationException} for numbers, booleans,
         * arrays and nested objects - so any non-string field blew up.
         */
        private Object resolveValue(JsonElement element) {
            //  NOTE: `element` is never null - it is always a member of a JsonObject's
            //  entry set - so only JsonNull has to be handled here.
            if (element.isJsonNull()) {
                return null;
            }
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return primitive.getAsString();
                }
                if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                }
                return primitive.getAsNumber();
            }
            if (element.isJsonArray()) {
                //  keep the structure so that array/collection fields are filled
                //  element-wise (see ArrayConverter), instead of being split by a separator.
                JsonArray array = element.getAsJsonArray();
                List<Object> list = new ArrayList<>(array.size());
                for (JsonElement item : array) {
                    list.add(this.resolveValue(item));
                }
                return list;
            }
            //  nested object: keep the raw element, a user defined converter may resolve it.
            return element;
        }
    }
}
