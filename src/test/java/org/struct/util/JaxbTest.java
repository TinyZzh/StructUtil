package org.excel.util;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Collection;

/**
 * @author TinyZ.
 * @version 2020.03.17
 */
public class JaxbTest {


    @Test
    public void test() throws JAXBException {
        load();

    }

    public <T> Collection<T> load() throws JAXBException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                "  <child>\n" +
                "    <id>1</id>\n" +
                "    <gold>200</gold>\n" +
                "    <lv>1</lv>\n" +
                "    <activity>4221</activity>\n" +
                "    <packId>3071</packId>\n" +
                "    <addExp>20</addExp>\n" +
                "    <type>1</type>\n" +
                "    <discountedItems>10520</discountedItems>\n" +
                "  </child>\n" +
                "  <child>\n" +
                "    <id>2</id>\n" +
                "    <gold>1000</gold>\n" +
                "    <lv>2</lv>\n" +
                "    <activity>4222</activity>\n" +
                "    <packId>3072</packId>\n" +
                "    <addExp>30</addExp>\n" +
                "    <type>1</type>\n" +
                "    <discountedItems>10510</discountedItems>\n" +
                "  </child>" +
                "</root>";
        JAXBContext context = JAXBContext.newInstance(Wrapper.class, VipConfigSyncBean.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StreamSource source = new StreamSource(new StringReader(xml));
        Wrapper<T> data = (Wrapper<T>)unmarshaller.unmarshal(source, Wrapper.class).getValue();
        return data.getRoot();
    }

    public static class Wrapper<T> {

        private Collection<T> root;

        @XmlAnyElement(lax = true)
        public Collection<T> getRoot() {
            return root;
        }

        public void setRoot(Collection<T> root) {
            this.root = root;
        }
    }


}
