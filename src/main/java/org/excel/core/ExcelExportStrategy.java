package org.excel.core;

public enum ExcelExportStrategy {
    /**
     * Use POI user model. high memory. large excel file will throw OOM exception
     */
    USER_MODEL,
    /**
     * Use POI event model. low memory. but unsupported some features.
     */
    EVENT_MODEL,
    /**
     * auto by file's length.
     */
    AUTO,
}
