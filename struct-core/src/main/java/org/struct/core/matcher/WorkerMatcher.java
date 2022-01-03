/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

package org.struct.core.matcher;

import org.struct.core.handler.StructHandler;

import java.io.File;
import java.io.Serializable;

public interface WorkerMatcher extends Serializable {

    int HIGHEST = Integer.MIN_VALUE;

    int LOWEST = Integer.MAX_VALUE;

    /**
     * if active the auto model. match the file extension and chouce the matched worker to process data file.
     * {@link StructHandler}'s order. sorted from small to large.
     *
     * @return the worker's order.
     */
    int order();

    /**
     * @param file the file.
     * @return return true if auto match the file extensions. otherwise false.
     */
    boolean matchFile(File file);
}
