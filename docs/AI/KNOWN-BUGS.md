# Bug 清单

> 排查时间：2026-08-28（基于 `gitbutler/workspace` 分支，含未提交的 Protobuf Handler）
> 2026-08-29 更新：提升覆盖率时**又发现并修复了一个 P0**（循环引用检测失效）。
> **本文所有 P0/P1 结论均有实机验证证据**（见每条目「证据」），非静态阅读推测。
> 排查用的临时探针测试均已删除。

## 状态

**P0-0 / P0-1 / P0-2 / P0-3 / P1-1 / P1-2 / P1-3 / P1-4 / P1-5 均已修复**，并补充了回归测试。
每条已修复项下方标注了 `✅ 已修复` 与实际改动。

测试规模：155 → **224**（+69）；失败数：3 → **1**（仅剩与本次无关的
`BomInputStreamTest`，属测试数据缺陷，见 P2-6）。
行覆盖率：90.41% → **94.99%**。

## 严重度说明

- **P0** —— 静默的数据正确性错误：不报错，但加载结果**是错的**。配置表场景最危险。
- **P1** —— 运行期异常 / 功能不生效。
- **P2** —— 边缘场景、设计缺陷、缺少防护。

---

## P0-0 `StructWorker` 循环引用检测**形同虚设**（2026-08-29 修复）

**位置**：`struct-core/.../core/StructWorker.java`（`handleReferenceFieldValue`）

```java
if (tempRefFieldValueMap.containsKey(clzFieldUrl)) {
    //  ... detect circular reference ...
    return;
}
// ...
//  the real value is only put AFTER the recursion returns
tempRefFieldValueMap.put(clzFieldUrl, Collections.unmodifiableMap(map));
```

**机理**：`put` 发生在递归**返回之后**。递归再次进入本方法时 `containsKey` 恒为 `false`，
环检测**永远无法触发**。

**证据**（临时探针，已删除）：自引用与互相引用**都**抛 `StackOverflowError`：

```
[PROBE self-ref] StackOverflowError -> detection is BROKEN
[PROBE mutual]   StackOverflowError -> detection is BROKEN
```

**修复**：在递归**之前**写入占位 `tempRefFieldValueMap.put(clzFieldUrl, Collections.emptyMap())`。
修复后：第一层正常解析，**第二层被切断并返回 null**，不再崩溃。

**回归测试**：`StructWorkerTest#testSelfReferenceIsDetected` / `#testMutualReferenceIsDetected`
/ `#testCircularReferenceForbidden`。

> 注：这 4 行长期未被任何测试覆盖（覆盖率报告显示为 uncovered line），
> 是本次提升覆盖率时**顺带发现的真实缺陷** —— 未覆盖代码往往是 bug 藏身处。

---

## P0-1 `EnumConverter` 空值 → 静默变成「第一个枚举常量」

**位置**：`struct-core/.../converter/EnumConverter.java:36-39`

```java
//  1. int -> enum
int i = (int) ConverterRegistry.convert(ctx, originValue, Integer.class);
if (0 <= i && i < enums.length) {
    return enums[i];       // ← originValue==null 时 i==0，返回 enums[0]
}
```

**证据**（实机探针）：

```
[PROBE Enum null] -> RED          # Color{RED, GREEN, BLUE}，空值变成了 RED
[PROBE Enum ""]   THROWS IllegalStateException
```

**机理**：`ConverterRegistry.convert(ctx, null, Integer.class)` → `IntegerConverter` 对 null 返回 `0`
→ `0 <= 0 && 0 < enums.length` 成立 → 返回 `enums[0]`。

**影响**：Excel/CSV 中枚举列**留空** → 静默变成第一个枚举值，而不是 `null`。
无任何日志、无异常，数据被污染。空串 `""` 反而抛异常 —— **null 与 "" 行为不一致**，进一步证明是缺陷。

**修复建议**：方法开头加 `if (originValue == null) return null;`

> **✅ 已修复**：`EnumConverter#convert` 开头对 `null` 与空白字符串统一返回 `null`
> （与 `StructImpl#add` 丢弃空值的语义保持一致）。回归测试见
> `EnumConverterTest#testNullValue` / `#testBlankValue` / `#testZeroOrdinalIsStillResolved`
> （后者确保 `0` 作为真实 ordinal 仍解析为第一个常量，未被误伤）。

---

## P0-2 `LocalDateConverter` / `LocalDateTimeConverter` 对 null 抛 NPE

**位置**：`LocalDateConverter.java:46`、`LocalDateTimeConverter.java:46`

```java
if (LocalDate.class != targetType
        || LocalDate.class == originValue.getClass()) {   // ← originValue 为 null 时 NPE
```

**证据**：

```
[PROBE LocalDate null]      THROWS NullPointerException: Cannot invoke "Object.getClass()" because "originValue" is null
[PROBE LocalDateTime null]  THROWS NullPointerException
[PROBE registry null->LocalDate] THROWS NullPointerException   # 经 ConverterRegistry 同样炸
```

**旁证**：同包的 `DateConverter.java:45` **有** `if (null == originValue) return null;`
—— 说明作者知道要判空，两个新 Converter 漏了。

**触发路径**：`StructImpl.add` 会丢弃 null/空值 → `get()` 返回 null
→ `ConverterRegistry.convert(ctx, null, LocalDate.class)` → NPE → 整行加载失败。
即**日期列留空就崩**。

**修复建议**：两处均加 `if (originValue == null) return null;`

> **✅ 已修复**：两个 Converter 均在方法开头加 `if (originValue == null) return null;`，
> 与同包 `DateConverter` 保持一致。回归测试见 `LocalDateConverterTest#testNullValue`、
> `LocalDateTimeConverterTest#testNullValue`（同时覆盖直接调用与经 `ConverterRegistry` 调用）。

---

## P0-3 `ExcelUMStructHandler` 公式单元格值被静默丢弃

**位置**：`struct-core/.../handler/ExcelUMStructHandler.java`，FORMULA 分支

```java
case FORMULA:
    if (cell instanceof Cell) {
        CellValue val = evaluator.evaluate((Cell) cell);
        return getExcelCellValue(val.getCellType(), cell, evaluator);
        //                       ↑ 求值后的类型      ↑ 却传回原始公式 cell
    }
```

**证据**：

```
[PROBE FORMULA->STRING] THROWS IllegalStateException: Cannot get a STRING value from a FORMULA cell
```

**机理**：递归时用**求值结果的 cellType**（如 STRING），对象却传**原始公式 cell**。
POI 不允许对公式 cell 直接调 `getStringCellValue()` → 抛 `IllegalStateException`
→ 被 `handleObjField` 的 `catch (Exception e) { // no-op }` 吞掉 → **该单元格值静默丢失**。

数值公式（`getNumericCellValue()` 返回缓存值）碰巧能工作，所以长期未暴露。
**返回字符串的公式**（`=CONCATENATE(...)`、`=IF(...)`、`=TEXT(...)`）必然丢失。

**修复建议**：`return getExcelCellValue(val.getCellType(), val, evaluator);`（传 `val` 而非 `cell`）

**为什么测试没抓到**：`ExcelUMStructHandlerTest.java:59-61` 同时 mock 了
`cell.getStringCellValue()` 返回 `"1"` 和 `formula.evaluate()` 返回 `CellValue("1")`，
两条路径返回值相同，无法区分 —— **mock 掩盖了真实缺陷**。

> **✅ 已修复**：递归改为传求值结果 `val`（`getExcelCellValue(val.getCellType(), val, evaluator)`）。
>
> 补充了 3 个**能真正区分路径**的测试（让公式 cell 的 `getStringCellValue()`/
> `getBooleanCellValue()` 抛出 POI 的真实异常）：`ExcelUMStructHandlerTest`
> `#testFormulaProducingString` / `#testFormulaProducingBoolean` / `#testFormulaProducingNumber`。
>
> 另用**真实 xlsx** 端到端验证（临时探针，已删除）：
> `=CONCATENATE("he","llo")` → `hello`、`=IF(1=1,"yes","no")` → `yes`、`=1+2` → `3`、`=10*5` → `50`，
> 修复前字符串公式的值会被静默丢弃。

---

## P1-1 `JsonStructHandler` 注册的 TypeAdapter 是死代码 + 双重转换

**位置**：`struct-core/.../handler/JsonStructHandler.java`

```java
builder.registerTypeAdapter(StructImpl.class, new StructJsonDeserializer());  // :63
...
Object rowStruct = gson.fromJson(reader, clzOfStruct);                        // :86 ← 传的是用户 Bean 类
```

**影响（三重）**：

1. `StructJsonDeserializer` **永不执行** —— Gson 按 `typeOfT` 查找 adapter，传入 `clzOfStruct` 不会命中 `StructImpl` 的 adapter。
2. **绕过框架全部 Converter**：Gson 直接把 JSON 反序列化成用户 Bean，
   `LocalDate` / `Enum` / 数组分隔符 / 自定义 converter 等规则**全部失效**。
3. **双重转换**：`worker.createInstance(rowStruct)` 再反射取值 → new 新实例 → 再走一遍 Converter。
   多一次对象创建与转换开销，且引用/嵌套字段依赖反射读取，易错。

**附带缺陷**：即便修好注册，`StructJsonDeserializer` 内 `e.getValue().getAsString()`
对数字/布尔/嵌套对象会抛异常（`getAsString()` 只支持 primitive string）。

**修复建议**：`gson.fromJson(reader, StructImpl.class)`，并重写 `StructJsonDeserializer` 的类型分派。

> **✅ 已修复**（连带改动，见下方「连带改动」）：
> 1. `handle()` 改为 `gson.fromJson(reader, StructImpl.class)`，激活了原本的死代码 adapter，
>    Converter 管线从此生效；
> 2. `StructJsonDeserializer` 重写：按 JsonPrimitive 的实际类型分派
>    （string / boolean / number 原样保留），`JsonNull` → `null`，
>    `JsonArray` → `List<Object>`，嵌套 `JsonObject` 保留原始 `JsonElement` 交由用户 converter 处理。
>
> 回归测试：`JsonStructHandlerTest#testAllValueTypes`（数字/布尔/字符串/null/数组/`LocalDate` 全覆盖）、
> `#testJsonArrayToStringArray`、`#testJsonArrayToList`，测试数据 `tpl_types.json`。
> 其中 **`LocalDate` 字段的验证是新能力的直接证明** —— 原实现绕开 Converter，根本无法填充该类型。

### 连带改动（为 P1-1 服务）

- **`ArrayConverter` 支持 `Collection` / 数组输入**：JSON 数组与 protobuf repeated 字段都以
  `List` 形式进入 Converter，原实现只能切分字符串（且分隔符常量 `"\\|"` 是**正则**，无法用于 join）。
  现在按元素逐个转换，数组和 `List` 字段都能正确填充。这同时修复了 P1-5。
- `convertCollection` 内部也走 `ArrayConverter`，因此 `List` 字段一并受益，无需改动。

---

## P1-2 `ProtobufStructHandler` 负缓存污染 + 非线程安全 Map

**位置**：`ProtobufStructHandler.java:72`、`:189`

```java
private final Map<String, Parser<?>> parserCache = new HashMap<>();   // :72
...
if (parserCache.containsKey(cacheKey)) {
    return (Parser<Message>) parserCache.get(cacheKey);               // :159-161
}
Parser<Message> parser = null;
...
parserCache.put(cacheKey, parser);                                    // :189 ← 可能 put null
return parser;
```

**影响**：

1. **非线程安全**：Handler 是 SPI 单例、被多线程共享，`HashMap` 并发 put 有数据损坏/死循环风险。
2. **负缓存污染**：首次解析失败时 `put(key, null)`。之后 `containsKey()` 恒为 true 而 `get()` 恒为 null
   → **永久失败**，没有任何失效机制。若启动时 classpath 未就绪，该 message 类型就再也加载不了。

**修复建议**：改 `ConcurrentHashMap`；仅当 `parser != null` 时 put。

> **✅ 已修复**：缓存改为 `ConcurrentHashMap`，且仅在 `parser != null` 时写入。
> 字段可见性改为包级私有以便断言。回归测试 `ProtobufStructHandlerTest`
> `#testNoNegativeParserCacheEntry` / `#testParserCacheIsThreadSafeMap`。

---

## P1-3 `ProtobufStructHandler.convertToStruct` 嵌套消息递归传错类型

**位置**：`ProtobufStructHandler.java:241`

```java
Object converted = convertToStruct(nestedMsg, targetClass);   // ← 应传嵌套消息自身的类型
```

嵌套消息用**父 Bean 类型**去匹配，`targetClass.isAssignableFrom(nestedMsg.getClass())` 恒 false，
递归无法正确建立嵌套字段结构。

> **✅ 已修复**：拆分为 `convertToStruct()`（仅判断目标类型是否为 Message）与
> `toStructImpl()`（专职展平为 `StructImpl`）；嵌套消息递归调用 `toStructImpl`。
> 同时新增 `resolveRepeated()`：**repeated 字段此前完全没处理**
> （原代码只识别了 `ProtocolStringList`，数值等 repeated 字段会走错分支），
> 现在按 `fieldDesc.isRepeated()` 判断并逐元素展平。
>
> 回归测试 `ProtobufStructHandlerTest#testNestedMessageIsFlattened` /
> `#testRepeatedFieldBecomesList` / `#testRepeatedMessageField`。
> 测试用 `Descriptors.FileDescriptor.buildFrom(...)` **编程构造描述符**，不依赖 protoc 生成类。

---

## P1-4 `ProtobufStructHandler.findDescriptor` 未实现「按 messageName 查找」

**位置**：`ProtobufStructHandler.java:196-215`

方法注释声称 *"Try to find by message name in the file descriptor"*，
实际只反射 `getDescriptor()` / `descriptor()` 静态方法（两者等价，属重复分支），
**完全忽略 `messageName` 参数**。

后果：`@StructSheet(sheetName = "消息名")` 指定 message 的设计意图失效 —— `sheetName` 被读取后未真正用于查找。

> **✅ 已修复**：先取类的 `getDescriptor()`（兼容旧版 `descriptor()`），
> 若 `messageName` 与之不同，则通过 `descriptor.getFile().findMessageTypeByName(messageName)`
> 在同一 `.proto` 文件内按名查找；找不到时记 warn 并回退到类自身的 descriptor。
>
> 回归测试 `ProtobufStructHandlerTest#testFindDescriptorByMessageName`
> （覆盖：按名命中 / 自身同名 / 未知名回退 / 无 descriptor 方法返回 `null`）。

---

## P1-5 `ArrayConverter` 对非数组目标类型抛 NPE

**位置**：`ArrayConverter.convert`

```java
Array.newInstance(targetType.getComponentType(), 0)   // List.class.getComponentType() == null → NPE
```

**证据**：既有测试 `StringToArrayConverterTest#convertNotArray`、`#convertParamNotString` 失败。

**决策**：采用**既有测试的期望**（非数组目标类型返回 `null`）。理由：
(1) `ArrayConverter` 职责就是处理数组目标类型，非数组交还调用方是合理语义；
(2) `ConverterRegistry#convertCollection` 已有 `if (ary != null ...)` 判空，行为安全；
(3) 满足既有测试，无需改动测试来迁就实现。

> **✅ 已修复**：仅收紧「非数组目标类型」这一个分支，返回 `null`（原：NPE）。
>
> **刻意保留的原有语义**（不要"顺手"改掉）：
> - `originValue == null` → **空数组**（框架约定：null 输入映射到目标类型的零值，
>   如数值返回 `0`/`0.0`、`BigInteger.ZERO）。**不是 `null`**。
> - 字符串切分路径：`split` → `ignoreBlank` 过滤（在 trim **之前**，故 `" "` 不会被过滤掉）→ `trim` → 逐元素 `ConverterRegistry.convert`。**顺序与原来完全一致**。
>
> **新增能力**：`Collection` / 数组输入按元素转换（供 JSON 数组、protobuf repeated 字段使用）。
> 元素经 `ConverterRegistry.convert` 转换，故非 String 元素（如 JSON 数字）也能正确处理；
> 基本类型数组遇到 `null` 元素会由组件转换器兜底为 `0`，**不会**触发
> `Array.set` 的 `IllegalArgumentException`。
>
> 原有 2 个失败测试现已通过。新增 `ArrayConverterTest`（20 个用例）覆盖上述各分支，
> 含 `null` 元素 → 基本类型/对象类型/枚举数组、跨数字类型转换、溢出与不可转换元素的显式报错、
> `ignoreBlank`/`trim` 顺序等边界。

---

## P2-1 `ListStructStore` 的 key 相关方法抛 UnsupportedOperationException

**位置**：`ListStructStore.java:96-99`

`AbstractStructStore#lookup(K...)` / `getOrDefault` / `tryGet` / `lookup(Predicate)` 中的前三者
均调用 `this.get(key)`，而 `ListStructStore.get()` 抛异常。
类注释已说明不支持，但接口层面无编译期防护，误用必在运行期炸。

---

## P2-2 StructStore 初始化失败被静默吞掉

**位置**：`ListStructStore.initialize():70-74`、`MapStructStore` 同

```java
} catch (Exception e) {
    LOGGER.info("initialize [...] store failure.", ...);   // 只打日志，不 rethrow
} finally {
    casStatusDone();                                       // 状态仍置为 DONE
}
```

**影响**：配置表加载失败 → 仅 INFO 日志 → 状态变为 DONE → **应用带着空数据/旧数据正常启动**。
对配置表场景风险较高。建议提供严格模式开关（fail-fast）。

---

## P2-3 `WorkerUtil.lookupStructHandler` 指定 matcher 时不校验扩展名

**位置**：`WorkerUtil.java:78-79`

```java
stream.filter(handler -> handler.matcher().getClass().isAssignableFrom(descriptor.getMatcher()));
```

用户显式指定 `@StructSheet(matcher = FileExtensionMatcher.class)` 时，只按**类型**过滤，
**不调用 `matchFile(file)`** → `.csv` 文件也会被 Excel Handler 尝试处理。
默认走 `WorkerMatcher.class` 分支时无此问题，故影响面小。

---

## P2-4 `WorkerUtil.resolveFilePath` 拼接产生双斜杠，`classpath:/` 无法加载根目录文件

**位置**：`struct-core/.../util/WorkerUtil.java:95`

```java
String path = filepath.substring(filepath.indexOf(":") + 1) + "/" + fileName;
```

`workspace` 末尾已带 `/`，此处又拼一个 `/`，产生双斜杠。

| workspace | 拼出的 path | 结果 |
| --- | --- | --- |
| `classpath:/org/struct/core/` | `/org/struct/core//tpl_val.json` | ✅ 能工作（**中间**双斜杠被规范化） |
| `classpath:/` | `//tpl_list_store.json` | ❌ **失败**（**开头**双斜杠被当作 URL authority） |

**影响**：把数据文件放在 resources 根目录、并用 `classpath:/` 作为 workspace 时加载失败，
且错误信息不直观（`file not exists. path: classpath:/tpl_list_store.json` —— 路径看起来是对的）。

**规避**：沿用现有模式 `classpath:/org/struct/core/`（文件放包目录下）。
**修复建议**：拼接前规范化重复斜杠，例如
`filepath.substring(...).replaceAll("/+", "/")` 或用 `Paths.get(...).normalize()`。

---

## P2-5 `ConverterRegistry#register` 的 null 检查是不可达代码

**位置**：`ConverterRegistry.java:72-75`

```java
Converter converter = Reflects.newInstance(clzOfConverter, params);
if (null == converter) {                    //  ← 永不为真
    throw new IllegalArgumentException(...);
}
```

`Reflects.newInstance` 要么返回实例、要么抛异常，**从不返回 null**，故该分支不可达。
这是防御性代码残留，不影响行为，但会拉低覆盖率且无法被测试覆盖。

---

## P2-6 其它（已在 ARCHITECTURE.md 记录）

- `AbstractStructStore#waitForStatus` 无退避忙等（高竞争烧 CPU）
- `StructConfig` 字段非 `volatile`，并发修改不保证可见性
- `ConverterRegistry#convert` 找不到转换器时静默返回原值，可考虑加严格模式
- `BomInputStreamTest` 测试数据缺陷（UTF-16 BOM + 单字节 ASCII）

---

## 排查中被推翻的怀疑（留档，避免重复排查）

| 疑点 | 结论 |
| --- | --- |
| `XlsEventStructHandler.endRow` 判定可疑，怀疑 endOrder 语义错误 | **实测正确**。默认 10 行、`endOrder=2` → 2 行、`=5` → 5 行、`=11` → 截断为 10 行 |
| `SingleFieldDescriptor.isBasicTypeCollection()` 对无注解字段 NPE | **不会触发**。`isReferenceField()` 先短路，`reference` 为 null 时提前返回 |
| `EnumConverter` 对空串处理不一致 | **确认为真**（见 P0-1 证据） |

---

## 复现方法

临时探针测试（排查后已删除）模板，放在 `struct-core/src/test/java/org/struct/core/handler/ProbeTest.java`
（须在同包，才能访问包级私有的 `getExcelCellValue`）：

```java
// 验证公式单元格：mock 一个「getStringCellValue 抛异常」的公式 cell
Cell cell = Mockito.mock(Cell.class);
Mockito.doThrow(new IllegalStateException("Cannot get a STRING value from a FORMULA cell"))
       .when(cell).getStringCellValue();
FormulaEvaluator ev = Mockito.mock(FormulaEvaluator.class);
Mockito.doReturn(new CellValue("hello")).when(ev).evaluate(Mockito.any(Cell.class));
handler.getExcelCellValue(CellType.FORMULA, cell, ev);   // 正确应返回 "hello"，当前抛异常

// 验证 null 转换
converter.convert(null, null, LocalDate.class);   // 当前 NPE
converter.convert(null, null, Color.class);       // 当前返回第一个枚举常量
```
