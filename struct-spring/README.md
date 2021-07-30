## struct-spring模块

配合spring framework使用. 

### 1. @StructScan注解

配置Scan扫描器, 在spring framework启动阶段扫描`@AutoStruct`注解或`StructStore`的子类。

```java
@Configuration
@StructScan(basePackages = "org.struct.spring.support")
public class StructConfiguration  {

}
```


### 2. @AutoStruct注解

当定义`@StructSheet`注解的类增加`@AutoStruct`注解. 由StructScan扫描到后，自动托管结构化数据并根据配置实例化为`MapStructStore`或`ListStructStore`

```java
@AutoStruct(keyResolverBeanName = "dataStructKeyResolver")
@StructSheet(fileName = "t_example_data.xlsx", sheetName = "Sheet1")
public class DataInfo {

}
```

### 3. spring-framework的`xml`配置示例

通过`xml`的方式定义`StructStore`实例。

```xml
<!--    Use keyResolverBeanName    -->
<bean id="store_1" class="org.struct.spring.support.MapStructStore">
    <property name="clzOfBean" value="org.struct.examples.DataInfo"/>
    <property name="keyResolverBeanName" value="dataStructKeyResolver"/>
</bean>
<!--    Use keyResolverBeanClass    -->
<bean id="store_2" class="org.struct.spring.support.MapStructStore">
    <property name="clzOfBean" value="org.struct.examples.DataInfo"/>
    <property name="keyResolverBeanClass" value="org.struct.examples.DataStructKeyResolver"/>
</bean>
<!--  User keyResolver  -->
<bean id="store_3_keyResolver" class="org.struct.examples.DataStructKeyResolver"/>
<bean id="store_3" class="org.struct.spring.support.MapStructStore">
    <property name="clzOfBean" value="org.struct.examples.DataInfo"/>
    <property name="keyResolver" ref="store_3_keyResolver"/>
</bean>
```

### 4. 使用`@StructStoreOptions`细化Store配置

 1. workspace: 工作空间路径
 2. lazyLoad: 是否启用懒加载模式. 避免冗余的配置表数据被记载到内存中
 3. waitForInit: 当启用懒加载模式时, 初始化时是否阻塞工作线程, 持续到加载初始化完成.