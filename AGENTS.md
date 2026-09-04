# AGENTS.md — StructUtil 项目 AI 协作指南

> 本文件供 AI（Code Agent / Copilot / Claude / Cursor 等）在本仓库内持续开发时阅读。
> 深入设计细节见 [`docs/AI/ARCHITECTURE.md`](docs/AI/ARCHITECTURE.md)，编码规范见 [`docs/AI/CONVENTIONS.md`](docs/AI/CONVENTIONS.md)。

---

## 0. 一句话定位

**StructUtil** 是一个「结构化数据 → Java Bean」的映射加载框架：把 Excel（`.xls`/`.xlsx`）、CSV、JSON、XML、Protobuf 等**表格式数据文件**，依据注解元数据映射成 Java 对象，支持基础类型、枚举、日期、数组、集合、Map、嵌套引用与聚合；并可无缝接入 Spring / Spring Boot，作为「配置表缓存（`StructStore`）」自动装配、懒加载与文件热重载。

典型场景：游戏/后端配置表（策划 Excel）→ 内存中的强类型缓存。

---

## 1. 技术栈与硬约束

| 项 | 值 | 备注 |
| --- | --- | --- |
| 构建 | Gradle 9.2.0（wrapper） | Windows 用 `gradlew.bat` |
| JDK | **25**（`JavaLanguageVersion.of(25)`） | 代码大量使用 Java 21+ 语法 |
| group / version | `org.structutil` / `5.0.0.beta-SNAPSHOT` | 定义在根 `build.gradle` |
| 编码 | UTF-8（`options.encoding`） | 中文注释合法且常见 |
| 模块系统 | JPMS，`struct-core`、`struct-spring` 均有 `module-info.java` | 新增可导出包必须同步改 `module-info.java` |

关键依赖版本集中在根 `build.gradle` 的 `ext.version_options`：

```groovy
SPRING_BOOT_VERSION: "4.0.0",  SPRING_VERSION: "7.0.1",  JUNIT_VERSION: "6.0.1",
MOCKITO_VERSION: "5.20.0",     LOG4J2_VERSION: "2.25.2", GSON_VERSION: "2.13.2",
POI_VERSION: "5.2.2",          PROTOBUF_VERSION: "4.29.3"
```

**语言级别硬要求**：源码使用 `record` 模式匹配、`instanceof` 模式匹配（如 `if (fieldOrRc instanceof RecordComponent rc)`）、`List.of`、文本块、`Optional` 等。
**禁止**为了兼容性把这些降级成 Java 8 写法。

---

## 2. 构建规范（Windows / PowerShell）

### 2.1 🔴 硬性要求：必须使用 no-daemon 模式

**禁止**让 Gradle 常驻 Daemon。这是强制约定，不是偏好。

```powershell
# ✅ 正确：显式 --no-daemon
.\gradlew.bat --no-daemon :struct-core:test

# ✅ 也可以：项目已全局禁用，不加参数同样是 no-daemon（见下）
.\gradlew.bat :struct-core:test

# ❌ 严禁：任何会拉起/复用常驻 daemon 的用法
.\gradlew.bat --daemon ...
gradle ...          # 直接用 gradle 命令而非 wrapper
```

根目录 [`gradle.properties`](gradle.properties) 已设置 `org.gradle.daemon=false` 作为**兜底**。
但 AI **仍应显式写出 `--no-daemon`**：不要依赖配置文件，别假设它一定存在或没被改回 `true`。

**为什么强制：**

1. **AI 会导致 daemon 进程溢出（首要原因）**。AI 一轮会话中可能触发几十次构建，且不同调用的参数 /
   JVM 参数 / toolchain 各异，Gradle 会为不同配置**各自拉起一个新 daemon**（默认单 daemon 空闲
   3 小时才退出）。这些进程常驻后台持续吃内存，很快堆积到十几个 GiB，最终拖垮机器或 OOM。
   no-daemon（single-use daemon）在构建结束时**立即退出**，进程数恒为 0。
2. **Windows 文件锁**。常驻 daemon 会长期持有 `build/` 与 Gradle user home 的锁，导致 `clean`、
   IDE 同步、以及并发构建失败。

代价是每次构建重付 JVM 启动 + 配置成本（本机约 6~13s）。**这个代价必须接受**。

> **AI 自检**：若怀疑已有 daemon 堆积，执行 `.\gradlew.bat --stop` 清理；
> 或 `jps -l | Select-String GradleDaemon` 查看。
> **不要**把 `--stop` 写进常规构建命令里，只在排障时用。
>
> **实测证据**：仅经过本轮十余次构建，机器上就残留了 2 个常驻 `GradleDaemon` 进程
> （`jps -l` 可见），`gradlew --stop` 后归零。这正是本条强制要求的原因。

### 2.2 常用命令

```powershell
# 全量编译 + 测试
.\gradlew.bat --no-daemon build

# 只编译单个模块（最快反馈）
.\gradlew.bat --no-daemon :struct-core:compileJava
.\gradlew.bat --no-daemon :struct-spring:compileJava
.\gradlew.bat --no-daemon :struct-spring-boot-starter:compileJava

# 跑测试（加 cleanTest 强制重跑，避免 UP-TO-DATE 跳过）
.\gradlew.bat --no-daemon :struct-core:cleanTest :struct-core:test

# 发布到本地仓库
.\gradlew.bat --no-daemon publishToMavenLocal

# 覆盖率报告（struct-core / struct-spring / struct-spring-boot-starter）
.\gradlew.bat --no-daemon codeCoverageReport
```

**注意 UP-TO-DATE**：即使 no-daemon，增量构建状态仍保存在磁盘，未变化的 task 会显示 `UP-TO-DATE`
而**不真正执行**。想确认测试真的跑了，用上面的 `cleanTest test` 组合，或加 `--rerun-tasks`。

> ### ⚠️ 对 AI 最重要的三条
> 0. **所有 Gradle 命令必须加 `--no-daemon`**（详见 §2.1）。AI 反复触发构建会让 daemon 进程堆积溢出。
> 1. 根 `build.gradle` 中设置了 `test { ignoreFailures = true }`，**测试失败不会让构建失败**。
>    你**不能**用 `BUILD SUCCESSFUL` 判断改动是否安全。必须亲自检查：
>    - `struct-core/build/test-results/test/*.xml`（机器可读）
>    - `struct-core/build/reports/tests/test/index.html`
>
>    **当前基线（2026-09-04）：`struct-core` 共 242 个测试，其中 1 个失败
>    （`BomInputStreamTest`，属测试数据缺陷，见 §9.2）。改动后失败数 > 1 即为你引入的回归。**
>
>    ⚠️ 另有 **`struct-spring-boot-starter` 的 `StructAutoConfigurationTest#testAutoConfiguredMapperScannerRegistrar`
>    也是既有失败**（9 个测试中 1 个失败），**不是**你的回归 —— 已通过将生产文件回滚到 HEAD 复跑证实。
>    根因：该测试 stub 的是 `BeanFactory#getBean(String)`（单参），而 Spring Boot 4 的
>    `AutoConfigurationPackages#get` 走的是 `getBean(String, Class)`（双参），未 stub 故返回 null → NPE。
>    这是 Spring Boot 4 升级后的既有测试缺陷，与 struct-core 无关，修它需要动 starter 的测试。

### 2.3 覆盖率

**当前行覆盖率：94.99%**（目标 ≥ 90%）。测量命令：

```powershell
.\gradlew.bat --no-daemon codeCoverageReport
```

- 报告 XML：`build/reports/report.xml`（`html` 已被关闭，只生成 XML）
- 覆盖 `struct-core` / `struct-spring` / `struct-spring-boot-starter` 三个模块
- **必须**先 `cleanTest` 再跑：JaCoCo 的 `.exec` 会跨次累积。若先跑过 `--tests Xxx` 的
  单测再跑全量，结果会**虚高**（曾观测到 93.49% 实为 92.52%）。
- 修改涉及 `StructConfig` / `ConverterRegistry` 等全局单例的测试时，注意**测试间状态污染**
  （见 §9.2）。
>
> 2. **JDK 25 已就位并已登记**（2026-09-04）：JDK 装在 `E:\Program Files\Java\jdk-25`，
>    且 [`gradle.properties`](gradle.properties) 里已写入
>    `org.gradle.java.installations.paths=E:/Program Files/Java/jdk-25`。
>    因此**直接跑 `gradlew.bat` 即可**，不要再传 `-I xxx.gradle` 的 init script。
>    ✅ 已实测：`.\gradlew.bat --no-daemon :struct-core:cleanTest :struct-core:test` 通过，
>    产物 class 文件 major version = **69**（Java 25），`struct-core` 230 个测试 / 1 失败
>    （`BomInputStreamTest` 基线失败）。
>    ⚠️ 不要再把 toolchain 降级到 21：本机 JDK 21 (21.0.7) 仍在，但项目要求就是 25，
>    降级会掩盖 Java 22–25 语法问题。

---

## 3. 模块地图

```
struct-core/                  核心引擎（无 Spring 依赖），module-info: struct.core
  org/struct/annotation/      用户 API 注解：@StructSheet @StructField @StructOptional
  org/struct/core/            引擎主体：StructWorker StructDescriptor FieldDescriptor...
        /core/handler/        数据源处理器（SPI）：Excel/CSV/JSON/Protobuf...
        /core/converter/      类型转换器（SPI）：ConverterRegistry + 内置转换器
        /core/factory/        Bean 结构解析工厂（SPI）：JdkStructFactory
        /core/matcher/        文件匹配与优先级：WorkerMatcher / FileExtensionMatcher
        /core/filter/         Bean 级过滤器：StructBeanFilter
  org/struct/spi/             自研 SPI 加载器（非 JDK ServiceLoader）
  org/struct/util/            工具类：Reflects WorkerUtil ConverterUtil Strings BomInputStream
  org/struct/support/         FileWatcherService（文件变更监听，热重载基础）
  org/struct/exception/       6 个异常类型

struct-spring/                Spring 集成（ClassPath 扫描 + StructStore），module-info: struct.spring
  org/struct/spring/annotation/  @StructScan @AutoStruct @StructStoreOptions
  org/struct/spring/support/     ClassPathStructScanner StructStore(接口) MapStructStore ListStructStore
  org/struct/spring/handler/     XmlStructHandler（XML 数据源处理器）

struct-spring-boot-starter/   Spring Boot 自动装配
  StructAutoConfiguration / StructProperties / StructServiceProperties
  StructStoreService（BeanPostProcessor，负责注入与生命周期）
  StructStoreServiceHealthContributorAutoConfiguration（Actuator 健康检查）

struct-all/                   BOM 聚合模块（pom packaging，无源码）
struct-examples/              Spring Boot 示例应用（MyApplication + beans.xml + 数据文件）
```

依赖方向：`struct-examples` → `struct-spring-boot-starter` → `struct-spring` → `struct-core`。**严禁反向依赖**。

---

## 4. 架构速览

### 4.1 加载主流程

```
StructWorker.toList()/toMap()
   │
   ├─1. checkStructFactory()
   │      WorkerUtil.structFactory(clz, worker)              ← SPI: StructFactoryBean
   │      └─ JdkStructFactory.parseStruct()                  ← 扫描字段，建 FieldDescriptor
   │            └─ 对每个字段 createFieldDescriptor()
   │                  ├─ @StructOptional → OptionalDescriptor（内含多个 SingleFieldDescriptor）
   │                  └─ 否则           → SingleFieldDescriptor
   │            └─ worker.handleReferenceFieldValue(...)     ← 递归预加载被引用表
   │
   ├─2. handleDataFile(Consumer<T>)
   │      WorkerUtil.resolveFilePath(workspace, fileName)    ← classpath: / file: / 裸路径
   │      WorkerUtil.lookupStructHandler(descriptor, file)   ← 按 matcher + order 排序
   │      for handler in handlers: try { handler.handle(...) ; return } catch { 下一个 }
   │      StructHandler 解析一行/一条 → StructImpl(字段名→值)
   │            → worker.createInstance(StructImpl)
   │                  → JdkStructFactory.newStructInstance()
   │                        → 逐字段 handleInstanceFieldValue()（required 校验 + 类型转换 + 引用注入）
   │      ↓
   │  描述符.getFilter() 不为空 → 用 StructBeanFilter 包装 Consumer（过滤掉的行不进入结果）
   │
   └─3. 收集到 Collection / Map（toList / toMap / toListWithGroup / toMapWithGroup）
```

### 4.2 三大扩展 SPI

| SPI 接口 | 注册文件 | 作用 | 内置实现 |
| --- | --- | --- | --- |
| `StructHandler` | `META-INF/struct/org.struct.core.handler.StructHandler` | 解析一种数据格式 | `JsonStructHandler` `CsvStructHandler` `XlsEventStructHandler` `XlsxSaxStructHandler` `ExcelUMStructHandler` `ProtobufStructHandler`（core）；`XmlStructHandler`（spring） |
| `Converters` | `META-INF/struct/org.struct.core.converter.Converters` | 批量注册类型转换器 | `EmbeddedConverters`（`order=0`） |
| `StructFactoryBean` | `META-INF/struct/...StructFactoryBean` | 创建 `StructFactory`（Bean 结构解析器） | `DefaultStructFactoryBean` → `JdkStructFactory` |

> **SPI 加载器是自研的 `org.struct.spi.EnhancedServiceLoader`**，不是 JDK 的 `ServiceLoader`。
> 它会**同时**扫描 `META-INF/services/<接口全名>` 和 `META-INF/struct/<接口全名>` 两个目录。
> 实现类用 `@SPI(name=..., order=...)` 标注；`order` 升序排序（`load()` 取最后一个，`loadAll()` 返回全部）。

---

## 5. 核心概念词汇表

| 概念 | 含义 |
| --- | --- |
| **Struct Bean** | 被 `@StructSheet` 标注的目标 Java 类（POJO 或 `record`） |
| **StructDescriptor** | 类级元数据：`fileName` / `sheetName` / `startOrder` / `endOrder` / `matcher` / `filter` |
| **FieldDescriptor** | 字段元数据抽象基类，实现 `Comparable`（排序决定 record 构造参数顺序） |
| **SingleFieldDescriptor** | 单个字段：`name` `reference` `refGroupBy` `refUniqueKey` `aggregateBy` `aggregateType` `required` `cached` `converter` |
| **OptionalDescriptor** | `@StructOptional` 多候选字段，按顺序取第一个解析出非 null 值的候选 |
| **StructImpl** | 一行数据的临时中间表示（`HashMap<字段名, 值>`），`add()` 会忽略 null/空串 |
| **StructWorker** | 一次加载任务的驱动者，持有 workspace / descriptor / factory / 引用值缓存 |
| **WorkerMatcher** | 决定某个 Handler 能否处理该文件 + 优先级（`HIGHEST=MIN_VALUE`，`LOWEST=MAX_VALUE`） |
| **StructStore** | Spring 侧的「配置表缓存」抽象（`get/getAll/lookup/reload/dispose`） |
| **Aggregate（聚合）** | `@StructField(aggregateBy="父字段")`，父记录的数组/集合字段值作为 key 批量取子表数据 |

---

## 6. 扩展点速查（改哪类需求，动哪个文件）

| 需求 | 落点 |
| --- | --- |
| 支持新的数据格式（YAML、DB…） | 新建 `XxxStructHandler implements StructHandler` → `@SPI` → 写进 `META-INF/struct/org.struct.core.handler.StructHandler` → **同步 `module-info.java` 的 `provides`** |
| 新增/修改类型转换规则 | `struct-core/.../converter/`；单类型 → `ConverterRegistry.register()`；批量 → 实现 `Converters` SPI |
| 调整全局行为 | `StructConfig`（枚举单例，可运行时改）+ `StructInternal`（静态字段，由系统属性初始化） |
| 新增类级注解属性 | `annotation/StructSheet.java` → `core/StructDescriptor.java` → 各 Handler 消费 |
| 新增字段级注解属性 | `annotation/StructField.java` → `core/SingleFieldDescriptor.java` → `factory/JdkStructFactory.java` 消费 |
| 改 Spring 扫描/注册规则 | `spring/support/ClassPathStructScanner.java`（include/exclude filter + BeanDefinition 生成） |
| 改 Spring Boot 默认配置 | `spring-boot-starter/.../StructProperties.java` / `StructServiceProperties.java` / `StarterConstant` |
| 改热重载 | `support/FileWatcherService.java` + `StructAutoConfiguration#fileWatcherService` |

---

## 7. AI 改动前检查清单

- [ ] **🔴 先读 [`docs/AI/KNOWN-BUGS.md`](docs/AI/KNOWN-BUGS.md)**：其中 3 个 P0 是**静默的数据正确性错误**。若你的改动触及 `converter/` 或 `handler/`，先确认没有踩到或加重它们。
- [ ] **🔴 所有 Gradle 命令带 `--no-daemon`**（§2.1）。AI 反复构建会让常驻 daemon 堆积直至内存溢出 —— 这是最容易犯且后果最严重的一条。
- [ ] **版权头**：每个新 Java 文件顶部必须有 Apache-2.0 头（模板见 `idea_copyright_header.xml`，年份用 `Copyright (c) 2024. - TinyZ.`）。
- [ ] **包名**：新增类必须落在 `module-info.java` 已 `exports` 的包中；若新增包，需同步 `exports`。
- [ ] **SPI 双注册**：新增 SPI 实现时，**`META-INF/struct/...` 文件与 `module-info.java` 的 `provides` 必须同时更新**（classpath 模式走前者，module 模式走后者）。漏掉 `provides` 不只是"模块模式下加载不到"——**会直接导致 `compileJava` 失败**（`module-info` 的 `provides` 要求实现类与接口在同一模块内可解析）。同理新依赖需在 `requires` 中声明。
- [ ] **序列化**：`FieldDescriptor` 及其子类实现了 `Serializable`，新增字段时保持 `@Serial private static final long serialVersionUID`。改字段会影响 `equals/hashCode/toString` —— 三者需同步更新（项目风格是全部手写 + `Objects.hash`）。
- [ ] **测试**：新增/修改行为必须有 JUnit 6 测试；`struct-core` 现有 45 个测试类可参考。测试数据文件放 `struct-core/src/test/resources/`，通过 `new StructWorker<>("classpath:/org/struct/core/", Xxx.class)` 加载。
- [ ] **不要**把 Spring 依赖引入 `struct-core`；**不要**让 `struct-core` 依赖 `struct-spring`。
- [ ] **不要**改动 `test { ignoreFailures = true }`，改为自己核查测试结果。
- [ ] **不要**把 `gradle.properties` 里的 `org.gradle.daemon` 改回 `true`。
- [ ] 修改公开 API 时同步更新 `README.md` / `README_en.md` / `FAQ.md`。

---

## 8. 易踩的坑（读代码时特别注意）

1. **Handler 降级链**：`StructWorker#handleDataFile` 会按顺序尝试所有匹配的 Handler，前一个抛异常就试下一个，全部失败才抛 `IllegalArgumentException`。因此 Handler 内部"解析失败"最好抛出明确异常，不要静默返回空数据。
2. **引用字段递归**：`JdkStructFactory#createSingleFieldDescriptor` 里对每个字段都会调用 `worker.handleReferenceFieldValue()`，该方法会**递归创建子 Worker 并完整加载被引用表**，结果缓存在 `tempRefFieldValueMap`（key = `ref类全名:字段名`）。
   循环引用由 `StructConfig.INSTANCE.isAllowCircularReferences()` 控制，默认**允许**（发现环时直接 return，字段值为 null）。
3. **字段顺序敏感**：`JdkStructFactory#newStructInstance` 对 `record` 走 `getDeclaredConstructor(argTypes)`，参数顺序 = `beanFieldsList` 顺序 = `FieldDescriptor.compareTo` 的排序结果（普通字段 < 引用字段 < 带自定义 converter 的字段；`OptionalDescriptor` 排最后）。改动 `compareTo` 会**破坏 record 反序列化**。
4. **required 语义**：`required` 只对**非引用字段**在 `JdkStructFactory#handleInstanceFieldValue` 中校验；引用字段的 required 在 `handleReferenceFieldValue` 中校验。默认值来自 `StructConfig.INSTANCE.isStructRequiredDefault()`（默认 `false`）。
5. **空值被吞**：`StructImpl#add` 会忽略 `null` 与空字符串，导致 `StructImpl.get()` 返回 `null`；这对"字段值为空字符串"的场景是有意设计。
6. **`ArrayConverter` 默认分隔符是正则 `\\|`**，且 trim 默认开启、ignoreBlank 默认关闭。修改默认值通过 `StructInternal` 的静态字段或系统属性（见下）。
7. **`waitForInit` 是忙等**：`AbstractStructStore#waitForStatus` 是 **无 sleep 的自旋循环**（`for(;;)`）。不要在生产路径上依赖它，也不要"顺手改成阻塞"而不评估影响。
8. **Options 是包级私有**：`org.struct.spring.support.Options` 是 `class Options`（非 public），外部不可直接引用。

**可通过系统属性调整的行为**（`org.struct.core.StructInternal` 静态块读取）：

| 系统属性 | 默认值 | 作用 |
| --- | --- | --- |
| `struct.core.internFieldName` | `true` | 字段名 `String.intern()`，省内存 |
| `struct.handler.xlsx.UMThreshold` | `1572864`（1.5MB） | 超过该大小禁用 Excel usermode Handler |
| `struct.array-converter.stringSeparator` | `\\|` | 数组字符串分隔符（正则） |
| `struct.array-converter.stringTrim` | `true` | 数组元素是否 trim |
| `struct.array-converter.ignoreBlank` | `false` | 是否丢弃空元素 |

---

## 9. 当前工作区状态（本次分析时的快照）

`git status`（branch `gitbutler/workspace`）中的未提交改动：

- `M build.gradle`、`M struct-core/build.gradle`
- `M struct-core/src/main/java/org/struct/core/matcher/FileExtensionMatcher.java` —— 新增了 `.proto` / `.protobuf` 扩展名常量
- `M struct-core/src/main/resources/META-INF/struct/org.struct.core.handler.StructHandler` —— 注册了 `ProtobufStructHandler`
- `?? struct-core/src/main/java/org/struct/core/handler/ProtobufStructHandler.java` —— 新增的 Protobuf Handler

本次**新增**的文件：

- `?? gradle.properties` —— 关闭 Gradle Daemon（`org.gradle.daemon=false`）
- `?? AGENTS.md`、`?? docs/`、`?? .github/copilot-instructions.md` —— AI 协作文档
- `M struct-core/src/main/java/module-info.java` —— 补 `requires com.google.protobuf;` 与 Protobuf Handler 的 `provides`

**本次已修复的 3 个阻断编译的问题**（修复后 `struct-core` / `struct-spring` / `struct-spring-boot-starter` 全部编译通过）：

| # | 文件 | 问题 | 修复 |
| --- | --- | --- | --- |
| 1 | `core/handler/ProtobufStructHandler.java:27` | `import org.slfj.LoggerFactory;` 包名拼写错误 | → `org.slf4j.LoggerFactory` |
| 2 | `module-info.java` | 缺 `requires com.google.protobuf;`，且 `provides ... StructHandler` 未含 `ProtobufStructHandler` | 补 `requires` + `provides` |
| 3 | `core/handler/ProtobufStructHandler.java:181` | `DynamicMessage.newParser(Descriptor)` 在 protobuf-java 4.x **不存在** | → `DynamicMessage.getDefaultInstance(descriptor).getParserForType()` |

### 9.2 测试基线（1 个既有失败，非回归）

| 测试 | 现象 | 性质 |
| --- | --- | --- |
| `BomInputStreamTest#test` | `expected: <abc> but was: <鎱㈡實>` | **测试数据缺陷**：用 UTF-16BE/LE 的 BOM 前缀 + 单字节 ASCII 内容（`0x61 0x62 0x63`），按 UTF-16 解码得到汉字而非 `abc`。第 36–37 行的 UTF-32 同类用例已被注释掉。**未修** —— 修它需要重写测试数据为对应编码的真实内容，属独立任务 |

> 原基线中的 `StringToArrayConverterTest` 两条失败已在 P1-5 修复中消除（见 §9.4）。

### 9.2.1 测试间状态污染（重要）

`StructConfig` 是**进程级单例**，JUnit 不保证测试顺序。任何修改它的测试**必须**在
`@AfterEach` 中恢复原值，否则会污染后续测试 —— 表现为「单跑通过、全量跑失败」。

曾有 `StructConfigTest` 把 `allowCircularReferences` 设为 `false` 且从不恢复，
导致后续的循环引用测试在全量运行时失败。已修复。

**编写规则**：
- 修改 `StructConfig` / `ConverterRegistry` / `StructInternal` 等全局状态的测试，一律
  `@BeforeEach` 保存 + `@AfterEach` 恢复。
- 不依赖全局状态的**默认值**做断言；需要特定值时显式设置（参见
  `StructWorkerTest#withCircularReferencesAllowed`）。

### 9.3 其它遗留待办

1. `FileExtensionMatcher` 已定义 `FILE_PROTO = ".proto"`，但 `ProtobufStructHandler` 的 Matcher 只用了 `.protobuf` / `.bin` / `.pbf`，**未使用 `.proto`**。需确认是否有意。
2. `ProtobufStructHandler` 目前只有**单元级**测试（用编程构造的 protobuf 描述符），
   **缺端到端测试**（真实 `.protobuf` 二进制文件 + 生成的 Message 类）。
   本模块没有 protoc 步骤，加端到端测试需要引入 protoc Gradle 插件或提交生成代码 —— 需作者决策。
3. `struct-examples/src/test` 目录不存在，示例模块无测试。
4. P2 级问题（见 `KNOWN-BUGS.md`）尚未处理。

### 9.4 已完成的 P0/P1 修复（2026-08-28）

测试规模 **155 → 182**（新增 27），失败数 **3 → 1**。

| Bug | 修复文件 | 新增测试 |
| --- | --- | --- |
| P0-1 枚举空值 → 第一个常量 | `EnumConverter` | `EnumConverterTest` +3 |
| P0-2 日期空值 NPE | `LocalDateConverter`、`LocalDateTimeConverter` | 各 +1 |
| P0-3 公式单元格值丢失 | `ExcelUMStructHandler` | `ExcelUMStructHandlerTest` +3 |
| P1-1 JSON 绕过 Converter | `JsonStructHandler` | `JsonStructHandlerTest` +3、新资源 `tpl_types.json` |
| P1-2 负缓存污染 / 非线程安全 | `ProtobufStructHandler` | `ProtobufStructHandlerTest` +7（新文件） |
| P1-3 嵌套消息递归 / repeated | 同上 | 同上 |
| P1-4 未按 messageName 查找 | 同上 | 同上 |
| P1-5 `ArrayConverter` NPE | `ArrayConverter` | `ArrayConverterTest` +8（新文件） |

**行为变更请注意**（均为修复所需，非随意改动）：

- 枚举/日期的**空值现在为 `null`**（原：枚举变第一个常量、日期抛 NPE）。
- 数组**未改**：空值仍返回空数组；仅「非数组 targetType」由 NPE 变为返回 `null`。
- `JsonStructHandler` 现在走完整的 Converter 管线，`LocalDate`、自定义 converter 等**从失效变为生效**。
  若你的 JSON 此前依赖 Gson 原生反序列化行为，需复核。

---

## 10. 文档索引

| 文件 | 内容 |
| [`docs/AI/KNOWN-BUGS.md`](docs/AI/KNOWN-BUGS.md) | 🔴 **Bug 清单**（含实机证据、严重度分级；P0/P1 已修复并标注改动，P2 待办）。**改动相关代码前必读** |
| [`AGENTS.md`](AGENTS.md) | 本文件：项目总览、命令、扩展点、坑位 |
| [`docs/AI/ARCHITECTURE.md`](docs/AI/ARCHITECTURE.md) | 深入架构：加载时序、SPI 机制、类型转换、引用/聚合、Spring 集成、线程安全 |
| [`docs/AI/CONVENTIONS.md`](docs/AI/CONVENTIONS.md) | 编码规范、版权头、测试规范、新增扩展的 Step-by-Step 模板 |
| [`.github/copilot-instructions.md`](.github/copilot-instructions.md) | Copilot 入口（指向本文件） |
| [`README.md`](README.md) / [`README_en.md`](README_en.md) | 用户文档（中文 / 英文） |
| [`FAQ.md`](FAQ.md) | 常见问题 |
