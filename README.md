# ExcelUtil 1.x
Simple excel util based POI-3.10-final.
=========
@Author TinyZ
@Version 1.0

## Features
1.Support load *.xlsx, *.xls file, save content as JSON named *.data.
2.Support excel cell object convert to java Object. Example:
    * NUMERIC covert to Integer, Double
    * BOOLEAN covert to Boolean
    * STRING content is "true|false" covert to Boolean, "|i" covert to Integer array, "|d" covert to Double array, "|s" covert to String array else covert to String
    * Other type : throw unknown error

## Dependencies
You require the following to build it:
    * [Oracle JDK 6 or above](http://www.oracle.com/technetwork/java/)
    * POI-3.10-FINAL
    * slf4j-1.7.2 [non-necessity]



