package org.struct.core.handler;

import org.struct.core.StructWorker;
import org.struct.core.bean.FileExtensionMatcher;
import org.struct.core.bean.WorkerMatcher;
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

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_XML);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> structHandler, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(JaxbCollectionWrapper.class, clzOfStruct);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(file);
            JaxbCollectionWrapper<T> wrapper = (JaxbCollectionWrapper<T>) unmarshaller.unmarshal(source, JaxbCollectionWrapper.class).getValue();
            if (wrapper != null) {
                for (T objInstance : wrapper.getRoot()) {
                    worker.afterObjectSetCompleted(objInstance);
                    structHandler.accept(objInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
