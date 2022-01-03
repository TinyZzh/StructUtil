/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.core.StructDescriptor;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Use JAXB handle xml file.
 */
@SPI(name = "xml")
public class XmlStructHandler implements StructHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlStructHandler.class);
    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_XML);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        int i = 0;
        try {
            JAXBContext context = JAXBContext.newInstance(JaxbCollectionWrapper.class, clzOfStruct);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(file);
            JaxbCollectionWrapper<T> wrapper = (JaxbCollectionWrapper<T>) unmarshaller.unmarshal(source, JaxbCollectionWrapper.class).getValue();
            if (wrapper != null) {
                for (T objInstance : wrapper.getRoot()) {
                    int line = ++i;
                    if (descriptor.getStartOrder() > 0 && line < descriptor.getStartOrder()) {
                        //  do nothing
                    } else if (descriptor.getEndOrder() > 0 && line > descriptor.getEndOrder()) {
                        //  end
                        break;
                    } else {
                        worker.createInstance(objInstance).ifPresent(cellHandler);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("xml deserialize failure. struct:{}, file:{} line:{}", clzOfStruct, file.getName(), i, e);
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    /**
     * JXAB bean's wrapper.
     *
     * @param <T>
     * @author TinyZ.
     */
    public static class JaxbCollectionWrapper<T> {

        private Collection<T> root;

        /**
         * @return jxab bean's collection.
         */
        @XmlAnyElement(lax = true)
        public Collection<T> getRoot() {
            return root;
        }

        public void setRoot(Collection<T> root) {
            this.root = root;
        }
    }
}
