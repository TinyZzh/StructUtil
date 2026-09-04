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

import com.google.protobuf.Descriptors;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 所有格式共用的同一个 bean —— 这是对比能成立的前提。
 *
 * <p>csv / json / xlsx / xls / xml / protobuf 六种格式全部反序列化成这一个类，
 * 因此字段数量、字段类型、转换器链路、反射建实例的成本都完全相同，
 * 唯一的变量只有数据格式本身。</p>
 *
 * <p>字段设计上有几个刻意的取舍：</p>
 * <ul>
 *   <li>没有任何字段取 java / proto3 的默认值（0、空串、空数组）。proto3 在
 *       序列化时会省略默认值，若数据里出现 0，{@code getAllFields()} 里就取不到该字段，
 *       protobuf 的体积会被"人为"压小，比较就失真了。</li>
 *   <li>{@code rewards} 声明为 {@code int[]}，用来触发整条链路上最贵的一段：
 *       文本格式走 {@code ArrayConverter} 按 {@code |} 切分字符串，
 *       json / protobuf 走集合拷贝。</li>
 *   <li>{@code expireAt} 刻意控制在 10 位以内。POI 的格式化器会把更大的数字
 *       渲染成科学计数法，那样数据无法原样往返。</li>
 *   <li>JAXB 注解只有 {@code XmlStructHandler} 需要，对其余 handler 完全无副作用。</li>
 * </ul>
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class BenchItem {

    /**
     * 写数据文件时统一的列顺序 / 字段顺序。
     */
    public static final String[] FIELDS = {
            "id", "name", "quality", "price", "expireAt", "stackLimit", "rewards", "desc"
    };

    public int id;
    public String name;
    public int quality;
    public double price;
    public long expireAt;
    public int stackLimit;
    public int[] rewards;
    public String desc;

    /**
     * 供 {@code ProtobufStructHandler#findDescriptor} 反射查找运行时 message descriptor
     * （本模块没有 protoc 步骤，descriptor 是程序化构建的）。
     */
    public static Descriptors.Descriptor getDescriptor() {
        return BenchItemProto.DESCRIPTOR;
    }
}
