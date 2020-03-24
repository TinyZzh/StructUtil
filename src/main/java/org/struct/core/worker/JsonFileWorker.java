package org.struct.core.worker;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Use Gson stream API to load data file.
 *
 * @param <T>
 * @see "https://sites.google.com/site/gson/streaming"
 */
public class JsonFileWorker<T> extends StructWorker<T> {

    private final Gson gson = new Gson();

    public JsonFileWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        super(rootPath, clzOfBean, refFieldValueMap);
    }

    @Override
    protected void onLoadStructSheetImpl(Consumer<T> cellHandler, StructSheet annotation, File file) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                try {
                    T objInstance = (T) gson.fromJson(reader, this.clzOfBean);
                    this.afterObjectSetCompleted(objInstance);
                    cellHandler.accept(objInstance);
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
