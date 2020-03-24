package org.struct.core.bean;

import org.struct.core.StructWorker;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public abstract class WorkerMatcher implements Serializable {

    private static final long serialVersionUID = -1915810492215154100L;

    public static final int HIGHEST = Integer.MIN_VALUE;

    public static final int LOWEST = Integer.MAX_VALUE;

    private final Class<?> clzOfWorker;

    public WorkerMatcher(Class<?> clzOfWorker) {
        this.clzOfWorker = clzOfWorker;
    }

    /**
     * @return the class of {@link StructWorker} implement.
     */
    public Class<?> worker() {
        return clzOfWorker;
    }

    /**
     * if active the auto model. match the file extension and chouce the matched worker to process data file.
     * {@link StructWorker}'s order. sorted from small to large.
     *
     * @return the worker's order.
     */
    public int order() {
        return LOWEST;
    }

    /**
     * @param file the file.
     * @return return true if auto match the file extensions. otherwise false.
     */
    public abstract boolean matchFile(File file);

    @Override
    public String toString() {
        return "WorkerMatcher{" +
                "clzOfWorker=" + clzOfWorker +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerMatcher that = (WorkerMatcher) o;
        return Objects.equals(clzOfWorker, that.clzOfWorker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clzOfWorker);
    }
}
