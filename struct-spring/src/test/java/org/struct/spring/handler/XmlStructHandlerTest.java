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

package org.struct.spring.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.core.filter.StructBeanFilter;
import org.struct.exception.StructTransformException;
import org.struct.util.WorkerUtil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class XmlStructHandlerTest {

    @Test
    public void test() {
        XmlStructHandler handler = new XmlStructHandler();
        Assertions.assertNotNull(handler.matcher());
        StructWorker<BasicXmlBean> worker = WorkerUtil.newWorker(WORKSPACE, BasicXmlBean.class);
        worker.checkStructFactory();
        List<BasicXmlBean> beans = new ArrayList<>();
        handler.handle(worker, BasicXmlBean.class, beans::add, new File(WorkerUtil.resolveFilePath(WORKSPACE, FILE)));
        Assertions.assertFalse(beans.isEmpty());
        Assertions.assertEquals(3, beans.size());
        BasicXmlBean bean = beans.get(1);
        Assertions.assertEquals(2, bean.id);
        Assertions.assertEquals((byte) 2, bean.byteValue);
        Assertions.assertFalse(bean.booleanValue);
        Assertions.assertEquals((short) 2, bean.shortValue);
        Assertions.assertEquals(2, bean.intValue);
        Assertions.assertEquals(2L, bean.longValue);
        Assertions.assertEquals(2F, bean.floatValue);
        Assertions.assertEquals(2D, bean.doubleValue);
        Assertions.assertEquals((byte) 2, bean.byteObj);
        Assertions.assertEquals(false, bean.booleanObj);
        Assertions.assertEquals((short) 2, bean.shortObj);
        Assertions.assertEquals(2, bean.intObj);
        Assertions.assertEquals(2L, bean.longObj);
        Assertions.assertEquals(2F, bean.floatObj);
        Assertions.assertEquals(2D, bean.doubleObj);
        Assertions.assertEquals("2", bean.stringObj);
        Assertions.assertEquals(BigInteger.valueOf(2), bean.bigIntegerObj);
        Assertions.assertEquals(BigDecimal.valueOf(2), bean.bigDecimalObj);
        Assertions.assertArrayEquals(new int[]{2}, bean.intValueAry);
        Assertions.assertArrayEquals(new Double[]{2.0D}, bean.doubleObjAry);
    }

    @Test
    public void testLoadWithBeanFilter() {
        XmlStructHandler handler = new XmlStructHandler();
        StructWorker<BasicXmlBeanWithFilter> worker = spy(WorkerUtil.newWorker(WORKSPACE, BasicXmlBeanWithFilter.class));
        worker.checkStructFactory();
        List<BasicXmlBeanWithFilter> beans = new ArrayList<>();
        handler.handle(worker, BasicXmlBeanWithFilter.class, beans::add, new File(WorkerUtil.resolveFilePath(WORKSPACE, FILE)));
        Assertions.assertFalse(beans.isEmpty());
        Assertions.assertEquals(1, beans.size());
        Assertions.assertEquals(2, beans.get(0).id);

        doThrow(NullPointerException.class).when(worker).createInstance(any(Object.class));
        // doThrow(NullPointerException.class).when(worker).createInstance(any(StructImpl.class));
        Assertions.assertThrows(StructTransformException.class, () ->
                handler.handle(worker, BasicXmlBeanWithFilter.class, beans::add, new File(WorkerUtil.resolveFilePath(WORKSPACE, FILE))));
    }

    private static final String FILE = "tpl_xml_struct_handler.xml";
    private static final String WORKSPACE = "classpath:";

    @XmlRootElement(name = "child")
    @XmlAccessorType(XmlAccessType.FIELD)
    @StructSheet(fileName = FILE, startOrder = 1)
    static class BasicXmlBean {
        public int id;

        public byte byteValue;
        public boolean booleanValue;
        public short shortValue;
        public int intValue;
        public long longValue;
        public float floatValue;
        public double doubleValue;
        public Byte byteObj;
        public Boolean booleanObj;
        public Short shortObj;
        public Integer intObj;
        public Long longObj;
        public Float floatObj;
        public Double doubleObj;
        public String stringObj;
        public BigInteger bigIntegerObj;
        public BigDecimal bigDecimalObj;
        public int[] intValueAry;
        public Double[] doubleObjAry;

        @XmlElementWrapper(name = "subList")
        @XmlElement(name = "sub")
        public List<Sub> subList;
        @XmlJavaTypeAdapter(value = SubMapXmlAdapter.class)
        @XmlElement(name = "subMap")
        public Map<Integer, Sub> subMap;
    }

    static class Sub {
        public int lv;
        public String name;
    }

    @XmlType
    static class SubListClz {
        @XmlElement(name = "sub")
        public List<Sub> list = new ArrayList<>();
    }

    static class SubMapXmlAdapter extends XmlAdapter<SubListClz, Map<Integer, Sub>> {

        @Override
        public Map<Integer, Sub> unmarshal(SubListClz v) throws Exception {
            return v.list.stream().collect(Collectors.toMap(b -> b.lv, b -> b));
        }

        @Override
        public SubListClz marshal(Map<Integer, Sub> v) throws Exception {
            SubListClz obj = new SubListClz();
            obj.list = new ArrayList<>(v.values());
            return obj;
        }
    }

    @XmlRootElement(name = "child")
    @StructSheet(fileName = FILE, startOrder = 2, endOrder = 2, filter = MyFilter.class)
    static class BasicXmlBeanWithFilter {

        public int id;
    }

    static class MyFilter extends StructBeanFilter<BasicXmlBeanWithFilter> {
        public MyFilter(Consumer<BasicXmlBeanWithFilter> cellHandler) {
            super(cellHandler);
        }

        @Override
        public boolean test(BasicXmlBeanWithFilter bean) {
            return bean.id >= 2;
        }
    }
}