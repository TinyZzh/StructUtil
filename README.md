# Excel Util

Excel转Java Bean工具. 方便策划童鞋配置模板表. 同时降低开发同事的代码开发.

## 环境要求

    1. Java 8 above
    2. POI 3.x
```groovy
dependencies {
    compile 'org.apache.poi:poi:3.16'
    compile 'org.apache.poi:poi-ooxml:3.16'
}
```         

## 背景描述
描述几种业务场景.
供职过的几家公司采用excel的模板表数据转换成json, xml, csv或二进制等中间格式, 再交给程序使用. 主要的问题在以下几点: 

    1. 策划每次需要手动执行转换工具. 转换完成在提交文件. 
    2. 程序需要开发针对性的Excel转表工具. 增加了工作量. 
    3. Excel没有很优雅的方式表示“数组Array”、“列表List”、“键值对Map”等程序常用的数据结构.
    4. Excel表结构检查逻辑非常复杂. 表关联关系检查比较困难. 很容易造成数据漏填导致的问题.

## Excel Util解决的问题
    1. 策划、程序分离. 策划专注于Excel表单的填写. 程序不再关注模板表的解析
    2. 使用Annotation的方式实现Excel解析. 在不侵入业务代码的同时, 实现Excel的解析和加载
    3. 使用ExcelField的ref作为外键索引. 支持Excel跨表解析. 
    4. 模板表数据加载时. 通过外键索引可以检验模板表数据是否异常, 索引关系是否正确、数据是否缺失.
    5. 扩展支持更多的数据结构(当前实现的Array, List, Map, Vector，Set等JDK集合容器)
    6. 自定义扩展点Converter. 支持用户将Excel格式转换为自定义的任意结构

## Technical Implement Detail

主要包含两个Java Annotation. @ExcelSheet和@ExcelField

### 1. @ExcelSheet Annotation
定义Excel模板表对应个Java Bean

    1. fileName. 指定Bean对应的Excel文件文件名.
    2. sheetName. 指定Bean对应的Sheet名称. Excel有多个Sheet, 所以需要指定Sheet Name. 缺省值为Sheet1

### 1. @ExcelField Annotation
定义Excel模板表对应个Java Bean中的 Field

    1. name. 明确的指定bean中的字段对应的Excel的列名
    2. ref. 字段映射的ExcelSheet. 类似于索引. 明确关联另外一个Excel表结构.
    3. refGroupBy. 映射的目标根据此字段的值进行分组
    4. refUniqueKey. Map对应根据uniqueKey字段的值作为键值对的key.
    5. required. 字段是否为必须字段? TRUE时, 字段值必须存在, 字段为映射时, 映射必须存在.
    6. converter. 自定义转换器转换Excel的值


## Examples

### Step 1. Define A Java Bean With Annotation @ExcelSheet. 
Create class named Animal. every animal has biological classification and some other private attribute.
Defined the biological classification info in another sheet to reusable use this elements.
```java
@ExcelSheet(fileName = "Animal.xlsx", sheetName = "t_animal_info")
public static class Animal {

    private int id;

    private String name;

    private Double weight;

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
### Step 2. Load Excel Data With Custom Struct

```java
ExcelWorker<Animal> worker = new ExcelWorker<>(rootPath, Animal.class);
List<Animal> list = worker.load(ArrayList::new);
```