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

package org.struct.exception;

/**
 * @author TinyZ.
 * @version 2019.05.20
 */
public class ExcelTransformException extends RuntimeException {
    private static final long serialVersionUID = 8846222282251755758L;

    public ExcelTransformException() {
        super();
    }

    public ExcelTransformException(String message) {
        super(message);
    }

    public ExcelTransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelTransformException(Throwable cause) {
        super(cause);
    }

    public ExcelTransformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
