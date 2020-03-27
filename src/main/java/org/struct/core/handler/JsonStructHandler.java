package org.struct.core.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructWorker;
import org.struct.core.bean.FileExtensionMatcher;
import org.struct.core.bean.WorkerMatcher;
import org.struct.exception.ExcelTransformException;
import org.struct.spi.SPI;

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
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> structHandler, File file) {
        int i = 0;
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                i++;
                T objInstance = (T) gson.fromJson(reader, clzOfStruct);
                worker.afterObjectSetCompleted(objInstance);
                structHandler.accept(objInstance);
            }
            reader.endArray();
        } catch (Exception e) {
            LOGGER.warn("json deserialize failure. struct:{}, file:{}, line:{}", clzOfStruct, file.getName(), i, e);
            throw new ExcelTransformException(e.getMessage(), e);
        }
    }
}