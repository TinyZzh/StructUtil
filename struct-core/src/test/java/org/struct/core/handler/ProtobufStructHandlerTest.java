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

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.struct.annotation.StructSheet;
import org.struct.core.SingleFieldDescriptor;
import org.struct.core.StructWorker;
import org.struct.exception.StructTransformException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * The protobuf descriptors are built programmatically, so the test does not depend
 * on a generated message class (there is no protoc step in this module).
 */
public class ProtobufStructHandlerTest {

    private static Descriptors.FileDescriptor FILE_DESCRIPTOR;
    private static Descriptors.Descriptor OUTER;
    private static Descriptors.Descriptor INNER;

    @BeforeAll
    public static void init() throws Exception {
        FILE_DESCRIPTOR = Descriptors.FileDescriptor.buildFrom(
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setName("struct_test.proto")
                        .setPackage("struct.test")
                        .setSyntax("proto3")
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("Outer")
                                .addField(newField("id", 1, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .addField(newField("name", 2, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .addField(newField("child", 3, DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, ".struct.test.Inner"))
                                .addField(newField("tags", 4, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED, null))
                                .addField(newField("children", 5, DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED, ".struct.test.Inner"))
                                .build())
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("Inner")
                                .addField(newField("value", 1, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .build())
                        .build(),
                new Descriptors.FileDescriptor[0]);
        OUTER = FILE_DESCRIPTOR.findMessageTypeByName("Outer");
        INNER = FILE_DESCRIPTOR.findMessageTypeByName("Inner");
        Assertions.assertNotNull(OUTER);
        Assertions.assertNotNull(INNER);
    }

    private static DescriptorProtos.FieldDescriptorProto newField(String name, int number,
                                                                 DescriptorProtos.FieldDescriptorProto.Type type,
                                                                 DescriptorProtos.FieldDescriptorProto.Label label,
                                                                 String typeName) {
        DescriptorProtos.FieldDescriptorProto.Builder builder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(name)
                .setNumber(number)
                .setType(type)
                .setLabel(label);
        if (typeName != null) {
            builder.setTypeName(typeName);
        }
        return builder.build();
    }

    /**
     * A generated protobuf class exposes a static {@code getDescriptor()} method.
     */
    public static class OuterHolder {
        public static Descriptors.Descriptor getDescriptor() {
            return OUTER;
        }
    }

    private static DynamicMessage newOuterMessage() {
        DynamicMessage child = DynamicMessage.newBuilder(INNER)
                .setField(INNER.findFieldByName("value"), 42)
                .build();
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(OUTER)
                .setField(OUTER.findFieldByName("id"), 7)
                .setField(OUTER.findFieldByName("name"), "seven")
                .setField(OUTER.findFieldByName("child"), child);
        builder.addRepeatedField(OUTER.findFieldByName("tags"), "alpha");
        builder.addRepeatedField(OUTER.findFieldByName("tags"), "beta");
        builder.addRepeatedField(OUTER.findFieldByName("children"), child);
        builder.addRepeatedField(OUTER.findFieldByName("children"), child);
        return builder.build();
    }

    /**
     * {@code @StructSheet#sheetName} may point at another message declared in the same
     * {@code .proto} file. Before the fix the message name was ignored entirely.
     */
    @Test
    public void testFindDescriptorByMessageName() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        //  explicit message name is resolved
        Assertions.assertEquals(INNER, handler.findDescriptor(OuterHolder.class, "Inner"));
        //  the descriptor's own name resolves to itself
        Assertions.assertEquals(OUTER, handler.findDescriptor(OuterHolder.class, "Outer"));
        //  an unknown name falls back to the class descriptor instead of returning null
        Assertions.assertEquals(OUTER, handler.findDescriptor(OuterHolder.class, "NotExist"));
        //  no descriptor method at all -> null
        Assertions.assertNull(handler.findDescriptor(String.class, "Inner"));
    }

    /**
     * A nested message is read directly as its {@link Message}, not flattened into
     * a {@link org.struct.core.StructImpl}. The factory pulls fields straight from
     * the message, so the intermediate row object is gone.
     */
    @Test
    public void testGetFieldValueFromNestedMessageReturnsMessage() {
        SingleFieldDescriptor fd = new SingleFieldDescriptor();
        fd.setName("child");

        Object child = fd.getFieldValueFrom(newOuterMessage());
        Assertions.assertInstanceOf(Message.class, child);
    }

    /**
     * A repeated scalar field must become a plain {@link List} so that array /
     * collection fields can be filled element-wise.
     */
    @Test
    public void testGetFieldValueFromRepeatedScalarReturnsList() {
        SingleFieldDescriptor fd = new SingleFieldDescriptor();
        fd.setName("tags");

        Object tags = fd.getFieldValueFrom(newOuterMessage());
        Assertions.assertInstanceOf(List.class, tags);
        Assertions.assertEquals(List.of("alpha", "beta"), tags);
    }

    /**
     * A repeated message field must be read as a plain {@link List} of
     * {@link Message} elements (no nested {@code StructImpl}).
     */
    @Test
    public void testGetFieldValueFromRepeatedMessageReturnsListOfMessages() {
        SingleFieldDescriptor fd = new SingleFieldDescriptor();
        fd.setName("children");

        Object children = fd.getFieldValueFrom(newOuterMessage());
        Assertions.assertInstanceOf(List.class, children);
        Assertions.assertEquals(2, ((List<?>) children).size());
        Assertions.assertInstanceOf(Message.class, ((List<?>) children).get(0));
    }

    /**
     * A failed parser lookup must NOT be cached: a negative entry used to make
     * {@code containsKey} return true and {@code get} return null forever,
     * permanently disabling the message type.
     */
    @Test
    public void testNoNegativeParserCacheEntry() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        Assertions.assertTrue(handler.parserCache.isEmpty());
        //  java.lang.String is neither a Message nor exposes getDescriptor()
        Assertions.assertNull(handler.findDescriptor(String.class, "Whatever"));
        Assertions.assertTrue(handler.parserCache.isEmpty(),
                "a failed lookup must not leave a cache entry behind");
    }

    /**
     * The parser cache is shared, because the handler is an SPI singleton.
     */
    @Test
    public void testParserCacheIsThreadSafeMap() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        handler.parserCache.put("k", DynamicMessage.newBuilder(INNER).build().getParserForType());
        Assertions.assertEquals(1, handler.parserCache.size());
        Assertions.assertNotNull(handler.parserCache.get("k"));
    }

    @Test
    public void testMatcher() {
        Assertions.assertNotNull(new ProtobufStructHandler().matcher());
    }

    //  ==================================================================
    //  end to end: real .protobuf binary files loaded through StructWorker
    //  ==================================================================

    private static Descriptors.Descriptor ITEM;

    @BeforeAll
    public static void initItem() throws Exception {
        ITEM = Descriptors.FileDescriptor.buildFrom(
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setName("item_e2e.proto")
                        .setPackage("struct.e2e")
                        .setSyntax("proto3")
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("Item")
                                .addField(newField("id", 1, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .addField(newField("name", 2, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .addField(newField("price", 3, DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                .addField(newField("tags", 4, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                        DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED, null))
                                .build())
                        .build(),
                new Descriptors.FileDescriptor[0]).findMessageTypeByName("Item");
        Assertions.assertNotNull(ITEM);
    }

    /**
     * Write {@code count} length-delimited {@code Item} messages into
     * {@code dir/fileName}, then return the generated file.
     */
    private static File writeItems(Path dir, String fileName, int count) throws IOException {
        File file = dir.resolve(fileName).toFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            for (int i = 1; i <= count; i++) {
                DynamicMessage.Builder builder = DynamicMessage.newBuilder(ITEM)
                        .setField(ITEM.findFieldByName("id"), i)
                        .setField(ITEM.findFieldByName("name"), "item" + i)
                        .setField(ITEM.findFieldByName("price"), i * 1.5D);
                builder.addRepeatedField(ITEM.findFieldByName("tags"), "t" + i);
                builder.build().writeDelimitedTo(out);
            }
        }
        return file;
    }

    @Test
    public void testEndToEnd(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "items.protobuf", 3);
        StructWorker<ItemBean> worker = new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBean.class);
        ArrayList<ItemBean> beans = worker.toList(ArrayList::new);

        Assertions.assertEquals(3, beans.size());
        ItemBean first = beans.get(0);
        Assertions.assertEquals(1, first.id);
        Assertions.assertEquals("item1", first.name);
        Assertions.assertEquals(1.5D, first.price);
        //  repeated field -> String[]
        Assertions.assertArrayEquals(new String[]{"t1"}, first.tags);

        ItemBean last = beans.get(2);
        Assertions.assertEquals(3, last.id);
        Assertions.assertEquals("item3", last.name);
    }

    /**
     * {@code startOrder} skips the leading messages.
     */
    @Test
    public void testEndToEndWithStartOrder(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "items_start.protobuf", 5);
        StructWorker<ItemBeanStart2> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBeanStart2.class);
        ArrayList<ItemBeanStart2> beans = worker.toList(ArrayList::new);

        Assertions.assertEquals(4, beans.size());
        Assertions.assertEquals(2, beans.get(0).id, "the first message must be skipped");
    }

    /**
     * {@code endOrder} truncates the trailing messages.
     */
    @Test
    public void testEndToEndWithEndOrder(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "items_end.protobuf", 5);
        StructWorker<ItemBeanEnd2> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBeanEnd2.class);
        ArrayList<ItemBeanEnd2> beans = worker.toList(ArrayList::new);

        Assertions.assertEquals(2, beans.size());
        Assertions.assertEquals(1, beans.get(0).id);
        Assertions.assertEquals(2, beans.get(1).id);
    }

    /**
     * Without an explicit {@code sheetName} the bean's simple name is used as the
     * message name; it does not match a declared message, so the handler warns and
     * falls back to the descriptor found on the class.
     */
    @Test
    public void testEndToEndWithDefaultMessageName(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "items_default.protobuf", 2);
        StructWorker<ItemBeanDefaultName> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBeanDefaultName.class);
        ArrayList<ItemBeanDefaultName> beans = worker.toList(ArrayList::new);

        Assertions.assertEquals(2, beans.size());
        Assertions.assertEquals(1, beans.get(0).id);
    }

    /**
     * A bean that is neither a protobuf Message nor exposes a descriptor cannot be
     * parsed - it must fail loudly instead of silently loading nothing.
     */
    @Test
    public void testNoParser(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "nodesc.protobuf", 1);
        ProtobufStructHandler handler = new ProtobufStructHandler();
        StructWorker<NoDescriptorBean> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), NoDescriptorBean.class);
        ArrayList<NoDescriptorBean> sink = new ArrayList<>();

        Assertions.assertThrows(StructTransformException.class, () ->
                handler.handle(worker, NoDescriptorBean.class, sink::add, tempDir.resolve("nodesc.protobuf").toFile()));
    }

    /**
     * Malformed content must raise a {@link StructTransformException}
     * (wrapped {@link com.google.protobuf.InvalidProtocolBufferException}).
     */
    @Test
    public void testCorruptedData(@TempDir Path tempDir) throws Exception {
        File bad = tempDir.resolve("bad.protobuf").toFile();
        try (FileOutputStream out = new FileOutputStream(bad)) {
            //  an unterminated varint is not a valid length prefix
            out.write(new byte[]{
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        }
        ProtobufStructHandler handler = new ProtobufStructHandler();
        StructWorker<ItemBean> worker = new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBean.class);
        ArrayList<ItemBean> sink = new ArrayList<>();

        Assertions.assertThrows(StructTransformException.class, () ->
                handler.handle(worker, ItemBean.class, sink::add, bad));
    }

    /**
     * An unreadable "file" (a directory with a matching extension) surfaces as a
     * {@link StructTransformException} wrapping the {@link IOException}.
     */
    @Test
    public void testIoFailure(@TempDir Path tempDir) {
        File dir = tempDir.resolve("adir.protobuf").toFile();
        Assertions.assertTrue(dir.mkdirs());

        ProtobufStructHandler handler = new ProtobufStructHandler();
        StructWorker<ItemBean> worker = new StructWorker<>(tempDir.toAbsolutePath().toString(), ItemBean.class);
        ArrayList<ItemBean> sink = new ArrayList<>();

        Assertions.assertThrows(StructTransformException.class, () ->
                handler.handle(worker, ItemBean.class, sink::add, dir));
    }

    @StructSheet(fileName = "items.protobuf", sheetName = "Item")
    public static class ItemBean {
        public static Descriptors.Descriptor getDescriptor() {
            return ITEM;
        }

        public int id;
        public String name;
        public double price;
        public String[] tags;
    }

    @StructSheet(fileName = "items_start.protobuf", sheetName = "Item", startOrder = 2)
    public static class ItemBeanStart2 {
        public static Descriptors.Descriptor getDescriptor() {
            return ITEM;
        }

        public int id;
        public String name;
    }

    @StructSheet(fileName = "items_end.protobuf", sheetName = "Item", endOrder = 2)
    public static class ItemBeanEnd2 {
        public static Descriptors.Descriptor getDescriptor() {
            return ITEM;
        }

        public int id;
        public String name;
    }

    @StructSheet(fileName = "items_default.protobuf")
    public static class ItemBeanDefaultName {
        public static Descriptors.Descriptor getDescriptor() {
            return ITEM;
        }

        public int id;
    }

    @StructSheet(fileName = "nodesc.protobuf", sheetName = "NoSuchMessage")
    public static class NoDescriptorBean {
        public int id;
    }

    //  ==================================================================
    //  Path A: the target bean IS a protoc-generated protobuf Message.
    //  The parsed message is the bean itself - no intermediate StructImpl.
    //  ==================================================================

    /**
     * {@code Message.class.isAssignableFrom(clz)} routes to {@link #getMessageParser(Class)},
     * never to the descriptor fallback. This is the core of "pb -> Message bean, no StructImpl".
     */
    @Test
    public void testGetParserRoutesMessageToStaticParser() throws Exception {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        //  a Message subclass must go through getMessageParser (static parser()),
        //  not findDescriptor()/DynamicMessage.
        Parser<?> parser = handler.getParser(FakeMessageBean.class, "Fake");
        Assertions.assertNotNull(parser);
        //  the cache key is messageName:className; the entry must exist after a lookup.
        Assertions.assertFalse(handler.parserCache.isEmpty());
    }

    /**
     * A class that claims to be a Message (or is routed as one) but has no usable
     * static {@code parser()} must fail loudly - the previous silent downgrade to
     * DynamicMessage produced wrong-but-empty data.
     */
    @Test
    public void testMessageWithoutParserThrows() {
        ProtobufStructHandler handler = new ProtobufStructHandler();
        //  BrokenMessageBean IS a Message but has no static parser() -> getMessageParser must throw,
        //  proving path A no longer silently downgrades to the descriptor path.
        Assertions.assertThrows(StructTransformException.class,
                () -> handler.getParser(BrokenMessageBean.class, "Any"));
    }

    /**
     * A single (non-repeated) scalar field is read straight from the message via
     * {@link SingleFieldDescriptor#getFieldValueFrom(Object)} - no intermediate
     * {@link StructImpl} is ever produced in path A/path B.
     */
    @Test
    public void testGetFieldValueFromScalarField() {
        SingleFieldDescriptor idFd = new SingleFieldDescriptor();
        idFd.setName("id");
        Assertions.assertEquals(7, idFd.getFieldValueFrom(newOuterMessage()));

        SingleFieldDescriptor nameFd = new SingleFieldDescriptor();
        nameFd.setName("name");
        Assertions.assertEquals("seven", nameFd.getFieldValueFrom(newOuterMessage()));
    }

    /**
     * End-to-end path A via the zero-annotation entry point:
     * {@code new StructWorker<>(workspace, MessageClass, fileName)}. A protoc-generated
     * Message subclass carries no @StructSheet (regenerated on every build), so the
     * fileName is supplied explicitly and the message name defaults to the simple class name.
     */
    @Test
    public void testZeroAnnotationMessageEntryPoint(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "fake.bin", 2);
        //  FakeMessageBean has no @StructSheet - uses the explicit-fileName constructor.
        StructWorker<FakeMessageBean> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), FakeMessageBean.class, "fake.bin");
        ArrayList<FakeMessageBean> beans = worker.toList(ArrayList::new);

        Assertions.assertEquals(2, beans.size());
        Assertions.assertInstanceOf(Message.class, beans.get(0));
        Assertions.assertEquals("item1",
                ((FakeMessageBean) beans.get(0)).<String>getField(ITEM.findFieldByName("name")));
    }

    /**
     * A plain (non-Message) class without @StructSheet must be rejected by the
     * explicit-fileName constructor - it cannot be loaded as a protobuf target.
     */
    @Test
    public void testNonMessageWithoutAnnotationRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new StructWorker<>(".", NotAMessageBean.class, "x.bin"));
    }

    /**
     * A bean that is a Message subclass but fails to produce a parser must fail loudly
     * during handling, not silently load nothing.
     */
    @Test
    public void testHandleMessageWithBrokenParser(@TempDir Path tempDir) throws Exception {
        writeItems(tempDir, "broken.protobuf", 1);
        ProtobufStructHandler handler = new ProtobufStructHandler();
        StructWorker<BrokenMessageBean> worker =
                new StructWorker<>(tempDir.toAbsolutePath().toString(), BrokenMessageBean.class);
        ArrayList<BrokenMessageBean> sink = new ArrayList<>();

        Assertions.assertThrows(StructTransformException.class, () ->
                handler.handle(worker, BrokenMessageBean.class, sink::add,
                        tempDir.resolve("broken.protobuf").toFile()));
    }

    /**
     * Minimal hand-written protobuf Message subclass standing in for a protoc-generated
     * class. It delegates to an inner {@link DynamicMessage} and exposes a static
     * {@code parser()} so the handler takes path A. Intentionally small (id/name only).
     * <p>
     * Note: this stands in for a real protoc class - the real end-to-end Message path is
     * exercised in struct-examples with an actual generated class.
     */
    public static class FakeMessageBean extends AbstractMessage implements Message {
        private final DynamicMessage inner;

        public FakeMessageBean() {
            this.inner = DynamicMessage.getDefaultInstance(ITEM);
        }

        FakeMessageBean(DynamicMessage inner) {
            this.inner = inner;
        }

        public static Parser<FakeMessageBean> parser() {
            return new AbstractParser<>() {
                @Override
                public FakeMessageBean parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                        throws InvalidProtocolBufferException {
                    try {
                        DynamicMessage dm = DynamicMessage.parseFrom(ITEM, input);
                        return new FakeMessageBean(dm);
                    } catch (IOException e) {
                        throw new InvalidProtocolBufferException(e);
                    }
                }
            };
        }

        @Override
        public Parser<FakeMessageBean> getParserForType() {
            return parser();
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return ITEM;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return new FakeMessageBean(DynamicMessage.getDefaultInstance(ITEM));
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public Message.Builder newBuilderForType() {
            return null;
        }

        @Override
        public Message.Builder toBuilder() {
            return null;
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return inner.getAllFields();
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return inner.hasField(field);
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            return inner.getField(field);
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return inner.getRepeatedFieldCount(field);
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return inner.getRepeatedField(field, index);
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return UnknownFieldSet.getDefaultInstance();
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            inner.writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            return inner.getSerializedSize();
        }
    }

    /** A Message subclass whose static parser() is absent - must fail loudly. */
    @StructSheet(fileName = "broken.protobuf", sheetName = "Broken")
    public static class BrokenMessageBean extends AbstractMessage implements Message {
        @Override
        public Parser<BrokenMessageBean> getParserForType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return ITEM;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return new BrokenMessageBean();
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public Message.Builder newBuilderForType() {
            return null;
        }

        @Override
        public Message.Builder toBuilder() {
            return null;
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return Map.of();
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return false;
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            return null;
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return 0;
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return null;
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return UnknownFieldSet.getDefaultInstance();
        }

        @Override
        public void writeTo(CodedOutputStream output) {
        }

        @Override
        public int getSerializedSize() {
            return 0;
        }
    }

    /** A plain (non-Message) class - sanity anchor for the dynamic-message path. */
    public static class NotAMessageBean {
        public int id;
    }
}
