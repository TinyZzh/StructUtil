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

import org.struct.core.converter.EmbeddedConverters;
import org.struct.core.factory.DefaultStructFactoryBean;
import org.struct.core.handler.CsvStructHandler;
import org.struct.core.handler.ExcelUMStructHandler;
import org.struct.core.handler.JsonStructHandler;
import org.struct.core.handler.XlsEventStructHandler;
import org.struct.core.handler.XlsxSaxStructHandler;

/**
 * @author TinyZ.
 * @version 2022.08.17
 */
module struct.core {
    exports org.struct.annotation;
    exports org.struct.core;
    exports org.struct.core.handler;
    exports org.struct.core.converter;
    exports org.struct.core.factory;
    exports org.struct.core.filter;
    exports org.struct.core.matcher;
    exports org.struct.exception;
    exports org.struct.spi;
    exports org.struct.util;
    exports org.struct.support;

    requires java.xml;
    requires org.slf4j;
    requires com.google.gson;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    provides org.struct.core.factory.StructFactoryBean with
            DefaultStructFactoryBean;
    provides org.struct.core.handler.StructHandler with
            JsonStructHandler,
            CsvStructHandler,
            XlsEventStructHandler,
            XlsxSaxStructHandler,
            ExcelUMStructHandler
            ;
    provides org.struct.core.converter.Converters with
            EmbeddedConverters;

}