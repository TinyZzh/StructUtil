# Struct Util

[![Build](https://github.com/TinyZzh/StructUtil/workflows/Build/badge.svg)](https://github.com/TinyZzh/StructUtil)
[![codecov](https://codecov.io/gh/TinyZzh/StructUtil/branch/master/graph/badge.svg)](https://codecov.io/gh/TinyZzh/StructUtil)
[![license](https://img.shields.io/github/license/TinyZzh/StructUtil.svg)](https://github.com/TinyZzh/StructUtil)
[![release](https://img.shields.io/github/release/TinyZzh/StructUtil.svg)](https://github.com/TinyZzh/StructUtil/releases/latest)
[![wiki](https://img.shields.io/badge/Docs-Wiki-green.svg)](https://github.com/TinyZzh/StructUtil/wiki)

结构化数据处理工具。

通过定义Struct Data Class和辅助的简单注解, 实现常见的数据文件(e.g. *.csv, *.xlsx. etc.)映射转换为定义的Java实例的功能。
基本上避免了配置表解析，热重载等相关的开发工作量。

> `master`分支基于JDK 17 LTS开发.

> JDK 1.8的请使用[3.5.3.beta-SNAPSHOT](https://github.com/TinyZzh/StructUtil/releases/tag/3.5.3.beta-SNAPSHOT)

notice

```groovy
dependencies {
    //  Excel. *.xls or *.xlsx
    compile 'org.apache.poi:poi:5.2.2'
    compile 'org.apache.poi:poi-ooxml:5.2.2'
    //  json
    compile 'com.google.code.gson:gson:2.9.0'

    compile 'org.slf4j:slf4j-api:1.7.32'
}
```         

## 特性

1. 低侵入。对已有的代码仅需使用 **StructField** 和 **StructSheet** 两个注解
2. 高性能、低内存占用。使用增量或Stream API的方式避免大文件读取带来的低性能、高内存占用的问题
3. 扩展性。提供WorkerMatcher、StructHandler、Converter等扩展点供用户实现自定义扩展功能
4. 结构化数据自动校验和检查。自动检查校验结构化数据及依赖之间的关系。避免出现循环依赖等问题
5. 丰富的内置解析器。内置提供 **.xls**、**.xlsx**、**.json**、**.xml** 四种常见结构化数据的解析器（扩展中）
6. 丰富的JDK原生类的类型转换支持
7. 自定义类型转换器{Converter}
8. 灵活的结构化数据整理机制。输出的结果支持Array、List、Map、Vector、Set等JDK内置或自定义的集合容器
9. 灵活的文件变更监听和结构化数据文件动态加载能力
10. 支持 **Record** 类型(JDK 16+).

## 快速入门

主要包含两个Java注解. @StructSheet和@StructField

```java
//  Java Record
@StructSheet(fileName = "struct_examples.xlsx", sheetName = "Sheet 1")
record SimplaJO(
        @StructField(name = "id") int id,
        @StructField(ref = RefJO.class, refUniqueKey = "id") RefJO ref
) {
}

//  Java Class
@StructSheet(fileName = "struct_examples.xlsx", sheetName = "Sheet 1")
public final class SimplaJO {
    @StructField(name = "id") 
    private int id;
    @StructField(ref = RefJO.class, refUniqueKey = "id")
    private RefJO ref;
}
```

### 1. @StructSheet Annotation

定义数据文件的结构.

|注解名称|缺省值|可选字段|备注|
|:--|:--:|:--:|---:|
|fileName|''|**N**|数据文件名称, 带文件的后缀名. e.g. struct.xlsx|
|sheetName|'Sheet1'|Y|表单名称. 针对Excel文件的包含多个Sheet|
|startOrder|1|Y|控制文件读取的开始. 缺省为: 1 从excel的1行(第一行为0)或文件的第一行开始|
|endOrder|-1|Y|控制文件读取的结束. 缺省为:-1
|matcher|WorkerMatcher.class|Y|自定义WorkHandler, 可以根据条件指定处理的StructHandler. 
|filter|StructBeanFilter.class|Y|过滤、筛选符合条件的JO. 

### 2. @StructField Annotation

定义列结构. 

|注解名称|缺省值|可选字段|备注|
|:--|:--:|:--:|---:|
|name|''|Y|数据文件中的列名, 当设置非空字符串时，使用注解的值替代类文件中的字段名来解析数据文件|
|ref|Object.class|Y|引用其他结构|
|refGroupBy|{}|Y|当ref值有效时, 引用的结构数据根据字段进行分组|
|refUniqueKey|{}|Y|当ref值有效时, 引用的结构数据根据字段转换为Map
|aggregateBy|''|Y|根据父结构中的字段值，对子结构进行聚合. 类似于groupBy的功能. 
|aggregateType|Object.class|Y|当aggregateBy生效时，聚合的集合类型. 不支持Map
|required|false|Y|字段值非空检查. 设置为True时, 字段值必须为非null的值.|
|converter|无|Y|将数据文件中的数据转换为期望的JO|

## Examples

### Step 1. Define A Java Bean With Annotation @StructSheet.

Create class named Animal. every animal has biological classification and some other private attribute.
Defined the biological classification info in another sheet to reusable use this elements.

```java

@StructSheet(fileName = "Animal.xlsx", sheetName = "t_animal_info")
public final class Animal {

    private int id;

    private String name;

    private Double weight;

    @StructField(ref = Classification.class, refUniqueKey = {"id"})
    private Classification bean;
}

@StructSheet(fileName = "Animal.xlsx", sheetName = "t_animal_classification")
public final class Classification {
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
StructWorker<Animal> worker=WorkerUtil.newWorker(rootpath,Animal.class);
        List<Animal> list=worker.toList(ArrayList::new);
```

### [Feature] Custom Type Converter

实现接口`Converter`来实现自定义的类型转换器. 参考`org.struct.core.converter.ArrayConverter`

示例:

```java
public class StringToArrayConverter implements Converter {

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        if (!targetType.isArray() || String.class != originValue.getClass()) {
            return null;
        }
        String content = (String) originValue;
        Class<?> componentType = targetType.getComponentType();
        String[] data = content.split(separator);
        if (exceptBlank) {
            data = Arrays.stream(data)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
        Object array = Array.newInstance(componentType, data.length);
        for (int i = 0; i < data.length; i++) {
            Array.set(array, i, ConverterUtil.covert(data[i], componentType));
        }
        return array;
    }
}
```

### [Feature] Custom Struct Handler

使用OSGI技术实现动态加载服务.
实现接口`StructHandler`实现自定义`StructHandler`. 实现`WorkerMatcher`接口来自定义Worker的匹配器.

内嵌的Matcher实现为`FileExtensionMatcher`, 根据文件的扩展名来识别数据文件的类型和匹配有效的`StructHandelr`.
`FileExtensionMatcher`是简单的文件的特征匹配实现， 主要利用文件扩展名和文件大小来匹配.

在META-INF中新增`org.struct.core.handler.StructHandler`文件. 文件内容为用户定义的`StructHandler`的类名.
已内嵌的`ExcelUMStructHandler`为例：

```text
org.struct.core.handler.ExcelUMStructHandler
```

定义`WorkerMatcher`：

```java
public class ExcelUMStructHandler implements StructHandler {

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(524288L, WorkerMatcher.HIGHEST,
            FileExtensionMatcher.FILE_XLSX, FileExtensionMatcher.FILE_XLS);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        //  do something
    }
}
```

### [Feature] Custom Data Filter

继承抽象类`StructBeanFilter`实现自定义的Filter.
通过自定义Filter和自定义约定的配置表数据标签来筛选和过滤有效的配置表数据.
配合模板表热更新可以实现包含但不限于`热屏蔽开发中功能的测试数据`、`热下线玩家刷BUG或异常的线上配置数据`等功能

示例:

```java

// 用例: @StructSheet(fileName = "struct.xml", filter = MyFilter.class)
public class MyFilter extends StructBeanFilter<StructJO> {
    @Override
    public boolean test(StructJO obj) {
        return obj.lv > 2;
    }
}
```

### [Support] Hot Update Configuration

工具类`FileWatcherService`实现了简单的文件变更监控Hook. 通过监听文件变更事件，当文件发生变更时，触发Hook来实现热加载.

```java
FileWatcherService service = FileWatcherService.newBuilder().setWatchService(mockMs)
        .setScheduleInitialDelay(10L)
        .setScheduleTimeUnit(TimeUnit.DAYS)
        .setScheduleDelay(999L)
        .setExecutor(Executors.newScheduledThreadPool(1,r->new Thread(r,"test")))
        .build();
service.bootstrap();
service.register("./examples/")
        .registerHook("./examples/tpl_vip.xml",runnable)
        .registerHook("./examples/tpl_vip2.xml",runnable)
```