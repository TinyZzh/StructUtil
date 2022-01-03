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

package org.struct.core.handler;

import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructDescriptor;
import org.struct.util.WorkerUtil;

import java.io.File;

@StructSheet(fileName = "")
public class CompletableFutureTest {

    @Test
    public void test() {
        String path = WorkerUtil.resolveFilePath("file:examples/CfgItem.xlsx", new StructDescriptor(CompletableFutureTest.class).getFileName());
        System.out.println(path);

        System.out.println(new File(path).exists());
        System.out.println(new File(path));
    }
}
