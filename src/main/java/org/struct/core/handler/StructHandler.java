package org.struct.core.handler;

import org.struct.core.StructWorker;
import org.struct.core.bean.WorkerMatcher;

import java.io.File;
import java.util.function.Consumer;

public interface StructHandler {

    WorkerMatcher matcher();

    <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> structHandler, File file);
}
