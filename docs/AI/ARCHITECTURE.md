# StructUtil 架构深入解析（AI 参考）

> 面向需要修改/扩展本框架的 AI。阅读前请先读根目录 [`AGENTS.md`](../../AGENTS.md)。
> 所有路径相对于仓库根目录。

---

## 1. 设计目标与关键取舍

| 目标 | 实现手段 | 代价 / 限制 |
| --- | --- | --- |
| 数据源无关 | `StructHandler` SPI + `WorkerMatcher` 匹配与降级 | 每种格式需单独实现 Handler |
| 内存友好 | `.xlsx` 默认走 SAX 事件模型（`XlsxSaxStructHandler`）；大文件自动禁用 usermode | SAX 模式不支持公式求值 |
| 表达力强 | `@StructField(ref/ refUniqueKey / refGroupBy / aggregateBy)`，支持嵌套引用、分组、聚合 | 引用关系在解析期递归展开，配置错误会抛运行期异常 |
| 零 Spring 依赖 | 核心引擎 `struct-core` 只依赖 slf4j + POI + Gson + protobuf | Spring 侧另建模块 |
| 可扩展 | 自研 SPI（`EnhancedServiceLoader`）支持 `name` + `order` | 需同时维护 `META-INF/struct/*` 与 `module-info.java` |
| 省内存 | `StructInternal.INTERN_FIELD_NAME`（字段名 `intern`）、`@StructField(cached)`（值 `intern`） | 滥用 `intern` 会撑大字符串常量池 |

---

## 2. 包职责详解

### 2.1 `struct-core`

```
org.struct.annotation          用户 API（RUNTIME 保留，@Inherited，支持 FIELD / RECORD_COMPONENT）
  ├─ @StructSheet              类级：fileName / sheetName / startOrder / endOrder / matcher / filter
  ├─ @StructField              字段级：name / ref / refGroupBy / refUniqueKey / aggregateBy /
  │                                     aggregateType / required / cached / converter
  └─ @StructOptional           字段级：StructField[] value()，按顺序尝试，取第一个解析成功的

org.struct.core                引擎主体
  ├─ StructWorker              一次加载任务的驱动者（入口 API）
  ├─ StructDescriptor          类级元数据（由 @StructSheet 生成）
  ├─ FieldDescriptor           字段元数据抽象（abstract，Serializable + Comparable）
  │   ├─ SingleFieldDescriptor 单个字段
  │   └─ OptionalDescriptor    多候选字段
  ├─ StructImpl                一行数据的临时中间表示
  ├─ StructConfig              全局配置（enum 单例，可运行时改）
  ├─ StructInternal            内部静态开关（由系统属性初始化）
  ├─ ArrayKey                  多字段组合 key（refUniqueKey/refGroupBy 多列时）
  ├─ TypeRefFactory<T>         函数式接口，用于保留泛型（如 HashMap::new）
  │
  ├─ core/handler/             数据源解析层（SPI）
  ├─ core/converter/           类型转换层（SPI）
  ├─ core/factory/             Bean 结构解析层（SPI）
  ├─ core/matcher/             文件匹配 + 优先级
  └─ core/filter/              Bean 级过滤

org.struct.spi                 自研 SPI：EnhancedServiceLoader / ServiceLoader / SPI / ExtensionDefinition
org.struct.util                Reflects WorkerUtil ConverterUtil AnnotationUtils Strings BomInputStream ExcelUtil
org.struct.support             FileWatcherService（WatchService 封装，热重载基础）
org.struct.exception           6 个异常（见 §11）
```

### 2.2 `struct-spring` / `struct-spring-boot-starter`

见 §9。

---

## 3. 加载全流程（逐帧）

### 3.1 入口

```java
StructWorker<MyBean> worker = new StructWorker<>("classpath:/org/struct/core/", MyBean.class);
List<MyBean> list = worker.toList(ArrayList::new);
```

`StructWorker` 构造时立即创建 `StructDescriptor(clzOfStruct)`：从类上查找 `@StructSheet`，**缺失即抛异常**。

```30:49:struct-core/src/main/java/org/struct/core/StructDescriptor.java
    public StructDescriptor(Class<?> clzOfStruct) {
        this(Objects.requireNonNull(AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct),
                "clazz:" + clzOfStruct.getName() + " must be annotated by @StructSheet"));
    }
```

### 3.2 阶段一：解析 Bean 结构（惰性，首次 `checkStructFactory()` 时执行）

```
StructWorker#checkStructFactory
 └─ WorkerUtil#structFactory(clzOfStruct, worker)        // 遍历 SPI StructFactoryBean，第一个成功者胜出
     └─ DefaultStructFactoryBean#newInstance  → new JdkStructFactory(clz, worker)
 └─ JdkStructFactory#parseStruct()
     ├─ record：遍历 getRecordComponents()，顺序 = 声明顺序
     └─ POJO ：Reflects#resolveAllFields(clz, true)（含父类），跳过 static，setAccessible(true)
     ├─ 每个字段 → createFieldDescriptor()
     │     ├─ 有 @StructOptional → OptionalDescriptor（内部对每个 StructField 建 SingleFieldDescriptor）
     │     └─ 否则               → SingleFieldDescriptor(fieldOrRc, @StructField)
     │           └─ worker.handleReferenceFieldValue(this, descriptor)   // ★ 递归预加载引用表
     └─ beanFieldsList = map.values().stream().sorted().toList()          // ★ FieldDescriptor#compareTo
```

要点：
- **`parseStruct()` 会递归加载所有被引用表**，并把结果放进 `StructWorker#tempRefFieldValueMap`（`key = ref类全名 + ":" + 字段名`）。
- `beanFieldsList` 的顺序对 `record` 是**构造参数顺序**，由 `FieldDescriptor#compareTo` 决定。

```66:92:struct-core/src/main/java/org/struct/core/FieldDescriptor.java
    @Override
    public int compareTo(FieldDescriptor o) {
        //  优先SingleFieldDescriptor > OptionalFieldDescriptor
        if (this instanceof OptionalDescriptor
                && o instanceof OptionalDescriptor) {
            return this.getName().compareTo(o.getName());
        } else if (this instanceof OptionalDescriptor) {
            return 1;
        } else if (o instanceof OptionalDescriptor) {
            return -1;
        } else if (this instanceof SingleFieldDescriptor
                && o instanceof SingleFieldDescriptor) {
            //  优先级  field > ref field > custom converter field
            ...
```

### 3.3 阶段二：读文件、逐行/逐条转换

```236:254:struct-core/src/main/java/org/struct/core/StructWorker.java
    public void handleDataFile(Consumer<T> cellHandler) {
        String filePath = WorkerUtil.resolveFilePath(this.workspace, this.descriptor.getFileName());
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists. path: " + filePath);
        }
        List<StructHandler> collected = WorkerUtil.lookupStructHandler(this.descriptor, file);
        for (StructHandler handler : collected) {
            try {
                handler.handle(this, this.clzOfStruct, wrapCellHandler(this.descriptor, cellHandler), file);
                return;
            } catch (Exception e) {
```

- 路径解析：`classpath:` / `file:` / 裸路径三种前缀；`classpath:` 找不到时**原样返回拼接路径**（随后 `new File()` 不存在即报错）。
- **Handler 降级链**：匹配到的 Handler 按 `matcher().order()` 升序尝试，前一个抛异常就试下一个，全失败抛 `IllegalArgumentException("unknown data file extension...")`。
- `wrapCellHandler`：若 `@StructSheet(filter=...)` 指定了具体类，用 `getConstructor(Consumer.class)` 反射包一层 `StructBeanFilter`。

### 3.4 阶段三：行 → Bean

```
StructHandler 负责：读一行 → new StructImpl() → struct.add(列名, 单元格值)
        ↓
worker.createInstance(StructImpl)
        ↓
JdkStructFactory#newStructInstance(structImpl)
    ├─ record：forEachBeanFields 收集 args[]/argTypes[] → getDeclaredConstructor(argTypes).newInstance(args)
    └─ POJO ：Reflects#newInstance(clz) → 逐字段 sfd.setFieldValue(instance, v)
        ↓（每个字段）
JdkStructFactory#handleInstanceFieldValue(structImpl, sfd)
    ├─ value = sfd.getFieldValueFrom(structImpl)        // StructImpl → get(name)；POJO → Field.get / RecordComponent accessor
    ├─ required 校验（仅非引用字段；null 或空串视为非法）
    ├─ 分流：
    │   ├─ sfd.getConverter() != null        → converter.convert(ctx, value, fieldType)
    │   ├─ sfd.isReferenceField()
    │   │     ├─ isBasicTypeCollection()     → ConverterRegistry#convertCollection(...)
    │   │     └─ 否则                        → handleReferenceFieldValue(structImpl, sfd)
    │   └─ 否则                              → ConverterRegistry#convert(ctx, value, fieldType)
    └─ sfd.isCached() && value is String     → str.intern()
```

`OptionalDescriptor` 的处理在 `forEachBeanFields`：

```170:177:struct-core/src/main/java/org/struct/core/factory/JdkStructFactory.java
                if (fd instanceof OptionalDescriptor ofd) {
                    for (SingleFieldDescriptor sfd : ofd.getDescriptors()) {
                        Object value = this.handleInstanceFieldValue(structImpl, sfd);
                        if (value != null) {
                            consumer.accept(i, sfd, value);
                            break;
                        }
                    }
```

---

## 4. 数据源层（Handler / Matcher）

### 4.1 内置 Handler

| Handler | SPI name | order | Matcher | 说明 |
| --- | --- | --- | --- | --- |
| `ExcelUMStructHandler` | `excel-user` | 0 | `FileExtensionMatcher(1.5MB 阈值, HIGHEST, .xlsx, .xls)` | POI usermode；支持公式求值；**文件 ≥1.5MB 时不匹配** |
| `XlsxSaxStructHandler` | `xlsx` | —(默认0) | `FileExtensionMatcher(.xlsx)` | SAX 事件模型；不支持公式；用 `EndOfExcelSheetException` 提前终止 |
| `XlsEventStructHandler` | — | — | `.xls` | `.xls` 的事件模型实现 |
| `CsvStructHandler` | `csv` | 0 | `.csv` | 继承 `RowWithSeparatorStructHandler`，分隔符 `,` |
| `JsonStructHandler` | `json` | —(默认0) | `.json` | Gson streaming + 自定义 `StructImpl` 反序列化器 |
| `ProtobufStructHandler` | `protobuf` | —(默认0) | `.protobuf` / `.bin` / `.pbf` | 新增。以 `StructDescriptor#getSheetName()` 作为 message 名；按 **length-delimited** 流逐条读取；解析器优先取 `clz.parser()`，否则反射 `getDescriptor()`/`descriptor()` 构造 `DynamicMessage` 解析器 |
| `XmlStructHandler`（spring 模块） | — | — | `.xml` | 通过 `module-info` 的 `provides` 注册 |

Excel 列 → 字段的映射：**首行（表头）作为列名**。
- usermode：`resolveExcelColumnToField(headRow)`，表头行 = `max(0, startOrder-1)`。
- SAX：`isFirstRow` 标记的那一行的单元格值写入 `headRowMap`。

`startOrder` 语义（`@StructSheet` 默认 1）：
- Excel：0-based 行号，默认 1 → 从第 2 行开始（第 1 行是表头）。
- JSON / CSV：作为行/记录序号下限。

### 4.2 匹配与排序

```75:84:struct-core/src/main/java/org/struct/util/WorkerUtil.java
    public static List<StructHandler> lookupStructHandler(StructDescriptor descriptor, File file) {
        List<StructHandler> handlers = HANDLERS_HOLDER.get();
        Stream<StructHandler> stream = handlers.stream();
        if (WorkerMatcher.class != descriptor.getMatcher()) {
            stream = stream.filter(handler -> handler.matcher().getClass().isAssignableFrom(descriptor.getMatcher()));
        } else {
            stream = stream.filter(handler -> handler.matcher().matchFile(file));
        }
        return stream.sorted(Comparator.comparingInt(o -> o.matcher().order())).collect(Collectors.toList());
    }
```

- 用户在 `@StructSheet(matcher = XxxMatcher.class)` 指定时，按**类型可赋值**过滤（注意方向：`handler.matcher().getClass().isAssignableFrom(descriptor.getMatcher())`）。
- 未指定（默认 `WorkerMatcher.class`）时，走 `matchFile(file)`。
- `WorkerMatcher.HIGHEST = Integer.MIN_VALUE`、`LOWEST = Integer.MAX_VALUE`。

---

## 5. 类型转换层

### 5.1 注册表

`ConverterRegistry` 是**静态**注册表（`ConcurrentHashMap<Class<?>, Converter>`），静态块里通过 SPI 加载所有 `Converters` 实现并合并。

```111:129:struct-core/src/main/java/org/struct/core/converter/ConverterRegistry.java
    public static Object convert(ConvertContext ctx, Object originValue, Class<?> requiredType) {
        if (Object.class == requiredType
                || requiredType.isInstance(originValue)) {
            return originValue;
        }
        //  try lookup user's converter first.
        Converter converter = lookup(requiredType);
        if (null == converter) {
            if (requiredType.isEnum()) {
                converter = lookup(Enum.class);
            } else if (requiredType.isArray()) {
                converter = lookup(Array.class);
            }
        }
        if (null != converter) {
            return converter.convert(ctx, originValue, requiredType);
        }
        return originValue;
    }
```

**查找顺序**：精确类型 → 若是 enum 回落到 `Enum.class` → 若是数组回落到 `Array.class` → 都找不到就**原样返回**（不报错！这是常见的"值没转换"问题来源）。

### 5.2 内置转换器（`EmbeddedConverters`，`order = 0`）

| Key | Converter | 行为要点 |
| --- | --- | --- |
| `int/Integer`, `long/Long`, `short/Short`, `byte/Byte` | `Integer/Long/Short/Byte Converter` | 支持十六进制字符串（`0x..`，`ConverterUtil#isHexNumber`）；溢出抛异常；`null → 0` |
| `float/Float`, `double/Double` | `Float/Double Converter` | `null → 0.0` |
| `boolean/Boolean` | `BooleanConverter` | `ConverterUtil#isBooleanTrue`；数字按 `==1` |
| `BigInteger` / `BigDecimal` | `BigInteger/BigDecimal Converter` | `null → ZERO` |
| `Enum.class`（统一入口） | `EnumConverter` | 先试 ordinal(int)，再试 name（含大小写/大写回退、忽略大小写比较），最后 enum→enum 按 ordinal |
| `String` | `StringConverter` | `toString()` |
| `Array.class`（统一入口） | `ArrayConverter` | 按正则分隔符切分（默认 `\\|`），逐元素递归 `ConverterRegistry#convert` |
| `Date` | `DateConverter` | |
| `LocalDate` | `LocalDateConverter` | 先按 `Strings.DATE_FORMAT_PATTERN` 解析，失败再按时间戳（`< Integer.MAX_VALUE` 视为秒，否则毫秒） |
| `LocalDateTime` | `LocalDateTimeConverter` | 同上，模式 `Strings.DATE_TIME_FORMAT_PATTERN` |

### 5.3 集合

`ConverterRegistry#convertCollection(ctx, originValue, collectType, requiredType)`：
- 要求 `collectType` 是 `Collection` **且** `requiredType` 是基础类型（否则原样返回）。
- 根据接口类型选实现：`SortedSet→TreeSet`、`Set→HashSet`、其他→`ArrayList`；具体类则反射无参构造。
- 内部先转成数组（`Array.class` 的 converter），再逐元素 add。

典型用法：`@StructField(ref = Integer.class) private List<Integer> list;` → `SingleFieldDescriptor#isBasicTypeCollection()` 为 true。

### 5.4 自定义转换器接入方式

1. **字段级**：`@StructField(converter = MyConverter.class)`。
   `SingleFieldDescriptor` 构造时通过 `ConverterRegistry.lookupOrDefault(c, c)` 拿到实例（会顺带注册进全局表）。
2. **全局级**：`ConverterRegistry.register(targetType, converterOrClass)`。
3. **SPI 批量**：实现 `Converters`，注册到 `META-INF/struct/org.struct.core.converter.Converters`（+ `module-info` 的 `provides`）。
   注意 `EmbeddedConverters` 用 `putIfAbsent`，**不会覆盖**同类型的已注册项；而 `ConverterRegistry#register` 是 `put`，**会覆盖**。

`ConvertContext` 默认实现 `DefaultConvertContext(structImpl, fieldDescriptor)`，把当前行数据和字段元数据传给转换器。

---

## 6. 引用字段（ref）与聚合（aggregateBy）

### 6.1 预加载：递归子 Worker

```92:125:struct-core/src/main/java/org/struct/core/StructWorker.java
    public void handleReferenceFieldValue(StructFactory structFactory, SingleFieldDescriptor descriptor) throws RuntimeException {
        if (descriptor == null || !descriptor.isReferenceField() || descriptor.isBasicTypeCollection()) {
            return;
        }
        String clzFieldUrl = descriptor.getRefFieldUrl();
        if (tempRefFieldValueMap.containsKey(clzFieldUrl)) {
            LOGGER.debug("Struct circular references, clzFieldUrl:{}, prev:{}", clzFieldUrl, descriptor.getName());
            if (!StructConfig.INSTANCE.isAllowCircularReferences())
                throw new RuntimeException("loop dependent with key:" + clzFieldUrl + ", prev:" + descriptor.getName());
            return;
        }
        ...
```

目标类型 → 子 Worker 产出形态：

| 字段类型 | 子 Worker 调用 | 缓存 value 形态 |
| --- | --- | --- |
| 数组 | `toListWithGroup(ArrayList::new, refGroupBy)` | `Map<groupKey, T[]>` |
| `Collection` | `toListWithGroup(targetType, refGroupBy)` | `Map<groupKey, Collection<T>>` |
| `Map` | `toMapWithGroup(targetType, refUniqueKey, refGroupBy)` | `Map<groupKey, Map<uniqueKey, T>>` |
| 其它（单值） | `toMap(HashMap::new, refUniqueKey)` | `Map<uniqueKey, T>` |

全部以 `Collections.unmodifiableMap` 存进 `tempRefFieldValueMap`。

### 6.2 取值

`JdkStructFactory#handleReferenceFieldValue(structImpl, fd)`：

```269:274:struct-core/src/main/java/org/struct/core/factory/JdkStructFactory.java
            refKeys = fd.getRefGroupBy().length > 0
                    ? fd.getRefGroupBy()
                    : fd.getRefUniqueKey();
            keys = this.getFieldValuesArray(structImpl, refKeys);
            val = map.get(keys);
```

- `refGroupBy` 优先级高于 `refUniqueKey`。
- 多列 key → `ArrayKey`；单列 → 直接该值。
- 结果若是数组，会 `Arrays.copyOf(..., fieldType)` 修正数组类型（避免 `Object[]` 赋给 `String[]` 的 `ArrayStoreException`）。

### 6.3 聚合（`aggregateBy`，since 4.0）

与普通 ref 的区别：**key 来自父记录的数组/集合字段**，一次性取出多条。

```245:267:struct-core/src/main/java/org/struct/core/factory/JdkStructFactory.java
        if (fd.isAggregateField()) {
            refKeys = new String[]{fd.getAggregateBy()};
            keys = this.getFieldValuesArray(structImpl, refKeys);
            //  key value's type
            Class<?> targetFieldType = fd.getFieldType();
            if (keys.getClass().isArray()) {
                int length = Array.getLength(keys);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(map.get(Array.get(keys, i)));
                }
                val = targetFieldType.isArray() ? list.toArray() : list;
            } else if (keys instanceof Collection ck) {
                ...
            } else if (Map.class.isAssignableFrom(keys.getClass())) {
                throw new UnSupportConvertOperationException("Un support Map.class key yet.");
```

限制：**不支持 Map 类型的聚合 key**。

### 6.4 循环引用

默认 `StructConfig.INSTANCE.allowCircularReferences = true` → 检测到环就跳过（字段值为 null）。
若设为 `false` → 抛 `RuntimeException("loop dependent with key:...")`。
注意：**环检测依赖 `tempRefFieldValueMap` 的写入发生在递归返回之后**，因此自引用/互相引用的表必须显式配置，否则可能得到 null 而非报错。

---

## 7. 中间表示：`StructImpl`

```40:69:struct-core/src/main/java/org/struct/core/StructImpl.java
    public void add(String fieldName, Object val) {
        this.add(fieldName, val, false);
    }
    ...
    public void add(String fieldName, Object val, boolean force) {
        if (fieldName == null || fieldName.isEmpty()) {
            return;
        }
        //  ignore NULL or empty value;
        boolean ignore = (val == null)
                || (val instanceof String && ((String) val).isEmpty());
        if (ignore) {
            return;
        }
```

关键点：
- 字段名映射：`HashMap<String, Object>`，**key 是表头文本 / JSON 字段名**，与 `@StructField(name=...)` 或字段名匹配。
- `add` 会**静默丢弃 null 与空串** —— 这让"该列没填"与"该列不存在"在数据层面等价，`get()` 都返回 `null`。
- 字段名是否 `intern()` 由 `StructInternal.INTERN_FIELD_NAME` 控制。
- `SingleFieldDescriptor#getFieldValueFrom(instance)` 对 `StructImpl` 走 `si.get(this)`；对普通对象走反射。

---

## 8. Bean 过滤（`StructBeanFilter`）

```49:53:struct-core/src/main/java/org/struct/core/filter/StructBeanFilter.java
    @Override
    public final void accept(T t) {
        if (test(t)) {
            cellHandler.accept(t);
        }
    }
```

- 抽象类，`accept` 是 `final`，子类只需实现 `Predicate#test`。
- **必须提供 `public XxxFilter(Consumer<T> cellHandler)` 构造函数**，`StructWorker#wrapCellHandler` 通过 `getConstructor(Consumer.class)` 反射实例化。
- 通过 `@StructSheet(filter = XxxFilter.class)` 配置。
- 注意：过滤发生在**转换成 Bean 之后、写入结果集合之前**。

---

## 9. Spring / Spring Boot 集成

### 9.1 注册流程

```
@SpringBootApplication
  └─（starter）StructAutoConfiguration
        ├─ @Bean StructConfig           —— 把 StructProperties 写入 StructConfig.INSTANCE + ArrayConverter
        ├─ @Bean StructStoreConfig      —— 全局默认：workspace / lazyLoad / watchFile / 调度参数 / banner
        ├─ @Bean StructStoreService     —— BeanPostProcessor，收集所有 StructStore、触发初始化
        ├─ @Bean FileWatcherService     —— 监听 workspace 下文件变更 → store::reload
        └─ @Import AutoConfiguredMapperScannerRegistrar
              └─ ClassPathStructScanner#doScan(AutoConfigurationPackages)
```

或用户显式使用 `@StructScan("com.xxx")` → `StructScannerRegistrar`（`ImportBeanDefinitionRegistrar`）。
两者互斥：`StructAutoConfiguration.StructMapperServiceNotFoundConfiguration` 上有 `@ConditionalOnMissingBean({StructScannerRegistrar.class})`。

### 9.2 `ClassPathStructScanner`

继承 Spring 的 `ClassPathBeanDefinitionScanner`，`useDefaultFilters=false`。

`registerFilters()`：
- **exclude**：非具体类；`MapStructStore`；`ListStructStore`
- **include 1**：同时标注 `@AutoStruct` 与 `@StructSheet`
- **include 2**：具体类且（父类是 `AbstractStructStore` 或实现了 `StructStore`）

`registerBeanDefinition()` 分两类处理：

**A. 本身就是 `StructStore` 实现**（用户自定义 Store）→ 直接注册，注入：
- `StructConstant.CLZ_OF_BEAN` ← `GenericTypeResolver.resolveTypeArguments(clz, StructStore.class)[1]`
- `@AutoStruct` 的 `mapKey` → `MapKeyFieldResolver`；`keyResolverBeanName`；`keyResolverBeanClass`
- `@StructStoreOptions` → `Options.generate(...)`

**B. 是 `@AutoStruct` + `@StructSheet` 的 Struct Bean** → **自动生成**一个 Store BeanDefinition：
- beanName = 原 beanName + `"StructStore"`
- Store 类型选择优先级：`@AutoStruct#clzOfStore()` → 有 `mapKey`/`keyResolverBeanName`/`keyResolverBeanClass` 则用 `MapStructStore` → 兜底 `ListStructStore`
- 构造参数：`cavs.addIndexedArgumentValue(0, gbd.getBeanClass())`（**要求 Store 有单参 `Class<B>` 构造器**）
- `setAutowireMode(AUTOWIRE_BY_NAME)`、`SCOPE_SINGLETON`、`ROLE_APPLICATION`

> ⚠️ 注意 B 分支有一个细节：`generateStructStoreBeanDefinition` 里对 `clzOfStore` 的两次判断都使用 `if`（不是 `else if`），因此 `keyResolverBeanClass` 的判断会覆盖 `mapKey` 的结果（两者都最终指向 `MapStructStore`，行为一致）。

### 9.3 Store 生命周期与状态机

`AbstractStructStore` 用 `AtomicIntegerFieldUpdater` 维护 `status`：

```
NORMAL(0) --casStatusInit()--> INITIALIZING(1) --casStatusDone()--> DONE(2)
```

- `afterPropertiesSet()`：`options == null` 时从容器取 `StructStoreConfig` 生成；`!lazyLoad` 则立即 `initialize()`。
- `initialize()`：`casStatusInit()` 失败说明已有线程在初始化；此时若 `options.isWaitForInit()` 则 `waitForDone()`。
- `reload()`：仅当已 `DONE` 时，`casStatusReset()` 后重新 `initialize()`。
- `MapStructStore#loadStructData`：`WorkerUtil.newWorker(workspace, clzOfBean).toMap(HashMap::new, keyResolver::resolve)`，结果 `unmodifiableMap`；`dispose()` 时置回 `EMPTY_MAP`。
- `MapStructStore` 的 key 解析器解析优先级：`keyResolver` 字段 → `keyResolverBeanName`（容器 byName）→ `keyResolverBeanClass`（容器 byType，其次 `Reflects#newInstance`）→ 都没有则抛 `NoSuchKeyResolverException`。

> ⚠️ `waitForStatus` 是**无 sleep 的忙等自旋**（`for(;;)`）。高竞争下会烧 CPU；如需优化，这是明确的改进点。

### 9.4 热重载

`StructAutoConfiguration#fileWatcherService`：
1. 确保 workspace 目录存在且是目录；
2. `FileWatcherService.newBuilder()` 设置调度参数，`registerAll(workspace.toPath())`，`bootstrap()`；
3. 对每个 `StructStore`，用 `Reflects#resolveStructRelatedFileName(store.clzOfBean())` 解析出该 Bean **依赖的所有数据文件名**（含引用表），逐个 `registerHook(resolveFilePath(workspace, fileName), store::reload)`。

这意味着改一张被引用的子表，所有引用它的父表 Store 也会 reload。

### 9.5 配置属性

| 前缀 | 类 | 关键属性 |
| --- | --- | --- |
| `struct.core.*` | `StructProperties` | `structRequiredDefault` `ignoreEmptyRow` `allowCircularReferences` `arrayConverter.{stringSeparator,stringTrim,ignoreBlank}` |
| `struct.service.*` | `StructServiceProperties` | `workspace` `lazyLoad` `watchFile` `scheduleInitialDelay` `scheduleDelay` `scheduleTimeUnit` `banner` |
| 开关 | `StarterConstant` | `struct.store.enable`（默认 true）、`struct.service.enable`、`struct.watcher.enable` |

`StructAutoConfiguration` 本身由 `@ConditionalOnProperty(prefix="struct.store", name="enable", havingValue="true", matchIfMissing=true)` 控制。

---

## 10. 线程安全与性能

| 组件 | 线程安全性 | 说明 |
| --- | --- | --- |
| `StructWorker` | **非线程安全** | 持有可变的 `tempRefFieldValueMap`、`structFactory`。一次加载一个 Worker |
| `WorkerUtil.HANDLERS_HOLDER` / `FACTORY_BEAN_HOLDER` | 线程安全 | DCL + `volatile` 的 `Holder`，SPI 只加载一次 |
| `ConverterRegistry` | 线程安全 | `ConcurrentHashMap`；但**运行时 register 会立即对所有线程生效** |
| `EnhancedServiceLoader` | 线程安全 | `ConcurrentHashMap` + DCL；`createExtensionInstance` 在 `synchronized(this)` 内实例化 |
| `StructConfig` | 可变全局单例 | enum 单例的字段**非 volatile 非 final**，并发修改不保证可见性；应在启动期一次性设置 |
| `AbstractStructStore#cached` | `volatile` | 读多写少，reload 时整体替换引用 |
| `MapStructStore#size` | `volatile int` | |
| `StructImpl` | 非线程安全 | 单行临时对象 |

性能相关：
- 反射字段访问在 `parseStruct()` 时做一次 `setAccessible(true)`，之后复用 `Field` 对象；`MapKeyFieldResolver` 用 `MethodHandle`。
- `StructInternal.INTERN_FIELD_NAME=true` + `@StructField(cached=true)` 是官方给出的降内存手段。
- 大 `.xlsx`（≥1.5MB）自动排除 usermode Handler（阈值可经 `struct.handler.xlsx.UMThreshold` 调整）。

---

## 11. 异常体系

| 异常 | 抛出场景 |
| --- | --- |
| `StructTransformException` | Handler 解析失败、设置字段值失败（包装原始异常） |
| `NoSuchFieldReferenceException` | `refUniqueKey` / `refGroupBy` 在目标 Bean 上找不到字段；required 引用字段解析为 null |
| `UnSupportConvertOperationException` | 转换器无法处理（如不支持的类型、Map 作为聚合 key） |
| `IllegalAccessPropertyException` | 反射 get/set 字段失败；record 上调用 setter |
| `EndOfExcelSheetException` | SAX 模式提前终止读取（`rowNum >= lastRow`），被 `XlsxSaxStructHandler` 捕获并静默结束 |
| `ServiceNotFoundException` | SPI 找不到实现或实例化失败 |

注意：`handleDataFile` 会捕获 Handler 抛出的所有 `Exception` 继续尝试下一个 Handler，因此**这些异常不一定会冒泡到调用方**；只有全部 Handler 失败才抛 `IllegalArgumentException`。调试时建议把日志级别开到 INFO（`LOGGER.info("{} handle data file failure...")`）。

---

## 12. 已知限制与 Bug（供 AI 规划任务）

> 🔴 **Bug 完整清单见 [`KNOWN-BUGS.md`](KNOWN-BUGS.md)。**
> **P0-0 ~ P0-3、P1-1 ~ P1-5 均已修复**，清单中每条都标注了实际改动与回归测试。剩余 P2 为待办。
>
> 其中 **P0-0（循环引用检测失效）是在提升单元测试覆盖率时发现的**：
> 那 4 行长期没有测试覆盖，实机验证后发现自引用/互相引用都会 `StackOverflowError`。
> **未覆盖的代码往往是 bug 藏身处** —— 提升覆盖率的价值不止于数字。

### 12.1 修复后的空值语义（重要，改动相关代码前必读）

修复统一了「空值」的处理，三处 Converter 行为均有变更：

| 目标类型 | 修复前 | 修复后 |
| --- | --- | --- |
| `Enum` | `null` → **第一个枚举常量**；`""` → 抛异常 | `null` / 空白串 → **`null`** |
| `LocalDate` / `LocalDateTime` | `null` → **NPE** | `null` → **`null`**（与 `DateConverter` 一致） |
| 数组（`ArrayConverter`） | 非数组 targetType → **NPE** | 非数组 targetType → **`null`** |

**数组未被改动的部分**：`originValue == null` 仍返回**空数组**（框架约定：null 输入 → 目标类型的零值，
同数值的 `0`/`0.0`、`BigInteger.ZERO`）。字符串切分路径的 `ignoreBlank`(先) → `trim`(后) 顺序也保持原样。

`StructImpl#add` 本就丢弃 `null` 与空串，因此上述语义与框架的既有约定一致。

### 12.2 `ArrayConverter` 新增 `Collection` / 数组输入

分隔符常量 `ARRAY_CONVERTER_STRING_SEPARATOR`（默认 `"\\|"`）是**正则**，仅适用于 `split`，**不能用于 join**。
因此 JSON 数组、protobuf repeated 字段这类「结构化」值改为以 `List` 形式传入，由 `ArrayConverter` 逐元素转换。
`ConverterRegistry#convertCollection` 内部同样走 `ArrayConverter`，故 `List` 字段一并受益。

### 12.3 其余改进点（P2，未处理）

### 12.2 其余改进点

1. **`ArrayConverter` 对非数组目标类型会 NPE**：`convert()` 中 `Array.newInstance(targetType.getComponentType(), 0)`，当 `targetType` 是 `List.class` 等 `getComponentType()==null` 的类型时抛 NPE。现有两个测试期望它返回 `null`。需二选一修复（改实现 or 改测试）。
2. **`waitForStatus` 忙等**：`AbstractStructStore#waitForStatus` 无退避，建议改为 `CountDownLatch` / `CompletableFuture`。
3. **`StructConfig` 非线程安全发布**：字段缺少 `volatile`。
4. **`ConverterRegistry#convert` 找不到转换器时静默返回原值**：可考虑加一个严格模式开关，避免"类型没转"被静默吞掉。
5. **`resolveFilePath` 对 `classpath:` 找不到资源时返回拼接路径**（可能含 `classpath:` 前缀），最终由 `new File(...).exists()` 报错，错误信息不够直观。
6. **`ExcelUMStructHandler#getExcelCellValue`** 对 `NUMERIC` 做了 int/long/double 收窄；日期格式的单元格在 SAX 与 usermode 两条路径上的值形态可能不一致。
7. **`struct-examples` 无测试**：`src/test` 目录不存在。
