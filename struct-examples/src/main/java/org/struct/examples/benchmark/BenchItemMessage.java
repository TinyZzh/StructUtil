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

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 一个<b>手写</b>的 protobuf Message 类，用来模拟 {@code protoc} 生成的类。
 *
 * <p>为什么需要它：{@code struct-examples} 没有 protoc 步骤，而默认的
 * {@code ProtobufStructHandler} 路径 B 走的是 {@code DynamicMessage} —— 一种由
 * descriptor 驱动的通用解码器，比生成类慢一个量级（见
 * {@code ProtobufDiagnosticBenchmark}）。如果只测 DynamicMessage，
 * "protobuf 二进制有多快"这个问题就答偏了：慢的是解码器，不是格式。</p>
 *
 * <p>这个类提供：</p>
 * <ul>
 *   <li>静态 {@code parser()}，内部是<b>逐字段手写解码</b>（与 protoc 输出等价），
 *       而不是通用的 descriptor 查表；</li>
 *   <li>字段直接落在对象上，因此走 {@code ProtobufStructHandler} 的路径 A：
 *       解析结果<b>就是</b> bean 本身，不经过 {@code StructImpl} 中间层，
 *       也不再需要反射设置字段。</li>
 * </ul>
 *
 * <p>它衡量的不是"当前开箱体验"，而是"protobuf 格式在本框架里能到达的上限"，
 * 用来和 DynamicMessage 路径拉开参照。</p>
 */
public final class BenchItemMessage extends AbstractMessage implements Message {

    private int id;
    private String name;
    private int quality;
    private double price;
    private long expireAt;
    private int stackLimit;
    private final List<Integer> rewards = new ArrayList<>(3);
    private String desc;

    public BenchItemMessage() {
        //  no-op
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getQuality() {
        return this.quality;
    }

    public double getPrice() {
        return this.price;
    }

    public long getExpireAt() {
        return this.expireAt;
    }

    public int getStackLimit() {
        return this.stackLimit;
    }

    public List<Integer> getRewards() {
        return this.rewards;
    }

    public String getDesc() {
        return this.desc;
    }

    /**
     * 与 protoc 生成类同形的静态 parser。
     *
     * <p>tag 计算：{@code fieldNumber << 3 | wireType}。
     * {@code rewards}（字段 7，repeated int32）按 protobuf 惯例以 <b>packed</b> 编码写入
     * （wire type 2，tag 58），这里同时也兼容非 packed 形式（tag 56）。</p>
     */
    public static Parser<BenchItemMessage> parser() {
        return new AbstractParser<>() {
            @Override
            public BenchItemMessage parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                    throws InvalidProtocolBufferException {
                BenchItemMessage msg = new BenchItemMessage();
                try {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            break;
                        }
                        switch (tag) {
                            case 8 -> msg.id = input.readInt32();
                            case 18 -> msg.name = input.readString();
                            case 24 -> msg.quality = input.readInt32();
                            case 33 -> msg.price = input.readDouble();
                            case 40 -> msg.expireAt = input.readInt64();
                            case 48 -> msg.stackLimit = input.readInt32();
                            case 56 -> msg.rewards.add(input.readInt32());
                            case 58 -> {
                                int length = input.readRawVarint32();
                                int limit = input.pushLimit(length);
                                while (input.getBytesUntilLimit() > 0) {
                                    msg.rewards.add(input.readInt32());
                                }
                                input.popLimit(limit);
                            }
                            case 66 -> msg.desc = input.readString();
                            default -> {
                                if (!input.skipField(tag)) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e;
                } catch (IOException e) {
                    throw new InvalidProtocolBufferException(e);
                }
                return msg;
            }
        };
    }

    @Override
    public Parser<BenchItemMessage> getParserForType() {
        return parser();
    }

    @Override
    public Descriptors.Descriptor getDescriptorForType() {
        return BenchItemProto.DESCRIPTOR;
    }

    @Override
    public Message getDefaultInstanceForType() {
        return new BenchItemMessage();
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public Message.Builder newBuilderForType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message.Builder toBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
        throw new UnsupportedOperationException("BenchItemMessage is read-only; it is not built through descriptors.");
    }

    @Override
    public boolean hasField(Descriptors.FieldDescriptor field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getField(Descriptors.FieldDescriptor field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnknownFieldSet getUnknownFields() {
        return UnknownFieldSet.getDefaultInstance();
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
        output.writeInt32(1, this.id);
        output.writeString(2, this.name);
        output.writeInt32(3, this.quality);
        output.writeDouble(4, this.price);
        output.writeInt64(5, this.expireAt);
        output.writeInt32(6, this.stackLimit);
        for (Integer reward : this.rewards) {
            output.writeInt32(7, reward);
        }
        output.writeString(8, this.desc);
    }

    @Override
    public int getSerializedSize() {
        int size = 0;
        size += CodedOutputStream.computeInt32Size(1, this.id);
        size += CodedOutputStream.computeStringSize(2, this.name);
        size += CodedOutputStream.computeInt32Size(3, this.quality);
        size += CodedOutputStream.computeDoubleSize(4, this.price);
        size += CodedOutputStream.computeInt64Size(5, this.expireAt);
        size += CodedOutputStream.computeInt32Size(6, this.stackLimit);
        for (Integer reward : this.rewards) {
            size += CodedOutputStream.computeInt32Size(7, reward);
        }
        size += CodedOutputStream.computeStringSize(8, this.desc);
        return size;
    }
}
