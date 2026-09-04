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

package org.struct.examples.benchmark;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

/**
 * {@link BenchItem} 对应的 protobuf message descriptor，全部程序化构建，
 * 因此本模块不需要 {@code protoc}（与 {@code ProtobufStructHandlerTest} 同一手法）。
 *
 * <pre>
 * message BenchItem {
 *   int32  id         = 1;
 *   string name       = 2;
 *   int32  quality    = 3;
 *   double price      = 4;
 *   int64  expireAt   = 5;
 *   int32  stackLimit = 6;
 *   repeated int32 rewards = 7;
 *   string desc       = 8;
 * }
 * </pre>
 *
 * <p><b>读数字时务必注意：</b>解析走的是 {@code DynamicMessage}，一种由 descriptor
 * 驱动的通用解码器。真实 {@code protoc} 生成的类是手写的逐字段解码，明显更快，
 * 所以本基准给出的 protobuf 成绩只是<b>下界</b>。</p>
 */
public final class BenchItemProto {

    /**
     * 同时充当 excel 的 sheet 名和 {@code StructDescriptor#sheetName} ——
     * 因为 {@code ProtobufStructHandler} 正是从 sheetName 解析 message 名的。
     */
    public static final String MESSAGE_NAME = "BenchItem";

    public static final Descriptors.Descriptor DESCRIPTOR;

    static {
        try {
            Descriptors.FileDescriptor file = Descriptors.FileDescriptor.buildFrom(
                    DescriptorProtos.FileDescriptorProto.newBuilder()
                            .setName("bench_item.proto")
                            .setPackage("struct.bench")
                            .setSyntax("proto3")
                            .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                    .setName(MESSAGE_NAME)
                                    .addField(field("id", 1, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("name", 2, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("quality", 3, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("price", 4, DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("expireAt", 5, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("stackLimit", 6, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .addField(field("rewards", 7, DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED))
                                    .addField(field("desc", 8, DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                                            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL))
                                    .build())
                            .build(),
                    new Descriptors.FileDescriptor[0]);
            DESCRIPTOR = file.findMessageTypeByName(MESSAGE_NAME);
        } catch (Descriptors.DescriptorValidationException e) {
            throw new ExceptionInInitializerError(e);
        }
        if (DESCRIPTOR == null) {
            throw new ExceptionInInitializerError("message not found: " + MESSAGE_NAME);
        }
    }

    private BenchItemProto() {
        //  no-op
    }

    private static DescriptorProtos.FieldDescriptorProto field(String name, int number,
                                                              DescriptorProtos.FieldDescriptorProto.Type type,
                                                              DescriptorProtos.FieldDescriptorProto.Label label) {
        return DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(name)
                .setNumber(number)
                .setType(type)
                .setLabel(label)
                .build();
    }
}
