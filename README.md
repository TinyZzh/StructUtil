# Excel Util
--------------------

Author: TinyZ

Version: 1.0.1.alpha

## 介绍
    将Excel数据转换为对应的JSON格式. 在程序中读取JSON格式数据比Excel简单方便.

## Why use this tool ?
    covert excel data to JSON data struct.

## Features

* 1.Support load excel file (*.xlsx or *.xls), covert excel to json content and write to file named *.data.
* 2.Support excel cell object convert to java Object. Example:
    * NUMERIC covert to Integer, Double, Long
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
* POI-3.16
* Fastjson 1.2.6
* slf4j-1.7.2 [non-necessity]



