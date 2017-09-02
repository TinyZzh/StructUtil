# ExcelUtil 1.0.0.alpha
--------------------

Simple excel util based POI-3.10-final.
Author @TinyZ
Version 1.0.0.alpha

## Why use this tool ?
    In develop game. game component need some complex config data.

## matters
1.

## Features

* 1.Support load excel file (*.xlsx or *.xls), covert excel to json content and write to file named *.data.
* 2.Support excel cell object convert to java Object. Example:
    * NUMERIC covert to Integer, Double
    * BOOLEAN covert to Boolean
    * STRING content is
        1 - "true|false" No mater string upper or lower, Will be covert to Boolean,
        2 - "|i" covert to Integer array. Example "|i1988|1989" => [1988, 1989]
        3 - "|d" covert to Double array. Example "|d89.90|19.89" => [89.90, 19.89]
        4 - "|s" covert to String array. Example "|sstr1|str2" => ["str1", "str2"]
        5 - "|b" covert to boolean array. Example "|btrue|false" => [true, false]
        6 - covert to normal string. Example "|true|false" => "|true|false"
    * Other type : throw unknown error.

## Dependencies

You require the following to build it:
* [Oracle JDK 7 or above](http://www.oracle.com/technetwork/java/)
* POI-3.12
* Fastjson 1.1.45 above
* slf4j-1.7.2 [non-necessity]



