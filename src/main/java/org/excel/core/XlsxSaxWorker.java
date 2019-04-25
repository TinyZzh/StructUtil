/*
 *
 *
 *          Copyright (c) 2019. - TinyZ.
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

package org.excel.core;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.excel.annotation.ExcelSheet;
import org.excel.exception.EndOfExcelSheetException;
import org.excel.util.Reflects;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * SAX ExcelWorker. use event model to load .xlsx file.<br/>
 * <p>
 * <p> <a href='http://poi.apache.org/components/spreadsheet/index.html'>POI DOCUMENT</a></p>
 * <p> Limitations: </p>
 * <docs>
 * Due to the streaming nature of the implementation, there are the following limitations when compared to XSSF:<br/>
 * 1. Only a limited number of rows are accessible at a point in time.<br/>
 * 2. Sheet.clone() is not supported.<br/>
 * 3. Formula evaluation is not supported<br/>
 * <p>
 * See more details at SXSSF <a href='http://poi.apache.org/components/spreadsheet/how-to.html#sxssf'>How-To</a><br/>
 * <img src='http://poi.apache.org/components/spreadsheet/images/ss-features.png'/>
 * </docs>
 *
 * @param <T>
 */
public class XlsxSaxWorker<T> extends ExcelWorker<T> {

    public XlsxSaxWorker(String rootPath, Class<T> clzOfBean) {
        this(rootPath, clzOfBean, new HashMap<>());
    }

    public XlsxSaxWorker(String rootPath, Class<T> clzOfBean, Map<String, Map<Object, Object>> refFieldValueMap) {
        super(rootPath, clzOfBean, refFieldValueMap);
    }

    @Override
    protected void onLoadExcelSheetImpl(Consumer<T> cellHandler, ExcelSheet annotation, File file) {
        XlsxBeanSheetContentHandler<T> contentHandler = new XlsxBeanSheetContentHandler<>();
        contentHandler.setWorker(this);
        contentHandler.setFirstRow(annotation.startOrder());
        contentHandler.setLastRow(annotation.endOrder());
        contentHandler.setObjectConsumer(cellHandler);

        try (OPCPackage pkg = OPCPackage.open(file)) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
            ContentHandler handler = new XSSFSheetXMLHandler(styles, sharedStrings, contentHandler, true);

            XMLReader parser = SAXHelper.newXMLReader();
            parser.setContentHandler(handler);
            Iterator<InputStream> it = reader.getSheetsData();
            if (it instanceof XSSFReader.SheetIterator) {
                try {
                    while (it.hasNext()) {
                        InputStream inputStream = it.next();
                        if (((XSSFReader.SheetIterator) it).getSheetName().equalsIgnoreCase(annotation.sheetName())) {
                            parser.parse(new InputSource(inputStream));
                            break;
                        }
                    }
                } catch (EndOfExcelSheetException e) {
                    // row num large than endOrder. stop to load excel sheet.
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static class XlsxBeanSheetContentHandler<T> implements XSSFSheetXMLHandler.SheetContentsHandler {

        private XlsxSaxWorker<T> worker;
        private int firstRow = 0;
        private int lastRow = -1;
        private Consumer<T> objHandler;

        /**
         * column - field name
         */
        private Map<Integer, String> headRowMap = new HashMap<>();

        private boolean isFirstRow = true;

        private int curColumnIndex = 0;

        private T curInstance;

        @Override
        public void startRow(int rowNum) {
            if (this.lastRow >= 0 && rowNum >= this.lastRow)
                throw new EndOfExcelSheetException();
            if (rowNum >= this.firstRow) {
                curInstance = Reflects.newInstance(worker.clzOfBean);
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (!this.isFirstRow && curInstance != null) {
                worker.afterObjectSetCompleted(curInstance);
                this.objHandler.accept(curInstance);
            }
            this.isFirstRow = false;
            this.curColumnIndex = 0;
            this.curInstance = null;
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            try {
                if (isFirstRow) {
                    headRowMap.put(this.curColumnIndex, formattedValue.toLowerCase().trim());
                } else if (curInstance != null) {
                    worker.setObjectFieldValue(curInstance, headRowMap.get(this.curColumnIndex), curColumnIndex, formattedValue);
                }
            } finally {
                this.curColumnIndex++;
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            //  no-op
        }

        public void setFirstRow(int firstRow) {
            this.firstRow = firstRow;
        }

        public void setLastRow(int lastRow) {
            this.lastRow = lastRow;
        }

        public void setObjectConsumer(Consumer<T> consumer) {
            this.objHandler = consumer;
        }

        public void setWorker(XlsxSaxWorker<T> worker) {
            this.worker = worker;
        }
    }
}
