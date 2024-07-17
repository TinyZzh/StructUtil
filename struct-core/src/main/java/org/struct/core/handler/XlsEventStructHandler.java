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

import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.struct.core.StructDescriptor;
import org.struct.core.StructImpl;
import org.struct.core.StructWorker;
import org.struct.core.matcher.FileExtensionMatcher;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.exception.EndOfExcelSheetException;
import org.struct.exception.StructTransformException;
import org.struct.spi.SPI;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
@SPI(name = "xls")
public class XlsEventStructHandler implements StructHandler {

    private static final WorkerMatcher MATCHER = new FileExtensionMatcher(FileExtensionMatcher.FILE_XLS);

    @Override
    public WorkerMatcher matcher() {
        return MATCHER;
    }

    @Override
    public <T> void handle(StructWorker<T> worker, Class<T> clzOfStruct, Consumer<T> cellHandler, File file) {
        StructDescriptor descriptor = worker.getDescriptor();
        try (POIFSFileSystem fs = new POIFSFileSystem(file, true)) {
            //
            XlsListener<T> listener = new XlsListener<>(worker, descriptor, cellHandler);

            HSSFRequest request = new HSSFRequest();
            request.addListenerForAllRecords(listener.getFormatListener());
            HSSFEventFactory factory = new HSSFEventFactory();
            try {
                factory.processWorkbookEvents(request, fs);
            } catch (EndOfExcelSheetException e) {
                //  end of load.
            }
        } catch (Exception e) {
            throw new StructTransformException(e.getMessage(), e);
        }
    }

    /**
     * See <a href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java">XLS2CSVmra</a>
     */
    public static class XlsListener<T> implements HSSFListener {

        private StructWorker<T> worker;
        /**
         * The struct bean's descriptor.
         */
        private StructDescriptor descriptor;

        private Consumer<T> objHandler;
        /**
         * column - field name
         */
        private Map<Integer, String> headRowMap = new HashMap<>();
        /**
         * current sheet data.
         */
        private boolean isCurSheet = false;
        private boolean isFirstRow = true;
        private int startRow = -1;
        private int endRow = -1;

        private StructImpl rowStruct;

        //
        private int lastRowNumber;
        private int lastColumnNumber;

        // Records we pick up as we process
        private SSTRecord sstRecord;
        private FormatTrackingHSSFListener formatListener;

        // For handling formulas with string results
        private int nextRow;
        private int nextColumn;
        private boolean outputNextStringRecord;

        /**
         * Main HSSFListener method, processes events, and outputs the CSV as the file is processed.
         */
        private int sheetIndex;
        private BoundSheetRecord[] orderedBSRs;
        private List<BoundSheetRecord> boundSheetRecords = new ArrayList<BoundSheetRecord>();

        /**
         *
         */
        public XlsListener(StructWorker<T> worker, StructDescriptor descriptor, Consumer<T> objHandler) {
            this.worker = worker;
            this.descriptor = descriptor;
            this.objHandler = objHandler;

            MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
            formatListener = new FormatTrackingHSSFListener(listener);
        }

        @Override
        public void processRecord(Record record) {
            int thisRow = -1;
            int thisColumn = -1;
            String thisStr = null;

            switch (record.getSid()) {
                case BoundSheetRecord.sid:
                    boundSheetRecords.add((BoundSheetRecord) record);
                    break;
                case BOFRecord.sid:
                    BOFRecord br = (BOFRecord) record;
                    if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                        if (orderedBSRs == null) {
                            orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                        }
                        sheetIndex++;

                        this.isCurSheet = descriptor.getSheetName().equalsIgnoreCase(orderedBSRs[sheetIndex - 1].getSheetname());
                    }
                    break;
                case RowRecord.sid:
                    RowRecord rr = (RowRecord) record;
                    if (this.isCurSheet) {
                        if (this.startRow < 0) {
                            this.startRow = Math.max(descriptor.getStartOrder(), rr.getRowNumber());
                        }
                        if (descriptor.getEndOrder() > 0) {
                            this.endRow = Math.min(descriptor.getEndOrder(), rr.getRowNumber());
                        } else {
                            this.endRow = rr.getRowNumber() + 1;
                        }
                    }
                    break;
                case SSTRecord.sid:
                    sstRecord = (SSTRecord) record;
                    break;
                case BlankRecord.sid:
                case BoolErrRecord.sid:
                case NoteRecord.sid:
                case RKRecord.sid:
                    //  invalid excel cell value.
                    CellValueRecordInterface rc = (CellValueRecordInterface) record;
                    thisRow = rc.getRow();
                    thisColumn = rc.getColumn();
                    thisStr = "";
                    break;
                case FormulaRecord.sid:
                    FormulaRecord frec = (FormulaRecord) record;

                    thisRow = frec.getRow();
                    thisColumn = frec.getColumn();
                    if (Double.isNaN(frec.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        outputNextStringRecord = true;
                        nextRow = frec.getRow();
                        nextColumn = frec.getColumn();
                    } else {
                        thisStr = formatListener.formatNumberDateCell(frec);
                    }
                    break;
                case StringRecord.sid:
                    if (outputNextStringRecord) {
                        // String for formula
                        StringRecord srec = (StringRecord) record;
                        thisStr = srec.getString();
                        thisRow = nextRow;
                        thisColumn = nextColumn;
                        outputNextStringRecord = false;
                    }
                    break;
                case LabelRecord.sid:
                    LabelRecord lrec = (LabelRecord) record;

                    thisRow = lrec.getRow();
                    thisColumn = lrec.getColumn();
                    thisStr = lrec.getValue();
                    break;
                case LabelSSTRecord.sid:
                    LabelSSTRecord lsrec = (LabelSSTRecord) record;

                    thisRow = lsrec.getRow();
                    thisColumn = lsrec.getColumn();
                    if (sstRecord == null) {
                        thisStr = "";
                    } else {
                        thisStr = sstRecord.getString(lsrec.getSSTIndex()).toString();
                    }
                    break;

                case NumberRecord.sid:
                    NumberRecord numrec = (NumberRecord) record;

                    thisRow = numrec.getRow();
                    thisColumn = numrec.getColumn();

                    // Format
                    thisStr = formatListener.formatNumberDateCell(numrec);
                    break;
                default:
                    break;
            }

            // Handle new row
            if (thisRow != -1 && thisRow != lastRowNumber) {
                lastColumnNumber = -1;

                if (!isFirstRow && this.isCurSheet && thisRow >= this.startRow) {
                    rowStruct = new StructImpl();
                }
            }

            // Handle missing column
            if (record instanceof MissingCellDummyRecord) {
                MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
                thisRow = mc.getRow();
                thisColumn = mc.getColumn();
                thisStr = "";
            }

            // If we got something to print out, do so
            if (thisStr != null) {
                setObjectFieldValue(thisRow, thisColumn, thisStr);
            }

            // Update column and row count
            if (thisRow > -1)
                lastRowNumber = thisRow;
            if (thisColumn > -1)
                lastColumnNumber = thisColumn;

            // Handle end of row
            if (record instanceof LastCellOfRowDummyRecord) {
                // We're onto a new row
                lastColumnNumber = -1;


                // End the row
                if (!this.isFirstRow && rowStruct != null) {
                    worker.createInstance(rowStruct).ifPresent(this.objHandler);
                    this.rowStruct = null;
                }
                this.isFirstRow = false;

                if (this.endRow >= 0 && lastRowNumber + 1 >= this.endRow)
                    throw new EndOfExcelSheetException();
            }
        }

        private void setObjectFieldValue(int curRow, int columnIndex, String value) {
            String val = value.trim();
            if (isFirstRow) {
                headRowMap.put(columnIndex, val);
            } else if (rowStruct != null) {
                rowStruct.add(headRowMap.get(columnIndex), val);
            }
        }

        public FormatTrackingHSSFListener getFormatListener() {
            return formatListener;
        }
    }
}
