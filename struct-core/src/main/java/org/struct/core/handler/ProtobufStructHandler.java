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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

@SPI(name = "protobuf", order = 0)
public class ProtobufStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufStructHandler.class);
    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_PROTOBUF);

    private final DescriptorPool descriptorPool;

    public ProtobufStructHandler() {
        this(new DescriptorPool());
    }

    public ProtobufStructHandler(DescriptorPool descriptorPool) {
        this.descriptorPool = descriptorPool;
    }

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        int line = 0;
        try {
            Descriptors.Descriptor messageDescriptor = descriptorPool.getDescriptor(clzOfStruct);
            if (messageDescriptor == null) {
                throw new StructTransformException("Failed to get protobuf descriptor for class: " + clzOfStruct.getName());
            }

            int startOrder = descriptor.getStartOrder();
            int endOrder = descriptor.getEndOrder();

            try (FileInputStream fis = new FileInputStream(file)) {
                while (true) {
                    try {
                        byte[] data = readDelimitedFrom(fis);
                        if (data == null) {
                            break;
                        }
                        if (data.length == 0) {
                            break;
                        }
                        line++;
                        if (startOrder > 0) {
                            if (line < startOrder) {
                                continue;
                            }
                        }
                        if (endOrder > 0) {
                            if (line > endOrder) {
                                break;
                            }
                        }

                        DynamicMessage message = DynamicMessage.parseFrom(messageDescriptor, data);
                        StructImpl struct = convertToStructImpl(message);
                        worker.createInstance(struct).ifPresent(cellHandler);
                    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                        LOGGER.warn("protobuf parse failure. struct:{}, file:{}, line:{}", clzOfStruct, file.getName(), line, e);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("protobuf deserialize failure. struct:{}, file:{}, line:{}", clzOfStruct, file.getName(), line, e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    public byte[] readDelimitedFrom(FileInputStream fis) throws IOException {
        int length = 0;
        int shift = 0;
        while (true) {
            int b = fis.read();
            if (b == -1) {
                return null;
            }
            length |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }

        byte[] data = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            int bytesRead = fis.read(data, totalRead, length - totalRead);
            if (bytesRead == -1) {
                break;
            }
            totalRead += bytesRead;
        }
        if (totalRead < length) {
            return null;
        }
        return data;
    }

    public StructImpl convertToStructImpl(DynamicMessage message) {
        StructImpl struct = new StructImpl();
        for (Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
            Object value = message.getField(field);
            if (value == null) {
                continue;
            }
            if (field.isRepeated()) {
                StringBuilder sb = new StringBuilder();
                for (Object item : (Iterable<?>) value) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(item.toString());
                }
                struct.add(field.getName(), sb.toString());
            } else {
                struct.add(field.getName(), value.toString());
            }
        }
        return struct;
    }

    public static class DescriptorPool {
        private Descriptors.Descriptor cachedDescriptor;

        public Descriptors.Descriptor getDescriptor(Class<?> clazz) {
            if (cachedDescriptor != null) {
                return cachedDescriptor;
            }
            try {
                Class<?> parserClass = Class.forName(clazz.getName() + "OrBuilder");
                java.lang.reflect.Method method = parserClass.getMethod("getDescriptor");
                cachedDescriptor = (Descriptors.Descriptor) method.invoke(null);
                return cachedDescriptor;
            } catch (Exception e) {
                LOGGER.warn("Failed to get descriptor from OrBuilder for class: {}", clazz.getName());
            }
            return null;
        }

        public void clearCache() {
            this.cachedDescriptor = null;
        }
    }
}
