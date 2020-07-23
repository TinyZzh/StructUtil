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

package org.struct.spring.exceptions;

/**
 * @author TinyZ.
 * @date 2020-07-23.
 */
public class NoSuchKeyResolverException extends RuntimeException{
    private static final long serialVersionUID = -4909225945004896588L;

    public NoSuchKeyResolverException() {
        super();
    }

    public NoSuchKeyResolverException(String message) {
        super(message);
    }

    public NoSuchKeyResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchKeyResolverException(Throwable cause) {
        super(cause);
    }

    protected NoSuchKeyResolverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
