# Excel Util

[![Build Status](https://travis-ci.org/TinyZzh/ExcelUtil.svg?branch=master)](https://travis-ci.org/TinyZzh/ExcelUtil)
[![codecov](https://codecov.io/gh/TinyZzh/ExcelUtil/branch/master/graph/badge.svg)](https://codecov.io/gh/TinyZzh/ExcelUtil)
[![license](https://img.shields.io/github/license/TinyZzh/ExcelUtil.svg)](https://github.com/TinyZzh/ExcelUtil)
[![release](https://img.shields.io/github/release/TinyZzh/ExcelUtil.svg)](https://github.com/TinyZzh/ExcelUtil/releases/latest)
[![wiki](https://img.shields.io/badge/Docs-Wiki-green.svg)](https://github.com/TinyZzh/ExcelUtil/wiki)

Excel Convert To Java Bean Utility. 

## Requirements

    1. Java 8 above
    2. POI 3.x
```groovy
dependencies {
    compile 'org.apache.poi:poi:3.16'
    compile 'org.apache.poi:poi-ooxml:3.16'
}
```         

## Features
    
    1. Light. Only two annotation. implement load excel sheet direct convert to Java generic collection.
    2. Support Java 8's lambda functional. 
    3. Support Custom type converter. user can register custom converter to overwrite system default.
    4. Automatically check direct association of Excel sheets. by @ExcelSheet#required

## Technical Implement Detail

Just include two Java annotation: @ExcelSheet and @ExcelField

### 1. @ExcelSheet Annotation
This's annotation use to define the Java mapping bean with your excel sheet.

    1. fileName. define the excel file's name will be loaded.
    2. sheetName. define the excel sheet name will be loaded. 
    3. startOrder. define the excel sheet first data row start to load. default: 1. (start the second row) 
    3. endOrder. define the excel sheet last data row start to load.

### 1. @ExcelField Annotation
This's annotation use to define the Java mapping bean's Field. 
Use it you can implement custom convert logic to resolve special logic.

    1. name. define the excel column name. 
    2. ref. this's field is reference object.
    3. refGroupBy. reference object will be group by ArrayKey[field's value array].
    4. refUniqueKey. excel data convert to map. the map's key is ArrayKey[field's value array].
    5. required. if true check this field must exist in excel and the value not NULL.
    6. converter. definne custom converter. convert special struct by self's converter。


## Examples

### Step 1. Import Excel-Util Jar into project (build.gradle)

```groovy

dependencies {
    implementation 'org.excelutil:excel-util:2.0.0'
}
//  add the maven repository
repositories {
    maven {
        url "https://raw.github.com/TinyZzh/maven_repository/master/release/"
    }
}
```

### Step 2. Define A Java Bean With Annotation @ExcelSheet. 
Create class named Animal. every animal has biological classification and some other private attribute.
Defined the biological classification info in another sheet to reusable use this elements.
```java
@ExcelSheet(fileName = "Bean.xlsx", sheetName = "Sheet1")
public static class Animal {

    private int id;

    /**
     * if the field's name is equals column's name, the @ExcelField is not necessary.
     */
    @ExcelField(name = "name")
    private String name;

    @ExcelField()
    private Double weight;

    /**
     * this field required's class is {@link Classification}.
     * So the {@link Classification}'s excel data will be convert to a temp Map collection.
     * this field's value will be injected from map.
     * the key is {@link ArrayKey} include total refUniqueKey's value .
     */
    @ExcelField(ref = Classification.class, refUniqueKey = {"id"})
    private Classification bean;
}

@ExcelSheet(fileName = "Animal.xlsx", sheetName = "t_animal_classification")
public static class Classification {
    private int id;
    private String domain;
    private String phylum;
    private String clazz;
    private String order;
    private String family;
    private String genus;
    private String species;
}
```
### Step 3. Load Excel Data With Custom Struct

```java
ExcelWorker<Animal> worker = new ExcelWorker<>(rootPath, Animal.class);
List<Animal> list = worker.load(ArrayList::new);
```