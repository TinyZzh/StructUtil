package org.struct.core.worker;

import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Use JAXB handle xml file.
 *
 * @param <T>
 */
public class XmlFileWorker<T> extends StructWorker<T> {

    public XmlFileWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        super(rootPath, clzOfBean, refFieldValueMap);
    }

    @Override
    protected void onLoadStructSheetImpl(Consumer<T> cellHandler, StructSheet annotation, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(JaxbCollectionWrapper.class, this.clzOfBean);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(file);
            JaxbCollectionWrapper<T> wrapper = (JaxbCollectionWrapper<T>) unmarshaller.unmarshal(source, JaxbCollectionWrapper.class).getValue();
            if (wrapper != null) {
                for (T objInstance : wrapper.getRoot()) {
                    this.afterObjectSetCompleted(objInstance);
                    cellHandler.accept(objInstance);
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
