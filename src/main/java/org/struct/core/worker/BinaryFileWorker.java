package org.struct.core.worker;

import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple binary file worker implement.
 *
 * @param <T>
 */
public class BinaryFileWorker<T> extends StructWorker<T> {

    public BinaryFileWorker(String workspace, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        super(workspace, clzOfBean, refFieldValueMap);
    }

    @Override
    protected void onLoadStructSheetImpl(Consumer<T> cellHandler, StructSheet annotation, File file) {

    }
}
