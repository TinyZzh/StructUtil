/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
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

package org.struct.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class StructContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructContext.class);

    private static StructConfig DEFAULT_CONFIG = new StructConfig();

    static {
        for (Field field : StructConfig.class.getDeclaredFields()) {
            try {
                field.set(DEFAULT_CONFIG, System.getProperty(field.getName()));
            } catch (Exception e) {
                LOGGER.warn("");
            }
        }
    }

    private StructContext() {
        //  no-op
    }


}
