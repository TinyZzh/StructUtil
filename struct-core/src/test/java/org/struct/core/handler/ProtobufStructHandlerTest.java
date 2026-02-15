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

package org.struct.core.handler;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.core.handler.ProtobufStructHandler.DescriptorPool;
import org.struct.core.handler.testproto.PersonProto;
import org.struct.core.matcher.FileExtensionMatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtobufStructHandlerTest {

    @TempDir
    Path tempDir;

    // Three valid Person messages (Alice, Bob, Charlie)
    private static final byte[] PERSON_DATA = new byte[]{
        (byte)0x1B, (byte)0x08, (byte)0x01, (byte)0x12, (byte)0x05, (byte)0x41, (byte)0x6C, (byte)0x69, (byte)0x63, (byte)0x65, (byte)0x1A, (byte)0x0E, (byte)0x61, (byte)0x6C, (byte)0x69, (byte)0x63, 
        (byte)0x65, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x19, (byte)0x17, (byte)0x08, (byte)0x02, (byte)0x12, 
        (byte)0x03, (byte)0x42, (byte)0x6F, (byte)0x62, (byte)0x1A, (byte)0x0C, (byte)0x62, (byte)0x6F, (byte)0x62, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, 
        (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x1E, (byte)0x1F, (byte)0x08, (byte)0x03, (byte)0x12, (byte)0x07, (byte)0x43, (byte)0x68, (byte)0x61, (byte)0x72, (byte)0x6C, (byte)0x69, (byte)0x65, 
        (byte)0x1A, (byte)0x10, (byte)0x63, (byte)0x68, (byte)0x61, (byte)0x72, (byte)0x6C, (byte)0x69, (byte)0x65, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, 
        (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x23
    };

    // Five Person messages for testing start/end order
    private static final byte[] PERSON_FIVE_DATA = new byte[]{
        (byte)0x19, (byte)0x08, (byte)0x01, (byte)0x12, (byte)0x07, (byte)0x50, (byte)0x65, (byte)0x72, (byte)0x73, (byte)0x6F, (byte)0x6E, (byte)0x31, (byte)0x1A, (byte)0x0A, (byte)0x31, (byte)0x40, 
        (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x15, (byte)0x19, (byte)0x08, (byte)0x02, (byte)0x12, (byte)0x07, (byte)0x50, 
        (byte)0x65, (byte)0x72, (byte)0x73, (byte)0x6F, (byte)0x6E, (byte)0x32, (byte)0x1A, (byte)0x0A, (byte)0x32, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, 
        (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x16, (byte)0x19, (byte)0x08, (byte)0x03, (byte)0x12, (byte)0x07, (byte)0x50, (byte)0x65, (byte)0x72, (byte)0x73, (byte)0x6F, (byte)0x6E, (byte)0x33, 
        (byte)0x1A, (byte)0x0A, (byte)0x33, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x17, (byte)0x19, (byte)0x08, 
        (byte)0x04, (byte)0x12, (byte)0x07, (byte)0x50, (byte)0x65, (byte)0x72, (byte)0x73, (byte)0x6F, (byte)0x6E, (byte)0x34, (byte)0x1A, (byte)0x0A, (byte)0x34, (byte)0x40, (byte)0x74, (byte)0x65, 
        (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, (byte)0x6F, (byte)0x6D, (byte)0x20, (byte)0x18, (byte)0x19, (byte)0x08, (byte)0x05, (byte)0x12, (byte)0x07, (byte)0x50, (byte)0x65, (byte)0x72, 
        (byte)0x73, (byte)0x6F, (byte)0x6E, (byte)0x35, (byte)0x1A, (byte)0x0A, (byte)0x35, (byte)0x40, (byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x2E, (byte)0x63, (byte)0x6F, (byte)0x6D, 
        (byte)0x20, (byte)0x19
    };

    // Empty length message (length = 0)
    private static final byte[] PERSON_EMPTY_LEN_DATA = new byte[]{
        (byte)0x00
    };

    // Invalid protobuf data
    private static final byte[] PERSON_INVALID_DATA = new byte[]{
        (byte)0x0A, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09
    };

    @Test
    void testDescriptorPoolClearCache() {
        DescriptorPool pool = new DescriptorPool();
        pool.clearCache();
        Assertions.assertNull(pool.getDescriptor(String.class));
    }

    @Test
    void testDescriptorPoolWithCachedDescriptor() {
        DescriptorPool pool = new DescriptorPool();
        pool.clearCache();
        
        Descriptors.Descriptor mockDesc = mock(Descriptors.Descriptor.class);
        
        try {
            Field field = DescriptorPool.class.getDeclaredField("cachedDescriptor");
            field.setAccessible(true);
            field.set(pool, mockDesc);
            
            Descriptors.Descriptor result = pool.getDescriptor(String.class);
            Assertions.assertEquals(mockDesc, result);
        } catch (Exception e) {
            Assertions.fail("Failed to set cached descriptor: " + e.getMessage());
        }
    }

    @Test
    void testMatcherOrder() {
        FileExtensionMatcher matcher = new FileExtensionMatcher(10, FileExtensionMatcher.FILE_PROTOBUF);
        Assertions.assertEquals(10, matcher.order());
    }

    @Test
    void testMatcherEquals() {
        FileExtensionMatcher matcher1 = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        FileExtensionMatcher matcher2 = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        Assertions.assertEquals(matcher1, matcher2);
    }

    @Test
    void testMatcherHashCode() {
        FileExtensionMatcher matcher1 = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        FileExtensionMatcher matcher2 = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        Assertions.assertEquals(matcher1.hashCode(), matcher2.hashCode());
    }

    @Test
    void testMatcherToString() {
        FileExtensionMatcher matcher = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        String str = matcher.toString();
        Assertions.assertTrue(str.contains("FileExtensionMatcher"));
        Assertions.assertTrue(str.contains(".protobuf"));
    }

    @Test
    void testConstructorWithDescriptorPool() {
        DescriptorPool pool = new DescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        Assertions.assertNotNull(handler);
    }

    @Test
    void testDefaultConstructor() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        Assertions.assertNotNull(handler);
        Assertions.assertNotNull(handler.matcher());
    }

    @Test
    void testMatcher() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        Assertions.assertNotNull(handler.matcher());
        
        File testFile = mock(File.class);
        doReturn(true).when(testFile).exists();
        doReturn(true).when(testFile).canRead();
        doReturn("test.protobuf").when(testFile).getName();
        Assertions.assertTrue(handler.matcher().matchFile(testFile));
        
        File csvFile = mock(File.class);
        doReturn(true).when(csvFile).exists();
        doReturn(true).when(csvFile).canRead();
        doReturn("test.csv").when(csvFile).getName();
        Assertions.assertFalse(handler.matcher().matchFile(csvFile));
        
        Assertions.assertFalse(handler.matcher().matchFile(null));
    }

    @Test
    void testReadDelimitedFrom() throws IOException {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        File tempFile = tempDir.resolve("delimited.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(10);
            for (int i = 0; i < 10; i++) {
                fos.write(i);
            }
        }
        
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] data = handler.readDelimitedFrom(fis);
            Assertions.assertNotNull(data);
            Assertions.assertEquals(10, data.length);
        }
    }

    @Test
    void testReadDelimitedFromEmptyFile() throws IOException {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        File emptyFile = tempDir.resolve("empty.protobuf").toFile();
        emptyFile.createNewFile();
        
        try (FileInputStream fis = new FileInputStream(emptyFile)) {
            byte[] data = handler.readDelimitedFrom(fis);
            Assertions.assertNull(data);
        }
    }

    @Test
    void testReadDelimitedFromMultiByteVarint() throws IOException {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        File tempFile = tempDir.resolve("multibyte.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(0x80);
            fos.write(0x01);
            for (int i = 0; i < 128; i++) {
                fos.write(i);
            }
        }
        
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] data = handler.readDelimitedFrom(fis);
            Assertions.assertNotNull(data);
            Assertions.assertEquals(128, data.length);
        }
    }

    @Test
    void testReadDelimitedFromWithIncompleteData() throws IOException {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        File tempFile = tempDir.resolve("incomplete.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(10);
            fos.write(1);
            fos.write(2);
        }
        
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] data = handler.readDelimitedFrom(fis);
            Assertions.assertNull(data);
        }
    }

    @Test
    void testReadDelimitedFromWithZeroLength() throws IOException {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        File tempFile = tempDir.resolve("zero.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(0);
        }
        
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] data = handler.readDelimitedFrom(fis);
            Assertions.assertNotNull(data);
            Assertions.assertEquals(0, data.length);
        }
    }

    @Test
    void testMatcherWithNullFile() {
        FileExtensionMatcher matcher = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        Assertions.assertFalse(matcher.matchFile(null));
    }

    @Test
    void testMatcherWithNonExistentFile() {
        FileExtensionMatcher matcher = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);
        File nonExistent = new File("nonexistent.protobuf");
        Assertions.assertFalse(matcher.matchFile(nonExistent));
    }

    @Test
    void testMatcherWithMultipleExtensions() {
        FileExtensionMatcher matcher = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF, FileExtensionMatcher.FILE_JSON);
        
        File protoFile = mock(File.class);
        doReturn(true).when(protoFile).exists();
        doReturn(true).when(protoFile).canRead();
        doReturn("test.protobuf").when(protoFile).getName();
        Assertions.assertTrue(matcher.matchFile(protoFile));
        
        File jsonFile = mock(File.class);
        doReturn(true).when(jsonFile).exists();
        doReturn(true).when(jsonFile).canRead();
        doReturn("test.json").when(jsonFile).getName();
        Assertions.assertTrue(matcher.matchFile(jsonFile));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleWithNullDescriptor() throws IOException {
        DescriptorPool mockPool = mock(DescriptorPool.class);
        when(mockPool.getDescriptor(any(Class.class))).thenReturn(null);
        
        ProtobufStructHandler handler = new ProtobufStructHandler(mockPool);
        
        StructWorker<TestBean> worker = mock(StructWorker.class);
        StructDescriptor descriptor = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getStartOrder()).thenReturn(0);
        when(descriptor.getEndOrder()).thenReturn(0);
        
        File tempFile = tempDir.resolve("test.protobuf").toFile();
        tempFile.createNewFile();
        
        Consumer<TestBean> consumer = mock(Consumer.class);
        
        Assertions.assertThrows(org.struct.exception.StructTransformException.class, () -> {
            handler.handle(worker, TestBean.class, consumer, tempFile);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleWithDataAndBreakOnNullData() throws IOException {
        DescriptorPool mockPool = mock(DescriptorPool.class);
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        when(mockPool.getDescriptor(any(Class.class))).thenReturn(mockDescriptor);
        
        ProtobufStructHandler handler = new ProtobufStructHandler(mockPool);
        
        File tempFile = tempDir.resolve("test.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(5);
            fos.write(1);
        }
        
        StructWorker<TestBean> worker = mock(StructWorker.class);
        StructDescriptor descriptor = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getStartOrder()).thenReturn(0);
        when(descriptor.getEndOrder()).thenReturn(0);
        
        ArrayList<TestBean> results = new ArrayList<>();
        Consumer<TestBean> consumer = results::add;
        
        handler.handle(worker, TestBean.class, consumer, tempFile);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleWithIOException() throws IOException {
        DescriptorPool mockPool = mock(DescriptorPool.class);
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        when(mockPool.getDescriptor(any(Class.class))).thenReturn(mockDescriptor);
        
        ProtobufStructHandler handler = new ProtobufStructHandler(mockPool);
        
        File tempFile = tempDir.resolve("test3.protobuf").toFile();
        
        StructWorker<TestBean> worker = mock(StructWorker.class);
        StructDescriptor descriptor = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getStartOrder()).thenReturn(0);
        when(descriptor.getEndOrder()).thenReturn(0);
        
        ArrayList<TestBean> results = new ArrayList<>();
        Consumer<TestBean> consumer = results::add;
        
        Assertions.assertThrows(org.struct.exception.StructTransformException.class, () -> {
            handler.handle(worker, TestBean.class, consumer, tempFile);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertToStructImpl() throws Exception {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        Descriptors.FieldDescriptor idField = mock(Descriptors.FieldDescriptor.class);
        Descriptors.FieldDescriptor nameField = mock(Descriptors.FieldDescriptor.class);
        
        when(mockDescriptor.getFields()).thenReturn(List.of(idField, nameField));
        when(idField.getName()).thenReturn("id");
        when(nameField.getName()).thenReturn("name");
        when(idField.isRepeated()).thenReturn(false);
        when(nameField.isRepeated()).thenReturn(false);
        
        DynamicMessage mockMessage = mock(DynamicMessage.class);
        when(mockMessage.getDescriptorForType()).thenReturn(mockDescriptor);
        when(mockMessage.getField(idField)).thenReturn(1);
        when(mockMessage.getField(nameField)).thenReturn("Test");
        
        StructImpl struct = handler.convertToStructImpl(mockMessage);
        Assertions.assertNotNull(struct);
        Assertions.assertEquals("1", struct.get("id"));
        Assertions.assertEquals("Test", struct.get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertToStructImplWithNullValue() throws Exception {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        Descriptors.FieldDescriptor idField = mock(Descriptors.FieldDescriptor.class);
        
        when(mockDescriptor.getFields()).thenReturn(List.of(idField));
        when(idField.getName()).thenReturn("id");
        when(idField.isRepeated()).thenReturn(false);
        
        DynamicMessage mockMessage = mock(DynamicMessage.class);
        when(mockMessage.getDescriptorForType()).thenReturn(mockDescriptor);
        when(mockMessage.getField(idField)).thenReturn(null);
        
        StructImpl struct = handler.convertToStructImpl(mockMessage);
        Assertions.assertNotNull(struct);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertToStructImplWithRepeated() throws Exception {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        Descriptors.FieldDescriptor tagsField = mock(Descriptors.FieldDescriptor.class);
        
        when(mockDescriptor.getFields()).thenReturn(List.of(tagsField));
        when(tagsField.getName()).thenReturn("tags");
        when(tagsField.isRepeated()).thenReturn(true);
        
        DynamicMessage mockMessage = mock(DynamicMessage.class);
        when(mockMessage.getDescriptorForType()).thenReturn(mockDescriptor);
        when(mockMessage.getField(tagsField)).thenReturn(List.of("a", "b", "c"));
        
        StructImpl struct = handler.convertToStructImpl(mockMessage);
        Assertions.assertNotNull(struct);
        Assertions.assertEquals("a,b,c", struct.get("tags"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertToStructImplWithRepeatedEmptyList() throws Exception {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        Descriptors.FieldDescriptor tagsField = mock(Descriptors.FieldDescriptor.class);
        
        when(mockDescriptor.getFields()).thenReturn(List.of(tagsField));
        when(tagsField.getName()).thenReturn("tags");
        when(tagsField.isRepeated()).thenReturn(true);
        
        DynamicMessage mockMessage = mock(DynamicMessage.class);
        when(mockMessage.getDescriptorForType()).thenReturn(mockDescriptor);
        when(mockMessage.getField(tagsField)).thenReturn(List.of());
        
        StructImpl struct = handler.convertToStructImpl(mockMessage);
        Assertions.assertNotNull(struct);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleWithParseError() throws IOException {
        DescriptorPool mockPool = mock(DescriptorPool.class);
        Descriptors.Descriptor mockDescriptor = mock(Descriptors.Descriptor.class);
        when(mockPool.getDescriptor(any(Class.class))).thenReturn(mockDescriptor);
        
        ProtobufStructHandler handler = new ProtobufStructHandler(mockPool);
        
        File tempFile = tempDir.resolve("invalid.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(10);
        }
        
        StructWorker<TestBean> worker = mock(StructWorker.class);
        StructDescriptor descriptor = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getStartOrder()).thenReturn(0);
        when(descriptor.getEndOrder()).thenReturn(0);
        
        ArrayList<TestBean> results = new ArrayList<>();
        Consumer<TestBean> consumer = results::add;
        
        handler.handle(worker, TestBean.class, consumer, tempFile);
    }

    @Test
    void testFullFlowWithRealProtobufData() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_real.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(0);
        when(sd.getEndOrder()).thenReturn(0);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        when(worker.createInstance(any(StructImpl.class))).thenAnswer(invocation -> {
            StructImpl struct = invocation.getArgument(0);
            PersonBean bean = new PersonBean();
            bean.id = Integer.parseInt((String) struct.get("id"));
            bean.name = (String) struct.get("name");
            bean.email = (String) struct.get("email");
            bean.age = Integer.parseInt((String) struct.get("age"));
            return Optional.of(bean);
        });
        
        handler.handle(worker, PersonBean.class, results::add, tempFile);
        
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(1, results.get(0).id);
        Assertions.assertEquals("Alice", results.get(0).name);
        Assertions.assertEquals("alice@test.com", results.get(0).email);
        Assertions.assertEquals(25, results.get(0).age);
    }

    @Test
    void testFullFlowWithStartOrder() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_start.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_FIVE_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(2);
        when(sd.getEndOrder()).thenReturn(0);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        when(worker.createInstance(any(StructImpl.class))).thenAnswer(invocation -> {
            StructImpl struct = invocation.getArgument(0);
            PersonBean bean = new PersonBean();
            bean.id = Integer.parseInt((String) struct.get("id"));
            bean.name = (String) struct.get("name");
            bean.email = (String) struct.get("email");
            bean.age = Integer.parseInt((String) struct.get("age"));
            return Optional.of(bean);
        });
        
        handler.handle(worker, PersonBean.class, results::add, tempFile);
        
        Assertions.assertEquals(4, results.size());
        Assertions.assertEquals(2, results.get(0).id);
    }

    @Test
    void testFullFlowWithEndOrder() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_end.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_FIVE_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(0);
        when(sd.getEndOrder()).thenReturn(3);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        when(worker.createInstance(any(StructImpl.class))).thenAnswer(invocation -> {
            StructImpl struct = invocation.getArgument(0);
            PersonBean bean = new PersonBean();
            bean.id = Integer.parseInt((String) struct.get("id"));
            bean.name = (String) struct.get("name");
            bean.email = (String) struct.get("email");
            bean.age = Integer.parseInt((String) struct.get("age"));
            return Optional.of(bean);
        });
        
        handler.handle(worker, PersonBean.class, results::add, tempFile);
        
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(3, results.get(2).id);
    }

    @Test
    void testFullFlowWithStartAndEndOrder() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_range.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_FIVE_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(2);
        when(sd.getEndOrder()).thenReturn(3);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        when(worker.createInstance(any(StructImpl.class))).thenAnswer(invocation -> {
            StructImpl struct = invocation.getArgument(0);
            PersonBean bean = new PersonBean();
            bean.id = Integer.parseInt((String) struct.get("id"));
            bean.name = (String) struct.get("name");
            bean.email = (String) struct.get("email");
            bean.age = Integer.parseInt((String) struct.get("age"));
            return Optional.of(bean);
        });
        
        handler.handle(worker, PersonBean.class, results::add, tempFile);
        
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(2, results.get(0).id);
        Assertions.assertEquals(3, results.get(1).id);
    }

    @Test
    void testFullFlowWithEmptyDataLength() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_empty.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_EMPTY_LEN_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(0);
        when(sd.getEndOrder()).thenReturn(0);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        Consumer<PersonBean> consumer = results::add;
        
        handler.handle(worker, PersonBean.class, consumer, tempFile);
        
        Assertions.assertEquals(0, results.size());
    }

    @Test
    void testFullFlowWithInvalidData() throws Exception {
        PersonDescriptorPool pool = new PersonDescriptorPool();
        ProtobufStructHandler handler = new ProtobufStructHandler(pool);
        
        File tempFile = tempDir.resolve("person_invalid.protobuf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(PERSON_INVALID_DATA);
        }
        
        StructWorker<PersonBean> worker = mock(StructWorker.class);
        StructDescriptor sd = mock(StructDescriptor.class);
        when(worker.getDescriptor()).thenReturn(sd);
        when(sd.getStartOrder()).thenReturn(0);
        when(sd.getEndOrder()).thenReturn(0);
        
        ArrayList<PersonBean> results = new ArrayList<>();
        Consumer<PersonBean> consumer = results::add;
        
        handler.handle(worker, PersonBean.class, consumer, tempFile);
        
        Assertions.assertEquals(0, results.size());
    }

    static class TestBean {
        public int id;
        public String name;
    }

    @StructSheet(fileName = "person.protobuf")
    static class PersonBean {
        @StructField(name = "id")
        public int id;
        @StructField(name = "name")
        public String name;
        @StructField(name = "email")
        public String email;
        @StructField(name = "age")
        public int age;
    }

    static class PersonDescriptorPool extends DescriptorPool {
        @Override
        public Descriptors.Descriptor getDescriptor(Class<?> clazz) {
            return PersonProto.Person.getDescriptor();
        }
    }
}
