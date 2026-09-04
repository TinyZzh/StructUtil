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

package org.struct.examples.protobuf;

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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 示例目标 bean：一份与 {@code examples/item.proto} 等价的手写 protobuf Message 类，
 * 用来代表"用 protoc 生成的 Item 类"的形态（无需本地 protoc 依赖即可演示框架路径 A）。
 *
 * <p><b>注意本类刻意不加 {@link org.struct.annotation.StructSheet}</b>——真实 protoc 生成类
 * 每次重新生成都会被覆盖，无法注解。框架对这类零注解的 {@code Message} 子类提供专用入口
 * {@code new StructWorker<>(workspace, ItemProto.class, "item.bin")}：文件名显式传入，
 * message 名默认取类的简单名（即本例的 {@code ItemProto}）。
 *
 * <p>路径 A：{@code ProtobufStructHandler} 复用其静态 {@code parser()} 把二进制解析成
 * 该类的实例，不经过中间层 StructImpl。框架的 required/引用/聚合等高级特性对 Message 不适用。
 */
public final class ItemProto extends AbstractMessage implements Message {

    /** Item message 的运行时描述符，程序化构建（与 item.proto 定义一致）。 */
    static final Descriptors.Descriptor ITEM_DESCRIPTOR;

    static {
        try {
            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(
                    DescriptorProtos.FileDescriptorProto.newBuilder()
                            .setName("item.proto")
                            .setPackage("org.struct.examples.protobuf")
                            .setSyntax("proto3")
                            .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                    .setName("ItemProto")
                                    .addField(newField("id", 1,
                                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                    .addField(newField("name", 2,
                                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                    .addField(newField("price", 3,
                                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL, null))
                                    .addField(newField("tags", 4,
                                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED, null))
                                    .build())
                            .build(),
                    new Descriptors.FileDescriptor[0]);
            ITEM_DESCRIPTOR = fileDescriptor.findMessageTypeByName("ItemProto");
        } catch (Descriptors.DescriptorValidationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static DescriptorProtos.FieldDescriptorProto newField(
            String name, int number, DescriptorProtos.FieldDescriptorProto.Type type,
            DescriptorProtos.FieldDescriptorProto.Label label, String typeName) {
        DescriptorProtos.FieldDescriptorProto.Builder b = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(name).setNumber(number).setType(type).setLabel(label);
        if (typeName != null) {
            b.setTypeName(typeName);
        }
        return b.build();
    }

    private final DynamicMessage inner;

    private ItemProto(DynamicMessage inner) {
        this.inner = inner;
    }

    /** protoc 生成类都带一个静态 parser()，框架路径 A 正是反射调用它。 */
    public static Parser<ItemProto> parser() {
        return new AbstractParser<>() {
            @Override
            public ItemProto parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                    throws InvalidProtocolBufferException {
                try {
                    DynamicMessage dm = DynamicMessage.parseFrom(ITEM_DESCRIPTOR, input);
                    return new ItemProto(dm);
                } catch (IOException e) {
                    throw new InvalidProtocolBufferException(e);
                }
            }
        };
    }

    @Override
    public Parser<ItemProto> getParserForType() {
        return parser();
    }

    @Override
    public Descriptors.Descriptor getDescriptorForType() {
        return ITEM_DESCRIPTOR;
    }

    @Override
    public Message getDefaultInstanceForType() {
        return new ItemProto(DynamicMessage.getDefaultInstance(ITEM_DESCRIPTOR));
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

    // ===== 便于演示读取的便捷访问器（protoc 生成类会有同名 getXxx） =====

    public int getId() {
        return (Integer) getField(ITEM_DESCRIPTOR.findFieldByName("id"));
    }

    public String getName() {
        return (String) getField(ITEM_DESCRIPTOR.findFieldByName("name"));
    }

    public double getPrice() {
        return (Double) getField(ITEM_DESCRIPTOR.findFieldByName("price"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getTags() {
        return (List<String>) getField(ITEM_DESCRIPTOR.findFieldByName("tags"));
    }
}
