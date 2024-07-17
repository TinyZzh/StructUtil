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

import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.core.handler.XlsEventStructHandler.XlsListener;
import org.struct.util.WorkerUtil;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author TinyZ.
 * @version 2022.05.02
 */
class XlsEventStructHandlerTest {

    @Test
    public void test() throws Throwable {
        XlsEventStructHandler handler = new XlsEventStructHandler();
        Assertions.assertNotNull(handler.matcher());
        String workspace = "classpath:/org/struct/core/";
        StructWorker<Temp> worker = new StructWorker<>(workspace, Temp.class);
        // worker.toList(ArrayList::new);
        MethodHandle mh = MethodHandles.privateLookupIn(StructWorker.class, MethodHandles.lookup())
                .findVirtual(StructWorker.class, "checkStructFactory", MethodType.methodType(void.class));
        mh.invoke(worker);
        List<Temp> list = new ArrayList<>();
        handler.handle(worker, Temp.class, list::add, new File(WorkerUtil.resolveFilePath(workspace, "bean.xls")));
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    public void testProcessRecord() {
        XlsListener listener = new XlsListener(null, null, null);
        {
            RKRecord record = mock(RKRecord.class);
            Mockito.doReturn(RKRecord.sid).when(record).getSid();
            listener.processRecord(record);
        }
        {
            FormulaRecord record = mock(FormulaRecord.class);
            listener.getFormatListener().processRecord(record);
            Mockito.doReturn(FormulaRecord.sid).when(record).getSid();
//            listener.processRecord(record);
            Mockito.doReturn(Double.NaN).when(record).getValue();
            listener.processRecord(record);
        }
        {
            StringRecord record = mock(StringRecord.class);
            Mockito.doReturn(StringRecord.sid).when(record).getSid();
            listener.processRecord(record);
        }
        {
            LabelRecord record = mock(LabelRecord.class);
            Mockito.doReturn(LabelRecord.sid).when(record).getSid();
            listener.processRecord(record);
        }
        {
            MissingCellDummyRecord record = mock(MissingCellDummyRecord.class);
            listener.processRecord(record);
        }
    }

    @StructSheet(fileName = "bean.xls", sheetName = "Sheet1")
    public static class Temp {
        private int id;

        @StructField(name = "name")
        private String name;

        @StructField()
        private Double weight;
    }
}