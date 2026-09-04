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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Protobuf struct handler.
 * Supports parsing protobuf binary format data files.
 *
 * <p>Usage example:</p>
 * <pre>
 * &#64;StructSheet(fileName = "data.proto", sheetName = "YourMessageName")
 * public final class MyData {
 *     // fields
 * }
 * </pre>
 *
 * @see "https://developers.google.com/protocol-buffers"
 */
@SPI(name = "protobuf")
public class ProtobufStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufStructHandler.class);

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(
            FileExtensionMatcher.FILE_PROTOBUF,
            ".bin",
            ".pbf"
    );

    /**
     * Cache for protobuf parsers to avoid repeated reflection.
     * <p>
     * NOTE: this handler is a shared SPI singleton, so the cache must be thread safe.
     */
    final Map<String, Parser<?>> parserCache = new ConcurrentHashMap<>();

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        int line = 0;

        // Get the sheetName from descriptor, which should contain the protobuf message name
        String messageName = descriptor.getSheetName();
        if (messageName == null || messageName.isEmpty()) {
            // Use class name as default message name
            messageName = clzOfStruct.getSimpleName();
        }

        try {
            // Get or create parser for the message type. getParser never returns null:
            // a Message class failing to yield a parser throws, and a non-Message class
            // falls back to descriptor lookup (or throws if that also fails).
            Parser<Message> parser = getParser(clzOfStruct, messageName);

            // Read the entire file and parse as a single protobuf message
            // For repeated messages, the file should contain length-delimited messages
            //
            // NOTE: the stream MUST be buffered. parseDelimitedFrom() reads the length
            // varint byte by byte and then the message body in several reads, so on a
            // bare FileInputStream every single message costs a handful of read()
            // syscalls. Wrapping it in a BufferedInputStream measured 4.2x faster on a
            // 10k row file (see ProtobufDiagnosticBenchmark: 33.98 ms -> 8.07 ms).
            try (InputStream fis = new BufferedInputStream(new FileInputStream(file))) {
                while (true) {
                    line++;
                    
                    // Check start order
                    if (descriptor.getStartOrder() > 0 && line < descriptor.getStartOrder()) {
                        // Skip this message
                        try {
                            parser.parseDelimitedFrom(fis);
                        } catch (IOException e) {
                            // End of stream
                            break;
                        }
                        continue;
                    }

                    // Check end order
                    if (descriptor.getEndOrder() > 0 && line > descriptor.getEndOrder()) {
                        break;
                    }

                    // Parse a single length-delimited message
                    Message protobufMsg = parser.parseDelimitedFrom(fis);
                    if (protobufMsg == null) {
                        break;
                    }

                    // The parsed message is already a protobuf Message, so it can be fed
                    // straight into the factory - {@code SingleFieldDescriptor} reads each
                    // field directly from the message by name (see
                    // {@code SingleFieldDescriptor#fieldValueFromMessage}), with no
                    // intermediate {@code StructImpl} row object. This applies to BOTH
                    // paths:
                    //  - Path A: a protoc-generated Message subclass that IS the target
                    //    bean. Emit it as-is (a Message has no no-arg ctor, so the factory
                    //    must not try to rebuild it).
                    //  - Path B: a DynamicMessage for a plain POJO/record. The factory
                    //    builds the bean from the message the same way it would from a
                    //    StructImpl, only faster because the row object is gone.
                    if (clzOfStruct.isInstance(protobufMsg)) {
                        @SuppressWarnings("unchecked")
                        T instance = (T) protobufMsg;
                        cellHandler.accept(instance);
                    } else {
                        worker.createInstance(protobufMsg).ifPresent(cellHandler);
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            LOGGER.warn("protobuf deserialize failure. struct:{}, file:{}, line:{}", 
                    clzOfStruct, file.getName(), line, e);
            throw new StructTransformException("Failed to parse protobuf data: " + e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.warn("protobuf io failure. struct:{}, file:{}, line:{}", 
                    clzOfStruct, file.getName(), line, e);
            throw new StructTransformException("Failed to read protobuf file: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.warn("protobuf transformation failure. struct:{}, file:{}, line:{}", 
                    clzOfStruct, file.getName(), line, e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    /**
     * Get or create a parser for the given message class.
     * <p>
     * Explicitly splits into two mutually exclusive paths (no silent downgrade):
     * <ul>
     *   <li>path A: {@code clzOfStruct} is a protoc-generated {@link Message} subclass -
     *       obtain its parser from a default instance; a failure here throws immediately.</li>
     *   <li>path B: a plain POJO / record - build a {@link DynamicMessage} parser from a
     *       runtime-exposed {@link Descriptors.Descriptor}; missing descriptor throws.</li>
     * </ul>
     * The previous "try Message first, silently fall back to descriptor" order masked
     * real parse failures as wrong-but-silent data. This method never returns null.
     */
    @SuppressWarnings("unchecked")
    Parser<Message> getParser(Class<?> clzOfStruct, String messageName) {
        String cacheKey = messageName + ":" + clzOfStruct.getName();

        Parser<Message> cached = (Parser<Message>) parserCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Parser<Message> parser;
        if (Message.class.isAssignableFrom(clzOfStruct)) {
            //  Path A: a protoc-generated Message subclass. The parsed message IS the
            //  target bean - no intermediate StructImpl.
            //  The generated static parser() is itself just a wrapper around
            //  getDefaultInstance().getParserForType(), so we skip that layer and read
            //  the parser straight off a default instance. Generated classes ship a
            //  public no-arg constructor, so a single reflective newInstance() is all
            //  we need - no getMethod("parser")/invoke two-hop reflection.
            try {
                Message defaultInstance = (Message) clzOfStruct.getConstructor().newInstance();
                parser = (Parser<Message>) defaultInstance.getParserForType();
                if (parser == null) {
                    throw new StructTransformException(
                            "Protobuf getParserForType() returned null for Message class: " + clzOfStruct.getName());
                }
            } catch (ReflectiveOperationException e) {
                throw new StructTransformException(
                        "Failed to instantiate protobuf Message class: " + clzOfStruct.getName(), e);
            } catch (RuntimeException e) {
                //  e.g. a Message subclass whose getParserForType() throws - fail loudly.
                throw new StructTransformException(
                        "Failed to obtain parser for protobuf Message class: " + clzOfStruct.getName(), e);
            }
        } else {
            //  Path B: a plain POJO / record. Build a DynamicMessage parser from a
            //  runtime-exposed Descriptor; a missing descriptor throws (never silent).
            Descriptors.Descriptor descriptor = findDescriptor(clzOfStruct, messageName);
            if (descriptor == null) {
                throw new StructTransformException(
                        "No protobuf descriptor available for " + clzOfStruct.getName()
                                + ". A non-Message struct must expose a static getDescriptor()/descriptor().");
            }
            parser = (Parser<Message>) (Parser<?>) DynamicMessage.getDefaultInstance(descriptor).getParserForType();
        }

        //  NOTE: never cache a null result. A negative entry made the lookup
        //  return null forever (containsKey -> true, get -> null), so a transient
        //  failure permanently disabled the message type.
        parserCache.put(cacheKey, parser);
        return parser;
    }

    /**
     * Try to find the protobuf descriptor for the given class.
     */
    Descriptors.Descriptor findDescriptor(Class<?> clzOfStruct, String messageName) {
        //  the generated class exposes a static getDescriptor() (descriptor() in older
        //  protoc versions) returning this message's descriptor.
        Descriptors.Descriptor descriptor = null;
        for (String methodName : new String[]{"getDescriptor", "descriptor"}) {
            try {
                Method method = clzOfStruct.getMethod(methodName);
                descriptor = (Descriptors.Descriptor) method.invoke(null);
                if (descriptor != null) {
                    break;
                }
            } catch (Exception e) {
                //  ignore and try the next method name.
            }
        }
        if (descriptor == null) {
            return null;
        }
        //  @StructSheet#sheetName may point at another message declared in the same
        //  .proto file. Resolve it by name - previously messageName was ignored, so
        //  specifying a message name had no effect at all.
        if (messageName != null
                && !messageName.isEmpty()
                && !messageName.equals(descriptor.getName())) {
            Descriptors.Descriptor byName = descriptor.getFile().findMessageTypeByName(messageName);
            if (byName != null) {
                return byName;
            }
            LOGGER.warn("message:{} not found in file:{}, fallback to:{}",
                    messageName, descriptor.getFile().getName(), descriptor.getName());
        }
        return descriptor;
    }
}
