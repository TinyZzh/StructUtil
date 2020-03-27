package org.struct.core.bean;

import org.struct.core.handler.StructHandler;

import java.io.File;
import java.io.Serializable;

public interface WorkerMatcher extends Serializable {

    int HIGHEST = Integer.MIN_VALUE;

    int LOWEST = Integer.MAX_VALUE;

    /**
     * if active the auto model. match the file extension and chouce the matched worker to process data file.
     * {@link StructHandler}'s order. sorted from small to large.
     *
     * @return the worker's order.
     */
    int order();

    /**
     * @param file the file.
     * @return return true if auto match the file extensions. otherwise false.
     */
    boolean matchFile(File file);
}
