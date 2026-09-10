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

package org.struct.spring.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.struct.annotation.StructSheet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.struct.spring.annotation.AutoStruct;
import org.struct.spring.annotation.StructStoreOptions;
import org.struct.store.StoreOptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class ClassPathStructScannerTest {

    @Test
    public void testConstructors() {
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class)));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false, mock(Environment.class)));
        Assertions.assertNotNull(new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false, mock(Environment.class), mock(ResourceLoader.class)));
    }

    @Test
    public void testGenerateStructStoreBeanDefinition() {
        ClassPathStructScanner scanner = new ClassPathStructScanner(mock(BeanDefinitionRegistry.class));
        GenericBeanDefinition gbd = mock(GenericBeanDefinition.class);
        doReturn(Clz.class).when(gbd).getBeanClass();
        doReturn("Clz").when(gbd).getBeanClassName();
        AnnotatedGenericBeanDefinition definition = scanner.generateStructStoreBeanDefinition(gbd);
        Assertions.assertNotNull(definition);
    }

    @AutoStruct(mapKey = "id", keyResolverBeanClass = org.struct.store.MapKeyFieldResolver.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class Clz {

    }

    @Test
    public void testGenerateStructStoreBeanDefinitionWithStore() {
        ClassPathStructScanner scanner = new ClassPathStructScanner(mock(BeanDefinitionRegistry.class));
        GenericBeanDefinition gbd = mock(GenericBeanDefinition.class);
        doReturn(ClzA.class).when(gbd).getBeanClass();
        doReturn("ClzA").when(gbd).getBeanClassName();
        AnnotatedGenericBeanDefinition definition = scanner.generateStructStoreBeanDefinition(gbd);
        Assertions.assertNotNull(definition);
    }

    @AutoStruct(clzOfStore = ListStructStore.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class ClzA {

    }

    //  ------------------------------------------------------------------
    //  generateStructStoreBeanDefinition branches
    //  ------------------------------------------------------------------

    private static ClassPathStructScanner scanner() {
        return new ClassPathStructScanner(mock(BeanDefinitionRegistry.class));
    }

    private static GenericBeanDefinition beanDefinition(Class<?> clz) {
        GenericBeanDefinition gbd = mock(GenericBeanDefinition.class);
        doReturn(clz).when(gbd).getBeanClass();
        doReturn(clz.getName()).when(gbd).getBeanClassName();
        return gbd;
    }

    /**
     * Without {@code @AutoStruct} the store type falls back to {@link ListStructStore}.
     */
    @Test
    public void testFallbackToListStructStore() {
        AnnotatedGenericBeanDefinition definition = scanner().generateStructStoreBeanDefinition(beanDefinition(PlainClz.class));
        Assertions.assertNotNull(definition);
        Assertions.assertEquals(ListStructStore.class, definition.getBeanClass());
        //  the single arg (Class<B>) constructor argument is the struct bean itself
        Assertions.assertEquals(PlainClz.class,
                definition.getConstructorArgumentValues().getIndexedArgumentValue(0, null).getValue());
        Assertions.assertEquals(BeanDefinition.ROLE_APPLICATION, definition.getRole());
        Assertions.assertEquals(BeanDefinition.SCOPE_SINGLETON, definition.getScope());
    }

    /**
     * An abstract store class is rejected explicitly.
     */
    @Test
    public void testAbstractStoreClassIsRejected() {
        ClassPathStructScanner scanner = scanner();
        GenericBeanDefinition gbd = beanDefinition(AbstractStoreClz.class);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scanner.generateStructStoreBeanDefinition(gbd));
    }

    /**
     * An abstract key resolver class is rejected explicitly.
     */
    @Test
    public void testAbstractKeyResolverIsRejected() {
        ClassPathStructScanner scanner = scanner();
        GenericBeanDefinition gbd = beanDefinition(AbstractKeyResolverClz.class);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scanner.generateStructStoreBeanDefinition(gbd));
    }

    /**
     * {@code keyResolverBeanName} is propagated as a property value.
     */
    @Test
    public void testKeyResolverBeanNameProperty() {
        AnnotatedGenericBeanDefinition definition =
                scanner().generateStructStoreBeanDefinition(beanDefinition(KeyResolverNameClz.class));
        Assertions.assertEquals(MapStructStore.class, definition.getBeanClass());
        Assertions.assertEquals("myKeyResolver",
                definition.getPropertyValues().get(StructConstant.KEY_RESOLVER_BEAN_NAME));
    }

    /**
     * {@code mapKey} produces a {@link MapKeyFieldResolver} property.
     */
    @Test
    public void testMapKeyProperty() {
        AnnotatedGenericBeanDefinition definition =
                scanner().generateStructStoreBeanDefinition(beanDefinition(MapKeyClz.class));
        Assertions.assertEquals(MapStructStore.class, definition.getBeanClass());
        Assertions.assertNotNull(definition.getPropertyValues().get(StructConstant.KEY_RESOLVER));
    }

    /**
     * {@code @StructStoreOptions} is converted into a core {@link StoreOptions} property.
     */
    @Test
    public void testStructStoreOptionsProperty() {
        AnnotatedGenericBeanDefinition definition =
                scanner().generateStructStoreBeanDefinition(beanDefinition(WithOptionsClz.class));
        Object options = definition.getPropertyValues().get(StructConstant.KEY_OPTIONS);
        Assertions.assertNotNull(options);
        Assertions.assertInstanceOf(StoreOptions.class, options);
        Assertions.assertEquals("/ws/", ((StoreOptions) options).getWorkspace());
        Assertions.assertTrue(((StoreOptions) options).isLazyLoad());
    }

    /**
     * A custom (non abstract) store class declared by {@code @AutoStruct} is used.
     */
    @Test
    public void testCustomStoreClass() {
        AnnotatedGenericBeanDefinition definition =
                scanner().generateStructStoreBeanDefinition(beanDefinition(CustomStoreClz.class));
        Assertions.assertEquals(SpringTestDataMapStructStoreImpl.class, definition.getBeanClass());
    }

    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class PlainClz {
    }

    @AutoStruct(clzOfStore = AbstractStructStore.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class AbstractStoreClz {
    }

    @AutoStruct(keyResolverBeanClass = AbstractKeyResolver.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class AbstractKeyResolverClz {
    }

    @AutoStruct(keyResolverBeanName = "myKeyResolver")
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class KeyResolverNameClz {
    }

    @AutoStruct(mapKey = "id")
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class MapKeyClz {
    }

    @StructStoreOptions(workspace = "/ws/", lazyLoad = true)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class WithOptionsClz {
    }

    @AutoStruct(clzOfStore = SpringTestDataMapStructStoreImpl.class)
    @StructSheet(fileName = "t.xlsx", sheetName = "Sheet1")
    class CustomStoreClz {
    }

    abstract static class AbstractKeyResolver implements org.struct.store.StructKeyResolver<Integer, Object> {
    }

    //  ------------------------------------------------------------------
    //  registerBeanDefinition branches
    //  ------------------------------------------------------------------

    /**
     * A plain struct bean gets a generated store bean definition.
     */
    @Test
    public void testRegisterBeanDefinitionGeneratesStore() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(PlainClz.class);

        scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "plainClz"), registry);

        verify(registry).registerBeanDefinition(eq("plainClzStructStore"), any(BeanDefinition.class));
    }

    /**
     * An abstract store is skipped instead of being registered.
     */
    @Test
    public void testRegisterBeanDefinitionSkipsAbstractStore() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(AbstractStoreImpl.class);

        scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "abstractStore"), registry);

        verifyNoInteractions(registry);
    }

    /**
     * A store interface is skipped instead of being registered.
     */
    @Test
    public void testRegisterBeanDefinitionSkipsStoreInterface() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(StoreIface.class);

        scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "storeIface"), registry);

        verifyNoInteractions(registry);
    }

    /**
     * A raw store (its generic types can't be resolved) is rejected explicitly instead of being
     * registered with a null bean type.
     */
    @Test
    public void testRegisterBeanDefinitionRejectsRawStore() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(RawStore.class);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "rawStore"), registry));
    }

    /**
     * A bean class name that can't be resolved fails fast.
     */
    @Test
    public void testRegisterBeanDefinitionUnresolvableClass() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClassName("com.not.exist.Clz");

        Assertions.assertThrows(IllegalStateException.class, () ->
                scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "ghost"), registry));
    }

    /**
     * A custom store whose generic types resolve gets registered as is.
     */
    @Test
    public void testRegisterBeanDefinitionCustomStore() {
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(CustomStoreImpl.class);

        scanner().registerBeanDefinition(new BeanDefinitionHolder(gbd, "customStore"), registry);

        verify(registry).registerBeanDefinition(eq("customStore"), any(BeanDefinition.class));
        Assertions.assertEquals(String.class, gbd.getPropertyValues().get(StructConstant.CLZ_OF_BEAN));
    }

    //  ------------------------------------------------------------------
    //  include / exclude filters
    //  ------------------------------------------------------------------

    /**
     * A scanner that can actually read the classpath.
     */
    private static ClassPathStructScanner scanningScanner() {
        ClassPathStructScanner scanner = new ClassPathStructScanner(mock(BeanDefinitionRegistry.class), false,
                new StandardEnvironment(), new DefaultResourceLoader());
        scanner.registerFilters();
        return scanner;
    }

    /**
     * The core store implementations and everything under {@code org.struct.offheap} are internal -
     * they must never be registered as user stores.
     */
    @Test
    public void testExcludeFilterSkipsInternalPackages() {
        Assertions.assertTrue(scanningScanner().doScan("org.struct.store").isEmpty());
        Assertions.assertTrue(scanningScanner().doScan("org.struct.offheap").isEmpty());
    }

    /**
     * The include filter matches a concrete store that directly extends the core skeleton, and
     * ignores plain / abstract classes.
     */
    @Test
    public void testIncludeFilterMatchesCoreStoreSubclasses() {
        java.util.Set<BeanDefinitionHolder> holders = scanningScanner().doScan("org.struct.scanfixture");
        Assertions.assertEquals(1, holders.size());
        //  the nested class is named ScanFixtures.DirectCoreStore by the bean name generator
        Assertions.assertTrue(holders.iterator().next().getBeanName().contains("DirectCoreStore"));
    }

    /**
     * An abstract key resolver is never wired in as a {@code keyResolverBeanClass} property.
     */
    @Test
    public void testAbstractKeyResolverIsNotSetAsProperty() {
        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setBeanClass(MapStructStore.class);
        AutoStruct anno = AnnotationUtils.findAnnotation(AbstractKeyResolverClz.class, AutoStruct.class);

        scanner().handleStructStoreProperty(gbd, anno, null);

        Assertions.assertNull(gbd.getPropertyValues().get(StructConstant.KEY_RESOLVER_BEAN_CLASS));
    }

    interface StoreIface extends org.struct.store.StructStore<Integer, String> {
    }

    /**
     * Deliberately an inner (non independent) class: the {@code @ComponentScan} based tests in this
     * package would otherwise pick it up and try to register it as a real store bean.
     */
    @SuppressWarnings({"removal", "rawtypes"})
    class RawStore extends org.struct.store.MapStructStore {
    }

    @SuppressWarnings("removal")
    abstract static class AbstractStoreImpl extends MapStructStore<Integer, String> {
    }

    /**
     * Deliberately an inner (non independent) class, see {@link RawStore}.
     */
    @SuppressWarnings("removal")
    class CustomStoreImpl extends MapStructStore<Integer, String> {
    }
}