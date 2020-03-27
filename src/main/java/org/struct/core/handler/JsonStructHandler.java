package org.struct.core.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.struct.core.StructWorker;
import org.struct.core.bean.FileExtensionMatcher;
import org.struct.core.bean.WorkerMatcher;
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

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_JSON);

    private final Gson gson = new Gson();

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> structHandler, File file) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                try {
                    T objInstance = (T) gson.fromJson(reader, clzOfStruct);
                    worker.afterObjectSetCompleted(objInstance);
                    structHandler.accept(objInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
