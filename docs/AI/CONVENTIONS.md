# StructUtil 编码规范与开发模板（AI 参考）

> 配合 [`AGENTS.md`](../../AGENTS.md) 与 [`ARCHITECTURE.md`](ARCHITECTURE.md) 使用。
> 目标：让 AI 产出的代码与仓库既有风格**完全一致**，减少人工返工。

---

## 1. 版权头（强制）

**每个新建的 `.java` 文件**（含测试）顶部必须原样包含如下注释块（第 1 行开始，位于 `package` 之前）：

```java
/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
```

- 年份固定写 `2024.`（带点），作者固定 `TinyZ.`（带点）。
- 新建文件一律用 `Copyright (c) 2024.`；**不要**修改既有文件的年份。
- IDEA 模板定义见 [`idea_copyright_header.xml`](../../idea_copyright_header.xml)。

---

## 2. 命名与结构

| 元素 | 规范 | 示例 |
| --- | --- | --- |
| 包 | 全小写，按职责分层 | `org.struct.core.converter` |
| 类 | 大驼峰；接口不加 `I` 前缀 | `StructWorker`、`StructHandler` |
| SPI 实现 | 以职责结尾 | `XlsxSaxStructHandler`、`EmbeddedConverters` |
| 抽象类 | `Abstract` 前缀 | `AbstractStructStore` |
| 描述符 | `XxxDescriptor` | `SingleFieldDescriptor` |
| 工具类 | `XxxUtil`，`final class` + 私有构造 | `WorkerUtil`、`ConverterUtil` |
| 常量 | `UPPER_SNAKE`；`FileExtensionMatcher` 的文件扩展常量用 `FILE_` 前缀 | `FILE_XLSX` |
| 测试类 | `<被测类>Test` | `JsonStructHandlerTest` |
| 测试内嵌 Bean | 静态内部类，就近声明在测试类底部 | `KeyValueBean` |

**不要**为了"看起来现代"重命名既有类型或包，也不要新增 `XxxManager` / `XxxHelper` 这类与现有命名冲突的后缀。

---

## 3. Javadoc

- 公开 API（尤其是 annotation 属性、`org.struct.core` 下的类）必须有 Javadoc。
- 类级 Javadoc 使用 `@author` + 时间标记，**两种写法都存在，任选其一并保持一致**：
  ```java
  /**
   * @author TinyZ.
   * @version 2020.08.16
   */
  ```
  ```java
  /**
   * @author TinyZ.
   * @date 2020-08-19.
   */
  ```
- 新增注解属性时，`@since` 用主版本号，例如 `@since 4.0`（当前版本 `5.0.0.beta-SNAPSHOT`，新属性写 `@since 5.0`）。
- 行内注释用中文是**允许的且普遍存在**（如 `//  no-op`、`//  1. int -> enum`）。保持简洁，不要写废话注释。
- 允许使用 IntelliJ 的 region 折叠（既有代码中有）：
  ```java
  /// <editor-fold desc="   Protected Methods    "  defaultstate="collapsed">
  ...
  /// </editor-fold>
  ```
  新文件**不强制**使用。

---

## 4. 代码风格

- 缩进：**4 空格**，不使用 Tab。
- 花括号：K&R（同行），`if/for/while` **即使单行也加花括号**是主流，但少数单行省略也存在于既有代码中 —— 新增代码**建议始终加花括号**。
- 导入顺序（既有风格，无强制工具校验）：
  1. 第三方（`com.*`、`org.apache.*`、`org.springframework.*`、`org.junit.*`、`org.slf4j.*`）
  2. `org.struct.*`
  3. `java.*` / `javax.*`
  4. `static` 导入放最后
- 空实现统一写 `//  no-op`，例如：
  ```java
  } catch (Exception e) {
      //  no-op
  }
  ```
- 序列化类必须声明：
  ```java
  @Serial
  private static final long serialVersionUID = 8949543119635057452L;
  ```
- `equals` / `hashCode` / `toString` **全部手写**（不用 Lombok / `Objects` 之外的工具），`hashCode` 用 `31 * result + ...` 累积；数组字段用 `Arrays.equals` / `Arrays.hashCode`。
- 优先使用 Java 新语法：模式匹配 `instanceof`、record pattern、`List.of`、`var`（局部变量，谨慎）、`Stream`。**不要**降级到 Java 8 写法。
- 日志统一 `private static final Logger LOGGER = LoggerFactory.getLogger(Xxx.class);`，使用 `{}` 占位符。

---

## 5. 测试规范

| 项 | 要求 |
| --- | --- |
| 框架 | JUnit 6（`org.junit.jupiter`） + Mockito 5 |
| 断言 | `org.junit.jupiter.api.Assertions.*` |
| 位置 | `struct-core/src/test/java/...`，与被测类同包同名 + `Test` |
| 数据文件 | `struct-core/src/test/resources/org/struct/core/*.xlsx|json|csv|xml` |
| 路径写法 | `WorkerUtil.newWorker("classpath:/org/struct/core/", Xxx.class)` |
| 命名 | 方法名 `test` / `testWithXxx`（既有风格），语义清楚即可 |
| 分组测试 | 参考 `org/struct/core/examples/`（`SimpleFieldBeanTest`、`RefFieldBeanTest`、`OptionalFieldBeanTest`） |

典型测试骨架：

```java
package org.struct.core.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import java.util.ArrayList;

public class XxxStructHandlerTest {

    @Test
    public void test() {
        StructWorker<MyBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MyBean.class);
        ArrayList<MyBean> beans = worker.toList(ArrayList::new);
        Assertions.assertEquals(3, beans.size());
    }

    @StructSheet(fileName = "tpl_xxx.csv")
    public static class MyBean {
        @StructField(name = "key")
        public int key;
        public int val;
    }
}
```

> ⚠️ 构建脚本设置了 `test { ignoreFailures = true }`。**AI 必须主动检查 `build/test-results/test/*.xml`，不能只凭 `BUILD SUCCESSFUL` 判断。**

**当前基线**：`struct-core` 155 个测试、3 个失败（`StringToArrayConverterTest` 两条 + `BomInputStreamTest` 一条），均为既有缺陷，详见 `AGENTS.md` §9.2。
若你的改动导致失败数超过 3，即为引入回归。

---

## 6. Step-by-Step 模板

### 6.1 新增一种数据源 Handler（例如 YAML）

1. 新建 `struct-core/src/main/java/org/struct/core/handler/YamlStructHandler.java`：
   ```java
   package org.struct.core.handler;

   import org.struct.core.StructDescriptor;
   import org.struct.core.StructImpl;
   import org.struct.core.StructWorker;
   import org.struct.core.matcher.FileExtensionMatcher;
   import org.struct.core.matcher.WorkerMatcher;
   import org.struct.exception.StructTransformException;
   import org.struct.spi.SPI;

   @SPI(name = "yaml")
   public class YamlStructHandler implements StructHandler {

       private static final WorkerMatcher MATCHER = new FileExtensionMatcher(".yaml", ".yml");

       @Override
       public WorkerMatcher matcher() {
           return MATCHER;
       }

       @Override
       public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
           StructDescriptor descriptor = worker.getDescriptor();
           //  1. 打开文件（必要时用 BomInputStream 处理 BOM）
           //  2. 逐条解析 → new StructImpl() → struct.add(列名, 值)
           //  3. worker.createInstance(struct).ifPresent(cellHandler)
           //  4. 失败抛 StructTransformException
       }
   }
   ```
2. 在 `FileExtensionMatcher` 中补常量（若复用现有常量则跳过）。
3. **注册（两处都要）**：
   - `struct-core/src/main/resources/META-INF/struct/org.struct.core.handler.StructHandler` 追加一行全限定名；
   - `struct-core/src/main/java/module-info.java` 的 `provides org.struct.core.handler.StructHandler with {...}` 追加该类（并在 `requires` 中补齐新依赖模块）。
4. 补测试 `struct-core/src/test/java/org/struct/core/handler/YamlStructHandlerTest.java` + 资源文件 `struct-core/src/test/resources/org/struct/core/tpl_xxx.yaml`。
5. 更新 `README.md` / `README_en.md` 的支持格式表格。

### 6.2 新增一个类型转换器

- **单个类型 / 局部**：`ConverterRegistry.register(TargetType.class, new MyConverter())`；
  或字段级 `@StructField(converter = MyConverter.class)`（该转换器类需有无参构造）。
- **批量（推荐用于一组相关类型）**：实现 `Converters`
  ```java
  @SPI(name = "my", order = 100)
  public class MyConverters implements Converters {
      @Override
      public Map<Class<?>, Converter> getConverters() {
          return Map.of(TargetType.class, new MyConverter());
      }
  }
  ```
  并注册到 `META-INF/struct/org.struct.core.converter.Converters` + `module-info.java` 的 `provides ... Converters with ...`。
- 转换器实现约定：
  - 目标类型不匹配时**原样返回 `originValue`**（见 `StringConverter`）；
  - 无法转换时抛 `EmbeddedConverters.raiseUnSupportConvert(originValue, targetType)`；
  - `null` 处理要有明确语义（内置数值转换器统一返回 `0` / `0.0`）。

### 6.3 新增一个自定义 `StructStore`（Spring）

1. 继承 `AbstractStructStore<K, B>`（或实现 `StructStore<K, B>`）；
2. **必须提供 `public MyStore(Class<B> clzOfBean)` 构造器**（扫描器用 `addIndexedArgumentValue(0, ...)` 注入）；
3. 实现 `initialize()` / `dispose()` / `get(K)` / `getAll()` / `lookup(Predicate<B>)`；
4. 用 `@AutoStruct(clzOfStore = MyStore.class)` 标注 Struct Bean，或直接把 Store 类放在 `@StructScan` 扫描路径下；
5. 若需要自定义 key：`@AutoStruct(mapKey = "id")` 或实现 `StructKeyResolver`。

### 6.4 新增注解属性（贯通式改动清单）

| 层 | 文件 | 改动 |
| --- | --- | --- |
| 注解 | `org/struct/annotation/StructSheet.java` 或 `StructField.java` | 加属性 + Javadoc + `@since` |
| 描述符 | `core/StructDescriptor.java` 或 `core/SingleFieldDescriptor.java` | 加字段 + getter/setter + `equals`/`hashCode`/`toString` |
| 消费方 | `core/factory/JdkStructFactory.java` 与各 `handler/*` | 读取并使用 |
| 测试 | `src/test/java/org/struct/core/StructDescriptorTest.java` 等 | 覆盖新属性 |
| 文档 | `README.md` / `README_en.md` | 同步说明 |

---

## 7. 常见任务 → 应改动的文件

| 任务 | 主要文件 |
| --- | --- |
| "某列没被读到" | `handler/*`（列名/表头解析）→ `StructImpl#add`（空值被吞）→ `SingleFieldDescriptor` 的 `name` |
| "类型转换不对" | `converter/ConverterRegistry#convert` → 对应 `Converter` → `ConverterUtil` |
| "引用字段是 null" | `StructWorker#handleReferenceFieldValue` → `JdkStructFactory#handleReferenceFieldValue` → `tempRefFieldValueMap` |
| "循环引用死循环" | `StructConfig#allowCircularReferences` + `StructWorker#handleReferenceFieldValue` 的环检测 |
| "record 构造失败" | `FieldDescriptor#compareTo` 的排序 + `JdkStructFactory#newStructInstance` 的 `getDeclaredConstructor` |
| "Spring 没扫到 Bean" | `ClassPathStructScanner#registerFilters` → `StructScannerRegistrar` / `StructAutoConfiguration` |
| "改了文件没热重载" | `FileWatcherService` + `StructAutoConfiguration#fileWatcherService` + `Reflects#resolveStructRelatedFileName` |
| "内存占用高" | `StructInternal.INTERN_FIELD_NAME`、`@StructField(cached=true)`、SAX vs usermode Handler |
| "大 Excel 加载慢/OOM" | `StructInternal.HANDLER_XLSX_UM_LENGTH_THRESHOLD`、`XlsxSaxStructHandler` |

---

## 8. 禁止事项

- ❌ **不带 `--no-daemon` 跑 Gradle**。AI 反复构建会让常驻 daemon 堆积直至内存溢出（见 `AGENTS.md` §2.1）。
- ❌ 把 `gradle.properties` 中的 `org.gradle.daemon` 改回 `true`。
- ❌ 在 `struct-core` 中引入 Spring / Servlet / 任何容器依赖。
- ❌ 让 `struct-core` 反向依赖 `struct-spring` 或 `struct-spring-boot-starter`。
- ❌ 把 `test { ignoreFailures = true }` 改成 `false`（这会改变仓库既有的构建语义；如确需，单独提 issue 讨论）。
- ❌ 修改 `FieldDescriptor#compareTo` 的排序语义而不验证 record 反序列化。
- ❌ 为了"代码整洁"大面积重构既有文件（本仓库偏好**定向小改**）。
- ❌ 新增依赖版本硬编码在子模块 `build.gradle`；一律加到根 `build.gradle` 的 `ext.version_options`。
- ❌ 删除或改动既有公开 API 签名而不评估 `README.md` / `struct-examples` 的影响。
- ❌ 生成 markdown 说明文件（除用户明确要求）；代码优先。

---

## 9. 提交与版本

- 当前版本：`5.0.0.beta-SNAPSHOT`（根 `build.gradle`）。
- 模块化改动应同时覆盖：源码 → SPI 注册 → `module-info.java` → 测试 → 文档。
- 提交前自检命令（**必须 `--no-daemon`**）：
  ```powershell
  .\gradlew.bat --no-daemon :struct-core:cleanTest :struct-core:test :struct-spring:test
  # 然后人工检查 build/test-results/test/*.xml
  ```
  > 加 `cleanTest` 是为了避免 `UP-TO-DATE` 让测试被跳过、误以为通过。
  > 注意 `test { ignoreFailures = true }`：**构建永远 SUCCESSFUL**，必须自己看 XML。
