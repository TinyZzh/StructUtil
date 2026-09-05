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

import com.google.protobuf.DynamicMessage;
import org.struct.core.StructWorker;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Protobuf 路径 A 演示：pb 二进制文件 → protoc 生成的 Message 子类（这里是手写等价物
 * {@link ItemProto}），不经过中间层 StructImpl。
 *
 * <p>运行前本示例会先写一份 {@code item.bin}（length-delimited 的多条 Item 消息），
 * 再用 {@link StructWorker} 读回，证明"有 .pb 二进制 + Message bean 即可直接解析"。
 *
 * <p>真实项目里 item.bin 由 protoc 编码产出，本示例为了自包含用 DynamicMessage 现场生成。
 */
public final class ProtobufExample {

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "struct-protobuf-example");
        //noinspection ResultOfMethodCallIgnored
        dir.toFile().mkdirs();
        File bin = dir.resolve("item.bin").toFile();

        writeSampleBin(bin);

        //  路径 A 入口：零注解的 protoc 生成类，显式传入文件名；message 名默认取简单类名(ItemProto)。
        StructWorker<ItemProto> worker =
                new StructWorker<>(dir.toAbsolutePath().toString(), ItemProto.class, "item.bin");
        ArrayList<ItemProto> items = worker.toList(ArrayList::new);

        System.out.println("Loaded " + items.size() + " items from " + bin.getAbsolutePath());
        for (ItemProto it : items) {
            System.out.printf("  id=%d name=%s price=%.2f tags=%s%n",
                    it.getId(), it.getName(), it.getPrice(), it.getTags());
        }
    }

    private static void writeSampleBin(File bin) throws Exception {
        List<DynamicMessage> messages = List.of(
                newItem(1, "potion", 12.5, "buff", "consumable"),
                newItem(2, "sword", 99.0, "weapon"),
                newItem(3, "shield", 75.25, "armor", "rare"));

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(bin)) {
            for (DynamicMessage m : messages) {
                m.writeDelimitedTo(fos);
            }
        }
    }

    private static DynamicMessage newItem(int id, String name, double price, String... tags) {
        DynamicMessage.Builder b = DynamicMessage.newBuilder(ItemProto.ITEM_DESCRIPTOR);
        b.setField(ItemProto.ITEM_DESCRIPTOR.findFieldByName("id"), id);
        b.setField(ItemProto.ITEM_DESCRIPTOR.findFieldByName("name"), name);
        b.setField(ItemProto.ITEM_DESCRIPTOR.findFieldByName("price"), price);
        for (String t : tags) {
            b.addRepeatedField(ItemProto.ITEM_DESCRIPTOR.findFieldByName("tags"), t);
        }
        return b.build();
    }

    private ProtobufExample() {
    }
}
