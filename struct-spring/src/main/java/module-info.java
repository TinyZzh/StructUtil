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

import org.struct.spring.handler.XmlStructHandler;

/**
 * @author TinyZ.
 * @version 2022.08.17
 */
module struct.spring {
    exports org.struct.spring.annotation;
    exports org.struct.spring.exceptions;
    exports org.struct.spring.handler;
    exports org.struct.spring.support;

    requires struct.core;

    requires org.slf4j;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.aop;
    requires spring.oxm;
    requires jakarta.xml.bind;

    provides org.struct.core.handler.StructHandler with
            XmlStructHandler;

}