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

package org.struct.bootfixture;

import org.struct.annotation.StructField;
import org.struct.annotation.StructSheet;

/**
 * A struct bean for the file watcher test - it references another sheet so that
 * {@code Reflects#resolveStructRelatedFileName} resolves a name and the watcher actually walks it.
 *
 * @author TinyZ.
 */
@StructSheet(fileName = "tpl_val.json")
public class BootSheetBean {

    public int key;
    public String val;

    @StructField(ref = BootChildBean.class, refUniqueKey = "key")
    public BootChildBean child;
}
